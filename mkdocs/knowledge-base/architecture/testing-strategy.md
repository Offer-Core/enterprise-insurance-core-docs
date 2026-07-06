# Testing Strategy — Integration, Performance & Security

## Overview

This document defines the comprehensive testing strategy for the Enterprise Insurance Platform. It covers integration testing, performance/load testing, security testing, and the test automation framework.

---

## 1. Test Pyramid

```
         ╱╲
        ╱  ╲
       ╱ E2E╲           ← 5%  — Critical business flows (Cypress, Playwright)
      ╱──────╲
     ╱Integration╲       ← 20% — Service-to-service, DB, external APIs (Spring Boot Test, WireMock)
    ╱────────────╲
   ╱  Component   ╲      ← 25% — Slice tests (Web MVC, Data JPA, Kafka)
  ╱────────────────╲
 ╱    Unit Tests     ╲    ← 50% — Business logic, domain rules (JUnit 5, Mockito)
╱──────────────────────╲
```

---

## 2. Unit Testing

### 2.1 Standards

| Aspect | Standard |
|--------|----------|
| Framework | JUnit 5 + Mockito |
| Coverage Target | ≥ 85% line coverage, ≥ 70% branch coverage |
| Naming Convention | `{methodName}_should_{expectedBehavior}_when_{condition}` |
| Assertion Library | AssertJ (fluent assertions) |
| Test Data | Builder pattern for domain objects |

### 2.2 Example: Premium Calculation

```java
class PremiumCalculationServiceTest {

    private final PremiumCalculationService service = new PremiumCalculationService();

    @Test
    void calculatePremium_should_applyNoClaimsDiscount_when_driverHasNCD() {
        // Given
        var vehicle = MotorVehicle.builder()
            .vehicleValue(new BigDecimal("100000"))
            .vehicleUse("PRIVATE")
            .build();
        var driver = MotorDriver.builder()
            .noClaimsDiscount(new BigDecimal("0.30"))
            .yearsOfExperience(10)
            .violationsCount(0)
            .build();

        // When
        var premium = service.calculate(vehicle, driver);

        // Then
        assertThat(premium.basePremium()).isGreaterThan(BigDecimal.ZERO);
        assertThat(premium.discountApplied()).isTrue();
        assertThat(premium.finalPremium())
            .isLessThan(premium.basePremium());
    }

    @Test
    void calculatePremium_should_applyLoadingFactor_when_driverHasViolations() {
        // Given
        var vehicle = MotorVehicle.builder()
            .vehicleValue(new BigDecimal("100000"))
            .vehicleUse("PRIVATE")
            .build();
        var driver = MotorDriver.builder()
            .noClaimsDiscount(BigDecimal.ZERO)
            .yearsOfExperience(3)
            .violationsCount(3)
            .build();

        // When
        var premium = service.calculate(vehicle, driver);

        // Then
        assertThat(premium.loadingApplied()).isTrue();
        assertThat(premium.finalPremium())
            .isGreaterThan(premium.basePremium());
    }

    @Test
    void calculatePremium_should_throwException_when_vehicleValueIsZero() {
        // Given
        var vehicle = MotorVehicle.builder().vehicleValue(BigDecimal.ZERO).build();
        var driver = MotorDriver.builder().build();

        // When / Then
        assertThatThrownBy(() -> service.calculate(vehicle, driver))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Vehicle value must be greater than zero");
    }
}
```

---

## 3. Integration Testing

### 3.1 Database Integration Tests

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class PolicyRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void shouldFindActivePoliciesByCustomer() {
        // Given
        var customer = createCustomer();
        var policy = Policy.builder()
            .policyNumber("POL-TEST-001")
            .customer(customer)
            .status("ACTIVE")
            .effectiveDate(LocalDate.now())
            .expiryDate(LocalDate.now().plusYears(1))
            .annualPremium(new BigDecimal("2500"))
            .build();
        policyRepository.save(policy);

        // When
        var activePolicies = policyRepository
            .findByCustomerIdAndStatus(customer.getId(), "ACTIVE");

        // Then
        assertThat(activePolicies).hasSize(1);
        assertThat(activePolicies.get(0).getPolicyNumber()).isEqualTo("POL-TEST-001");
    }
}
```

### 3.2 Kafka Integration Tests

```java
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"insurance.policy.events"})
class PolicyEventPublisherIntegrationTest {

    @Autowired
    private KafkaTemplate<String, DomainEvent> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Test
    void shouldPublishAndConsumePolicyIssuedEvent() throws Exception {
        // Given
        var event = DomainEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType("PolicyIssued")
            .sourceService("policy-service")
            .data(Map.of("policyNumber", "POL-TEST-001"))
            .build();

        // When
        kafkaTemplate.send("insurance.policy.events", event.getSourceEntityId(), event);

        // Then
        var consumer = new KafkaConsumer<String, String>(
            Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-group",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
            )
        );
        consumer.subscribe(List.of("insurance.policy.events"));
        
