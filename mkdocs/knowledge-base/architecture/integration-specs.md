# Integration Specifications — Saudi National Systems

## Overview

This document provides the detailed, developer-ready integration specifications for connecting to Saudi Arabia national systems (Yakeen, Najm, SAMA) and payment gateways (MADA, SADAD). Each integration includes API specs, error handling, retry logic, and sample code.

---

## 1. Yakeen — National Identity Verification

### 1.1 Overview

Yakeen is the Saudi government system for verifying citizen and resident identity (NIN/Iqama). It is operated by the National Information Center (NIC).

| Property | Value |
|----------|-------|
| **Provider** | National Information Center (NIC) / ELM |
| **Protocol** | REST over HTTPS (mTLS) |
| **Authentication** | Client certificate + API Key |
| **Rate Limit** | 100 requests/minute per API key |
| **SLA** | 99.5% uptime, < 2s response time |
| **Environment** | Sandbox: `https://sandbox.yakeen.sa/api/v1` |
| **Production** | `https://api.yakeen.sa/api/v1` |

### 1.2 API Endpoints

#### POST /yakeen/v1/verify-citizen

Verify a Saudi citizen's identity.

**Request:**
```json
{
  "nationalId": "1098765432",
  "dateOfBirthG": "1990-05-15",
  "dateOfBirthH": "1410-02-15"
}
```

**Success Response (200):**
```json
{
  "status": "VALID",
  "citizenId": "1098765432",
  "firstNameAr": "أحمد",
  "fatherNameAr": "محمد",
  "grandFatherNameAr": "عبدالله",
  "lastNameAr": "الحربي",
  "firstNameEn": "Ahmed",
  "fatherNameEn": "Mohammed",
  "grandFatherNameEn": "Abdullah",
  "lastNameEn": "Al-Harbi",
  "gender": "M",
  "dateOfBirthG": "1990-05-15",
  "dateOfBirthH": "1410-02-15",
  "nationalityCode": "SAU",
  "occupationCode": "ENG_001",
  "occupationDescAr": "مهندس",
  "occupationDescEn": "Engineer",
  "address": {
    "region": "Riyadh",
    "city": "Riyadh",
    "district": "Al-Malaz"
  }
}
```

**Error Responses:**

| HTTP Status | Error Code | Description | Action |
|-------------|------------|-------------|--------|
| 400 | `INVALID_NATIONAL_ID` | NIN format invalid | Return validation error to user |
| 401 | `UNAUTHORIZED` | Invalid API key or certificate | Alert operations team |
| 404 | `CITIZEN_NOT_FOUND` | NIN not found in Yakeen | Prompt user to verify NIN |
| 429 | `RATE_LIMIT_EXCEEDED` | Too many requests | Retry with exponential backoff |
| 503 | `SERVICE_UNAVAILABLE` | Yakeen system down | Fallback to manual verification |

#### POST /yakeen/v1/verify-resident

Verify a Saudi resident (Iqama) identity.

**Request:**
```json
{
  "iqamaNumber": "2345678901",
  "dateOfBirthG": "1985-08-20",
  "expiryDate": "2026-08-20"
}
```

**Success Response (200):**
```json
{
  "status": "VALID",
  "iqamaNumber": "2345678901",
  "firstNameAr": "محمد",
  "fatherNameAr": "علي",
  "lastNameAr": "الهندي",
  "firstNameEn": "Mohammed",
  "fatherNameEn": "Ali",
  "lastNameEn": "Al-Hindi",
  "gender": "M",
  "dateOfBirthG": "1985-08-20",
  "nationalityCode": "IND",
  "occupationCode": "TCH_002",
  "occupationDescEn": "Technician",
  "iqamaExpiryDate": "2026-08-20"
}
```

### 1.3 Java Adapter Implementation

