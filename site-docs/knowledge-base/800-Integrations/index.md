# Saudi Arabia National Integration Specifications

This document outlines the API contracts, integration patterns, and security constraints for connecting with Saudi national systems and financial transaction entities.

For system-level integration architecture and adapter patterns, see the [Integration Architecture](../architecture/integration-architecture.md).

---

## 1. Yakeen ID Integration (Identity Verification)

The **ELM Yakeen** integration validates customer identity (National ID or Iqama) and fetches demographic data.

### API Contract Details
- **Protocol:** REST over HTTPS (Mutual TLS required).
- **Endpoint:** `POST /yakeen/v1/verify-citizen`
- **Request Schema:**
  ```json
  {
    "nationalId": "1098765432",
    "dateOfBirthG": "1990-05-15"
  }
  ```
- **Response Schema:**
  ```json
  {
    "status": "VALID",
    "firstNameAr": "أحمد",
    "lastNameAr": "الحربي",
    "firstNameEn": "Ahmed",
    "lastNameEn": "Al-Harbi",
    "gender": "M",
    "occupation": "Engineer"
  }
  ```

---

## 2. Najm Integration (Motor Accident History)

The **Najm** integration retrieves claims and accident history for rating calculations.

### API Contract Details
- **Protocol:** SOAP/XML or REST (SOAP WSDL used for MVP).
- **Endpoint:** `POST /najm/v2/accident-history`
- **Request Payload:**
  ```xml
  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:naj="http://najm.sa/api">
     <soapenv:Header/>
     <soapenv:Body>
        <naj:GetAccidentHistoryRequest>
           <naj:NationalId>1098765432</naj:NationalId>
           <naj:SequenceNumber>123456789</naj:SequenceNumber>
        </naj:GetAccidentHistoryRequest>
     </soapenv:Body>
  </soapenv:Envelope>
  ```
- **Response Data:** Returns number of historical accidents, fault percentages, and claim status records used as multipliers in the [Motor Rating Engine](../architecture/domain-architecture.md#24-motor-rating-engine).

---

## 3. Local Payment Gateways (Mada / SADAD)

The billing and payment module orchestrates transactions through licensed payment aggregators.

### Flow Architecture
1. **Initiate Payment**: Frontend requests payment url from Spring Boot backend.
2. **Redirection**: User completes authentication on the secure Mada/SADAD gateway.
3. **Webhook Callback**: The gateway sends a signed POST webhook containing status:
   ```json
   {
     "transactionId": "tx_90812739128",
     "amount": 1050.00,
     "currency": "SAR",
     "status": "CAPTURED",
     "signature": "sha256_hash_here"
   }
   ```
4. **Settlement**: Backend validates the signature, posts to [core.financial_transaction](../600-Database/index.md#corefinancial_transaction), and transitions policy status to `ACTIVE`.

---

## 4. Resilience & Circuit Breakers (Resilience4j)

All national API integrations must implement circuit breakers to prevent cascading failures using **Resilience4j**.

```yaml
resilience4j.circuitbreaker:
  instances:
    yakeenService:
      slidingWindowSize: 20
      failureRateThreshold: 50
      waitDurationInOpenState: 15s
      permittedNumberOfCallsInHalfOpenState: 5
```