        var records = consumer.poll(Duration.ofSeconds(5));
        assertThat(records).hasSize(1);
        
        var received = records.iterator().next();
        assertThat(received.value()).contains("POL-TEST-001");
    }
}
```

### 3.3 External API Integration Tests (WireMock)

```java
@SpringBootTest
@WireMockTest(httpPort = 9090)
class YakeenIntegrationTest {

    @Autowired
    private YakeenIdentityAdapter yakeenAdapter;

    @Test
    void shouldVerifyCitizenSuccessfully() {
        // Given
        stubFor(post(urlEqualTo("/yakeen/v1/verify-citizen"))
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

        // When
        var result = yakeenAdapter.verify(
            new IdentityVerificationRequest("1098765432", "1410-02-15", IdentityType.CITIZEN)
        );

        // Then
        assertThat(result.verified()).isTrue();
        assertThat(result.fullNameAr()).contains("أحمد");
    }

    @Test
    void shouldHandleYakeenNotFound() {
        // Given
        stubFor(post(urlEqualTo("/yakeen/v1/verify-citizen"))
            .willReturn(aResponse().withStatus(404)));

        // When
        var result = yakeenAdapter.verify(
            new IdentityVerificationRequest("0000000000", "1410-02-15", IdentityType.CITIZEN)
        );

        // Then
        assertThat(result.verified()).isFalse();
        assertThat(result.status()).isEqualTo(VerificationStatus.NOT_FOUND);
    }
}
```

---

## 4. Performance Testing

### 4.1 Load Test Scenarios (Gatling)

```scala
// PolicyIssuanceSimulation.scala
class PolicyIssuanceSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("https://api.insurance.sa")
    .header("Content-Type", "application/json")
    .header("Authorization", "Bearer ${jwtToken}")
    .header("X-Tenant-ID", "tenant-001")

  val policyFeeder = csv("data/policies.csv").circular

  val scn = scenario("Policy Issuance Flow")
    .feed(policyFeeder)
    .exec(
      http("Step 1: Verify Identity (Yakeen)")
        .post("/api/v1/policies/verify-identity")
        .body(StringBody("""{ "nationalId": "${nationalId}", "dateOfBirth": "${dob}" }"""))
        .check(status.is(200))
        .check(jsonPath("$.verified").is("true"))
    )
    .pause(1)
    .exec(
      http("Step 2: Get Najm History")
        .post("/api/v1/policies/najm-history")
        .body(StringBody("""{ "sequenceNumber": "${sequenceNumber}" }"""))
        .check(status.is(200))
    )
    .pause(1)
    .exec(
      http("Step 3: Calculate Premium")
        .post("/api/v1/policies/calculate-premium")
        .body(StringBody("""{ "vehicleValue": ${vehicleValue}, "ncd": ${ncd} }"""))
        .check(status.is(200))
        .check(jsonPath("$.finalPremium").saveAs("premium"))
    )
    .pause(1)
    .exec(
      http("Step 4: Issue Policy")
        .post("/api/v1/policies")
        .body(StringBody("""{ "productCode": "MOTOR_COMP", "premium": ${premium} }"""))
        .check(status.is(201))
        .check(jsonPath("$.policyNumber").saveAs("policyNumber"))
    )