```java
@Component
public class YakeenIdentityAdapter implements IdentityVerificationPort {

    private final YakeenApiClient client;
    private final YakeenRequestMapper mapper;
    private final MetricsRecorder metrics;

    @Override
    @CircuitBreaker(name = "yakeen", fallbackMethod = "verificationFallback")
    @Retry(name = "yakeen", fallbackMethod = "verificationFallback")
    @TimeLimiter(name = "yakeen")
    public IdentityVerificationResult verify(IdentityVerificationRequest request) {
        long start = System.currentTimeMillis();
        try {
            YakeenRequest yakeenRequest = mapper.toExternal(request);
            YakeenResponse response = client.getCitizenInfo(yakeenRequest);
            metrics.recordSuccess("yakeen", System.currentTimeMillis() - start);
            return mapper.toDomain(response);
        } catch (YakeenNotFoundException e) {
            metrics.recordFailure("yakeen", "NOT_FOUND");
            return IdentityVerificationResult.notFound(request.getNationalId());
        } catch (YakeenRateLimitException e) {
            metrics.recordFailure("yakeen", "RATE_LIMIT");
            throw e; // Let retry mechanism handle
        } catch (Exception e) {
            metrics.recordFailure("yakeen", "ERROR");
            throw e;
        }
    }

    private IdentityVerificationResult verificationFallback(
            IdentityVerificationRequest request, Exception ex) {
        log.warn("Yakeen unavailable — deferring verification for NIN: {}",
                 MaskUtil.maskNin(request.getNationalId()));
        return IdentityVerificationResult.deferred(
            request.getNationalId(),
            "Yakeen unavailable, verification deferred"
        );
    }
}
```

### 1.4 Resilience Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      yakeen:
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
        permittedNumberOfCallsInHalfOpenState: 3
        recordExceptions:
          - org.springframework.web.client.HttpServerErrorException
          - java.net.ConnectException
          - java.net.SocketTimeoutException
        ignoreExceptions:
          - com.insurance.integration.yakeen.YakeenNotFoundException
  retry:
    instances:
      yakeen:
        maxAttempts: 3
        waitDuration: 1s
        exponentialBackoffMultiplier: 2
        retryExceptions:
          - org.springframework.web.client.HttpServerErrorException
          - java.net.ConnectException
          - java.net.SocketTimeoutException
  timelimiter:
    instances:
      yakeen:
        timeoutDuration: 5s
        cancelRunningFuture: true
```

---

## 2. Najm — Accident and Claims History

### 2.1 Overview

Najm provides access to vehicle accident history for Saudi motor insurance underwriting. It is operated by the Najm Company for Insurance Services.

| Property | Value |
|----------|-------|
| **Provider** | Najm Company for Insurance Services |
| **Protocol** | SOAP/XML (WSDL-based) |
| **Authentication** | WS-Security UsernameToken |
| **Rate Limit** | 200 requests/minute per insurer |
| **SLA** | 99.0% uptime, < 3s response time |
| **Environment** | Sandbox: `https://sandbox.najm.sa/najmws/AccidentHistoryService` |
| **Production** | `https://api.najm.sa/najmws/AccidentHistoryService` |

### 2.2 SOAP Operations

#### GetAccidentHistory

**SOAP Request:**
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:naj="http://najm.sa/api/v2">
   <soapenv:Header>
      <wsse:Security xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
         <wsse:UsernameToken>
            <wsse:Username>INSURER_CODE</wsse:Username>
            <wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">API_KEY</wsse:Password>
         </wsse:UsernameToken>
      </wsse:Security>
   </soapenv:Header>
   <soapenv:Body>
      <naj:GetAccidentHistoryRequest>
         <naj:RequestType>VEHICLE</naj:RequestType>
         <naj:Identifier>1234567890</naj:Identifier>
         <naj:InsuranceCompanyCode>INS_001</naj:InsuranceCompanyCode>
      </naj:GetAccidentHistoryRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

**SOAP Response:**
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
   <soapenv:Body>
      <naj:GetAccidentHistoryResponse>
         <naj:ResponseCode>00</naj:ResponseCode>
         <naj:ResponseMessage>Success</naj:ResponseMessage>
         <naj:AccidentRecords>
            <naj:AccidentRecord>
               <naj:AccidentID>ACC-2024-12345</naj:AccidentID>
               <naj:AccidentDate>2024-03-15</naj:AccidentDate>
               <naj:AccidentType>COLLISION</naj:AccidentType>
               <naj:FaultPercentage>100</naj:FaultPercentage>
               <naj:ClaimStatus>PAID</naj:ClaimStatus>
               <naj:PaidAmount>8500.00</naj:PaidAmount>
            </naj:AccidentRecord>
         </naj:AccidentRecords>
         <naj:TotalAccidents>1</naj:TotalAccidents>
         <naj:RiskScore>LOW</naj:RiskScore>
      </naj:GetAccidentHistoryResponse>
   </soapenv:Body>
</soapenv:Envelope>
```

### 2.3 SOAP Client Configuration

```java
@Configuration
public class NajmSoapClientConfig {

    @Value("${najm.wsdl.url}")
    private String wsdlUrl;

    @Value("${najm.username}")
    private String username;

    @Value("${najm.password}")
    private String password;

    @Bean
    public Jaxb2Marshaller najmMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("com.insurance.integration.najm.generated");
        return marshaller;
    }

    @Bean
    public NajmAccidentHistoryClient najmClient(Jaxb2Marshaller najmMarshaller) {
        NajmAccidentHistoryClient client = new NajmAccidentHistoryClient();
        client.setDefaultUri(wsdlUrl);
        client.setMarshaller(najmMarshaller);
        client.setUnmarshaller(najmMarshaller);
        
        // Add WS-Security header
        ClientInterceptor interceptor = new ClientInterceptor() {
            @Override
            public boolean handleRequest(MessageContext messageContext) {
                SoapMessage soapMessage = (SoapMessage) messageContext.getRequest();
                soapMessage.setSoapAction("GetAccidentHistory");
                // Add security header
                try {
                    soapMessage.getDocument().getDocumentElement()
                        .setAttribute("xmlns:wsse", 
                            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
                } catch (Exception e) {
                    throw new RuntimeException("Failed to add security header", e);
                }
                return true;
            }
        };
        client.setInterceptors(interceptor);
        return client;
    }
}
```

### 2.4 Error Handling

| SOAP Fault Code | Meaning | Action |
|-----------------|---------|--------|
| `INVALID_IDENTIFIER` | Sequence number or NIN invalid | Return validation error |
| `IDENTIFIER_NOT_FOUND` | No records found | Return empty result |
| `AUTHENTICATION_FAILED` | Invalid credentials | Alert operations, retry with new credentials |
| `RATE_LIMIT_EXCEEDED` | Too many requests | Retry with backoff |
| `INTERNAL_ERROR` | Najm system error | Fallback to manual underwriting |

### 2.5 Response Mapping

```java
@Component
public class NajmResponseMapper {

    public AccidentHistoryResult toDomain(GetAccidentHistoryResponse response) {
        List<AccidentRecord> records = response.getAccidentRecords().stream()
            .map(this::mapRecord)
            .toList();
        
        return AccidentHistoryResult.builder()
            .totalAccidents(response.getTotalAccidents())
            .riskScore(RiskScore.valueOf(response.getRiskScore()))
            .records(records)
            .retrievedAt(Instant.now())
            .build();
    }