  setUp(
    scn.inject(
      nothingFor(10 seconds),
      rampUsers(50).during(60 seconds),
      constantUsersPerSec(20).during(300 seconds),
      rampUsers(0).during(60 seconds)
    )
  ).protocols(httpProtocol)
}
```

### 4.2 Performance Test Scenarios

| Scenario | Target | Load Pattern | Success Criteria |
|----------|--------|-------------|------------------|
| Policy Issuance | 20 req/s | Ramp to 50 users, sustain 20 req/s for 5 min | p95 < 3s, error rate < 1% |
| Premium Calculation | 50 req/s | Ramp to 100 users, sustain 50 req/s for 5 min | p95 < 1s, error rate < 0.5% |
| Claim Registration | 10 req/s | Ramp to 30 users, sustain 10 req/s for 5 min | p95 < 4s, error rate < 1% |
| Customer Lookup | 100 req/s | Ramp to 200 users, sustain 100 req/s for 5 min | p95 < 500ms, error rate < 0.1% |
| Yakeen Integration | 5 req/s | Ramp to 15 users, sustain 5 req/s for 5 min | p95 < 2s, error rate < 2% |

### 4.3 Stress Test Scenarios

| Scenario | Load Pattern | Success Criteria |
|----------|-------------|------------------|
| Spike Test | 0 → 100 req/s in 10 seconds | No crash, auto-scale within 2 min |
| Soak Test | 20 req/s for 8 hours | No memory leak, no degradation |
| Burst Test | 50 req/s for 30 seconds, idle 60s, repeat | Recovery within 30 seconds |

---

## 5. Security Testing

### 5.1 Security Test Categories

| Category | Tools | Frequency |
|----------|-------|-----------|
| SAST (Static Analysis) | SonarQube, SpotBugs | Every commit |
| DAST (Dynamic Analysis) | OWASP ZAP | Weekly |
| Dependency Scanning | OWASP Dependency-Check, Trivy | Every build |
| Container Scanning | Trivy, Grype | Every build |
| Secret Scanning | GitLeaks, TruffleHog | Every commit |
| Penetration Testing | External vendor | Quarterly |

### 5.2 OWASP Top 10 Coverage

| Risk | Mitigation | Test |
|------|------------|------|
| A01: Broken Access Control | RBAC + RLS + JWT validation | Integration tests for each role |
| A02: Cryptographic Failures | AES-256-GCM for PII, TLS 1.3 | Encryption/decryption unit tests |
| A03: Injection | Parameterized queries, input validation | SQL injection scan (ZAP) |
| A04: Insecure Design | Threat modeling, security review | Architecture review |
| A05: Security Misconfiguration | CIS benchmarks, hardened images | Trivy config scan |
| A06: Vulnerable Components | Dependency scanning, SBOM | OWASP Dependency-Check |
| A07: Auth Failures | Keycloak, MFA, session management | Auth integration tests |
| A08: Data Integrity Failures | Event signing, audit log | Event signature verification |
| A09: Logging Failures | Structured logging, SIEM integration | Log review |
| A10: SSRF | Network policies, URL allowlist | Network policy tests |

### 5.3 Security Test Example

```java
@SpringBootTest
@AutoConfigureMockMvc
class PolicyApiSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRejectRequestWithoutJwt() throws Exception {
        mockMvc.perform(post("/api/v1/policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectRequestWithExpiredJwt() throws Exception {
        var expiredToken = generateExpiredToken();

        mockMvc.perform(post("/api/v1/policies")
                .header("Authorization", "Bearer " + expiredToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectRequestWithWrongRole() throws Exception {
        var token = generateTokenWithRole("CLAIMS_ADJUSTER");

        mockMvc.perform(post("/api/v1/policies")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "productCode": "MOTOR_COMP",
                        "annualPremium": 2500.00
                    }
                """))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowRequestWithCorrectRole() throws Exception {
        var token = generateTokenWithRole("POLICY_UNDERWRITER");

        mockMvc.perform(post("/api/v1/policies")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "productCode": "MOTOR_COMP",
                        "annualPremium": 2500.00
                    }
                """))
            .andExpect(status().isCreated());
    }
}
```

---

## 6. Test Automation Framework

### 6.1 Test Configuration

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:tc:postgresql:16-alpine:///testdb
    driver-class-name: org.testcontainers.jdbc.ContainerDatabaseDriver
  kafka:
    bootstrap-servers: ${spring.embedded.kafka.brokers}
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false

app:
  yakeen:
    base-url: http://localhost:9090
  najm:
    wsdl-url: http://localhost:9091/najmws
  payment:
    gateway-url: http://localhost:9092
```

### 6.2 Test Dependencies

```xml
<!-- pom.xml test dependencies -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>kafka</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-contract-stub-runner</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.wiremock</groupId>
    <artifactId>wiremock-standalone</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.gatling.highcharts</groupId>
    <artifactId>gatling-charts-highcharts</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 7. Test Data Management

### 7.1 Test Data Builders

```java
public class TestDataBuilders {

    public static Customer.CustomerBuilder aCustomer() {
        return Customer.builder()
            .customerNumber("CUST-TEST-" + UUID.randomUUID().toString().substring(0, 8))
            .nationalIdEncrypted("encrypted_nin")
            .identityType("CITIZEN")
            .fullNameAr("عميل اختبار")
            .fullNameEn("Test Customer")
            .mobileNumber("+966501234567")
            .tenantId("tenant-001");
    }

    public static Policy.PolicyBuilder aMotorPolicy() {
        return Policy.builder()
            .policyNumber("POL-TEST-" + UUID.randomUUID().toString().substring(0, 8))
            .productCode("MOTOR_COMP")
            .lineOfBusiness("MOTOR")
            .status("ACTIVE")
            .effectiveDate(LocalDate.now())
            .expiryDate(LocalDate.now().plusYears(1))
            .annualPremium(new BigDecimal("2500"))
            .currency("SAR")
            .tenantId("tenant-001");
    }

    public static Claim.ClaimBuilder aMotorClaim() {
        return Claim.builder()
            .claimNumber("CLM-TEST-" + UUID.randomUUID().toString().substring(0, 8))
            .status("REGISTERED")
            .incidentDate(LocalDate.now().minusDays(1))
            .claimedAmount(new BigDecimal("15000"))
            .currency("SAR")
            .tenantId("tenant-001");
    }
}
```

---

## 8. CI/CD Test Gates