    private AccidentRecord mapRecord(AccidentRecordXml xml) {
        return AccidentRecord.builder()
            .accidentId(xml.getAccidentID())
            .accidentDate(LocalDate.parse(xml.getAccidentDate()))
            .accidentType(xml.getAccidentType())
            .faultPercentage(xml.getFaultPercentage())
            .claimStatus(xml.getClaimStatus())
            .paidAmount(new BigDecimal(xml.getPaidAmount()))
            .build();
    }
}
```

---

## 3. MADA Payment Gateway

### 3.1 Overview

MADA is the Saudi national payment card network. Integration is handled through a PCI-DSS compliant payment gateway.

| Property | Value |
|----------|-------|
| **Provider** | MADA / Acquiring Bank |
| **Protocol** | REST over HTTPS |
| **Authentication** | API Key + HMAC Signature |
| **PCI Scope** | Tokenized flow (no raw PAN handling) |
| **SLA** | 99.9% uptime, < 3s response time |

### 3.2 Payment Flow

```
1. POST /api/v1/payments/initiate  →  Returns payment URL
2. User redirected to MADA hosted payment page
3. User completes authentication (3DS/OTP)
4. POST webhook to /api/v1/payments/webhook  →  Payment result
5. GET /api/v1/payments/{ref}/status  →  Verify status
```

### 3.3 API Endpoints

#### POST /api/v1/payments/initiate

```json
{
  "amount": 2500.00,
  "currency": "SAR",
  "merchantReference": "POL-MTR-2026-00001",
  "customer": {
    "name": "Ahmed Al-Harbi",
    "mobile": "+966501234567",
    "email": "ahmed@example.com"
  },
  "returnUrl": "https://app.insurance.sa/payment/success",
  "cancelUrl": "https://app.insurance.sa/payment/cancel",
  "webhookUrl": "https://api.insurance.sa/api/v1/payments/webhook"
}
```

**Response:**
```json
{
  "paymentId": "pay_a1b2c3d4",
  "paymentUrl": "https://mada-gateway.sa/pay/pay_a1b2c3d4",
  "expiresAt": "2026-01-01T09:20:00.000Z",
  "status": "PENDING"
}
```

#### POST /api/v1/payments/webhook

```json
{
  "paymentId": "pay_a1b2c3d4",
  "merchantReference": "POL-MTR-2026-00001",
  "amount": 2500.00,
  "currency": "SAR",
  "status": "CAPTURED",
  "gatewayReference": "GW-987654321",
  "paymentMethod": "MADA",
  "cardLastFour": "1234",
  "timestamp": "2026-01-01T09:15:00.000Z",
  "signature": "sha256_hmac_signature_here"
}
```

### 3.4 Webhook Signature Verification

```java
@Component
public class PaymentWebhookHandler {

    @Value("${payment.gateway.secret}")
    private String webhookSecret;

    public boolean verifySignature(PaymentWebhook payload, String signature) {
        String expectedSignature = HmacUtils.hmacSha256Hex(
            webhookSecret,
            payload.getPaymentId() + 
            payload.getAmount().toString() +
            payload.getCurrency() +
            payload.getStatus()
        );
        return expectedSignature.equals(signature);
    }

    public void handleWebhook(PaymentWebhook payload, String signature) {
        if (!verifySignature(payload, signature)) {
            throw new SecurityException("Invalid webhook signature");
        }
        
        switch (payload.getStatus()) {
            case "CAPTURED":
                billingService.confirmPayment(payload);
                break;
            case "FAILED":
                billingService.handleFailedPayment(payload);
                break;
            case "REFUNDED":
                billingService.handleRefund(payload);
                break;
        }
    }
}
```

---

## 4. SADAD Bill Payment

### 4.1 Overview

SADAD is the Saudi national bill payment system. Policies generate SADAD bill numbers for customer payment at banks or mobile apps.

| Property | Value |
|----------|-------|
| **Provider** | Saudi Arabian Monetary Authority (SAMA) |
| **Protocol** | SFTP + XML file exchange |
| **Authentication** | Digital certificate |
| **Biller Code** | Assigned by SAMA per insurer |

### 4.2 Bill Presentment File Format

```xml
<?xml version="1.0" encoding="UTF-8"?>
<SadadBills>
  <BatchHeader>
    <BillerId>INS_001</BillerId>
    <BatchDate>2026-01-01</BatchDate>
    <RecordCount>1</RecordCount>
    <TotalAmount>2500.00</TotalAmount>
  </BatchHeader>
  <Bill>
    <BillReference>POL-MTR-2026-00001</BillReference>
    <BillAmount>2500.00</BillAmount>
    <DueDate>2026-01-15</DueDate>
    <CustomerName>Ahmed Al-Harbi</CustomerName>
    <CustomerMobile>+966501234567</CustomerMobile>
    <CustomerNationalId>1098765432</CustomerNationalId>
    <BillDescription>Motor Insurance Policy - Comprehensive</BillDescription>
  </Bill>
</SadadBills>
```

### 4.3 Payment Confirmation File (Received from SADAD)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<SadadPayments>
  <Payment>
    <BillReference>POL-MTR-2026-00001</BillReference>
    <PaymentAmount>2500.00</PaymentAmount>
    <PaymentDate>2026-01-05</PaymentDate>
    <PaymentMethod>SADAD</PaymentMethod>
    <BankCode>RIBL</BankCode>
    <SADADReference>SDD-2026-123456</SADADReference>
    <Status>PAID</Status>
  </Payment>
</SadadPayments>
```

---

## 5. SAMA Regulatory Reporting

### 5.1 Overview

Motor insurance data is reported to SAMA at defined intervals.

| Report | Frequency | Format | Delivery Method | Due Date |
|--------|-----------|--------|-----------------|----------|
| Motor Policy Register | Monthly | XML/CSV | SFTP | 10th of next month |
| Claims Register | Monthly | XML/CSV | SFTP | 10th of next month |
| Financial Solvency | Quarterly | SAMA Portal | Web upload | 30 days after quarter end |
| Fraud Incident Report | Ad-hoc | SAMA Portal | Web upload | Within 24 hours |

### 5.2 Motor Policy Register XML Format

```xml
<?xml version="1.0" encoding="UTF-8"?>
<SamaReport>
  <ReportHeader>
    <ReportType>MOTOR_POLICY_REGISTER</ReportType>
    <ReportPeriod>2026-Q1</ReportPeriod>
    <InsurerCode>INS_001</InsurerCode>
    <InsurerName>Insurance Company SA</InsurerName>
    <GeneratedAt>2026-04-01T02:00:00</GeneratedAt>
    <RecordCount>1500</RecordCount>
  </ReportHeader>
  <Records>
    <Policy>
      <PolicyNumber>POL-MTR-2026-00001</PolicyNumber>
      <ProductCode>MOTOR_COMP</ProductCode>
      <Status>ACTIVE</Status>
      <EffectiveDate>2026-01-01</EffectiveDate>
      <ExpiryDate>2026-12-31</ExpiryDate>
      <CustomerNationalId>1098765432</CustomerNationalId>
      <CustomerName>Ahmed Al-Harbi</CustomerName>
      <VehicleSequenceNumber>1234567890</VehicleSequenceNumber>
      <VehiclePlateNumber>ABC1234</VehiclePlateNumber>
      <Premium>2500.00</Premium>
      <Commission>250.00</Commission>
      <TaxAmount>375.00</TaxAmount>
      <IssuedAt>2026-01-01T09:15:00</IssuedAt>
    </Policy>
  </Records>
</SamaReport>
```

### 5.3 Report Generation Job

```java
@Component
public class SamaReportGenerator {

    private final JdbcTemplate jdbcTemplate;
    private final SftpClient sftpClient;

    @Scheduled(cron = "0 0 2 1 * ?") // 2 AM on 1st of every month
    public void generateMonthlyReports() {
        String period = generatePeriod();
        generatePolicyRegister(period);
        generateClaimsRegister(period);
    }

    private void generatePolicyRegister(String period) {
        List<PolicyRecord> policies = jdbcTemplate.query(
            """
            SELECT p.policy_number, p.product_code, p.status,
                   p.effective_date, p.expiry_date, p.annual_premium,
                   p.commission_amount, p.tax_amount, p.issued_at,
                   c.national_id_encrypted, c.full_name_ar,
                   mv.sequence_number, mv.plate_number
            FROM core.policies p
            JOIN core.customers c ON c.id = p.customer_id
            LEFT JOIN motor.motor_vehicles mv ON mv.policy_id = p.id
            WHERE p.line_of_business = 'MOTOR'
              AND p.created_at >= DATE_TRUNC('month', CURRENT_DATE - INTERVAL '1 month')
              AND p.created_at < DATE_TRUNC('month', CURRENT_DATE)
            """,
            new PolicyRecordRowMapper()
        );

        String xml = generatePolicyRegisterXml(policies, period);
        String filename = String.format("MOTOR_POLICY_REGISTER_%s.xml", period);
        
        sftpClient.upload("/sama/incoming/" + filename, xml.getBytes(StandardCharsets.UTF_8));
        
        // Record in database
        jdbcTemplate.update(
            "INSERT INTO reporting.sama_reporting (report_type, report_period, status, data, file_reference) VALUES (?, ?, 'GENERATED', ?::jsonb, ?)",
            "MOTOR_POLICY_REGISTER", period, 
            "{\"recordCount\": " + policies.size() + "}", 
            filename
        );
    }
}
```

---

## 6. Integration Error Handling Summary

| Integration | Error Scenario | HTTP/Fault Code | Retry? | Fallback |
|-------------|---------------|-----------------|--------|----------|
| Yakeen | Invalid NIN | 400 | No | Return validation error |
| Yakeen | Rate limited | 429 | Yes (3x, backoff) | Defer verification |
| Yakeen | Service down | 503 | Yes (3x, backoff) | Defer verification |
| Yakeen | Auth failure | 401 | No | Alert operations |
| Najm | Invalid identifier | SOAP fault | No | Return validation error |
| Najm | Rate limited | SOAP fault | Yes (3x, backoff) | Manual underwriting |
| Najm | Service down | SOAP fault | Yes (3x, backoff) | Manual underwriting |
| MADA | Payment declined | 200 (status=FAILED) | No | Notify customer |
| MADA | Gateway timeout | 504 | Yes (2x) | Return error to user |
| SADAD | File rejected | N/A (next-day) | Yes (regenerate) | Alert operations |
| SAMA | Report rejected | N/A (next-day) | Yes (fix + resend) | Alert compliance team |

---

## 7. Integration Testing Strategy

### 7.1 WireMock Stubs for Yakeen

```java
@TestConfiguration
public class YakeenWireMockConfig {

    @Bean
    public WireMockServer yakeenWireMock() {
        WireMockServer server = new WireMockServer(options().port(9090));
        
        // Successful verification
        server.stubFor(post(urlEqualTo("/yakeen/v1/verify-citizen"))
            .withRequestBody(matchingJsonPath("$.nationalId", equalTo("1098765432")))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                        "status": "VALID",
                        "citizenId": "1098765432",
                        "firstNameAr": "أحمد",
                        "lastNameAr": "الحربي",
                        "gender": "M",
                        "dateOfBirthG": "1990-05-15"
                    }
                """)));
        
        // Not found
        server.stubFor(post(urlEqualTo("/yakeen/v1/verify-citizen"))
            .withRequestBody(matchingJsonPath("$.nationalId", equalTo("0000000000")))
            .willReturn(aResponse().withStatus(404)));
        
        // Timeout simulation
        server.stubFor(post(urlEqualTo("/yakeen/v1/verify-citizen"))
            .withRequestBody(matchingJsonPath("$.nationalId", equalTo("1111111111")))
            .willReturn(aResponse()
                .withFixedDelay(10000) // 10 second delay
                .withStatus(200)));
        
        server.start();
        return server;
    }
}
```

### 7.2 Integration Test Example

```java
@SpringBootTest
@AutoConfigureMockMvc
@Import(YakeenWireMockConfig.class)
class YakeenIntegrationTest {

    @Autowired
    private IdentityVerificationPort identityVerification;

    @Test
    void shouldVerifyValidCitizen() {
        var request = new IdentityVerificationRequest(
            "1098765432", "1410-02-15", IdentityType.CITIZEN
        );
        
        var result = identityVerification.verify(request);
        
        assertThat(result.verified()).isTrue();
        assertThat(result.fullNameAr()).contains("أحمد");
        assertThat(result.status()).isEqualTo(VerificationStatus.VERIFIED);
    }

    @Test
    void shouldReturnNotFoundForInvalidNIN() {
        var request = new IdentityVerificationRequest(
            "0000000000", "1410-02-15", IdentityType.CITIZEN
        );
        
        var result = identityVerification.verify(request);
        
        assertThat(result.verified()).isFalse();
        assertThat(result.status()).isEqualTo(VerificationStatus.NOT_FOUND);
    }

    @Test
    void shouldFallbackOnTimeout() {
        var request = new IdentityVerificationRequest(
            "1111111111", "1410-02-15", IdentityType.CITIZEN
        );
        
        var result = identityVerification.verify(request);
        
        assertThat(result.verified()).isFalse();
        assertThat(result.status()).isEqualTo(VerificationStatus.DEFERRED);
    }
}
```

---

## Document Maintenance

| Aspect | Detail |
|--------|--------|
| Last Updated | 2026-07-06 |
| Owner | Enterprise Architecture Team |
| Review Cycle | Quarterly, or when integration contracts change |
| Related Documents | [Integration Architecture](integration-architecture.md), [API Architecture](api-architecture.md) |
