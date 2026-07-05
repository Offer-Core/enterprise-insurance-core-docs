# Spring Boot Backend Development Standards

This document establishes the official coding standards, packages layout, architecture rules, and testing practices for all Spring Boot backend modules in the Enterprise Insurance Platform.

---

## 1. Modular Package Architecture

To maintain clear boundary isolations and support rapid onboarding of future lines of business, all backend source files must follow a **modular package layout** separated by vertical slices.

### Package Structures
```text
com.enterprise.insurance
  ├── core                      # Shared platform kernel
  │    ├── domain               # Core entities: Policy, Claim, Customer
  │    ├── repository           # Core repository interfaces
  │    ├── security             # Shared filter chains and security context
  │    └── exception            # Global exception classes
  └── lines                     # Lines of business (Vertical slices)
       └── motor                # Motor Insurance Vertical Slice
            ├── api             # Controller classes (REST endpoints)
            ├── application     # Services, rating engines, workflows
            ├── domain          # Motor entities: MotorVehicle, MotorDriver
            ├── infrastructure  # Adapters, external clients (Najm, Yakeen)
            └── repository      # DB persistence layer mapping
```

---

## 2. API Design & REST Guidelines

All Spring Boot controllers must follow the standard API conventions outlined in the [API Design Standards](../500-API/index.md).

### Controller Structure Specification
- Use `@RestController` and avoid returning raw map structures. Use strongly typed request/response Data Transfer Objects (DTOs).
- Always validate incoming payloads at the controller boundary using `@Valid` or `@Validated`.

#### Reference Implementation:
```java
package com.enterprise.insurance.lines.motor.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/v1/motor/policies")
public class MotorPolicyController {

    private final MotorPolicyService policyService;

    public MotorPolicyController(MotorPolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping
    public ResponseEntity<MotorPolicyResponse> createQuote(@Valid @RequestBody MotorQuoteRequest request) {
        MotorPolicyResponse response = policyService.createQuote(request);
        URI location = URI.create("/v1/motor/policies/" + response.getPolicyId());
        return ResponseEntity.created(location).body(response);
    }
}
```

---

## 3. Global Exception Handling & Error Envelope

To ensure the backend returns the standardized JSON error envelope defined in [API Error Handling](../500-API/index.md#standard-error-response-payload), a global `@RestControllerAdvice` handler must intercept all application boundaries.

### Exception Handler Implementation
```java
package com.enterprise.insurance.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Intercept Validation Failures (HTTP 400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorEnvelope> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        ErrorEnvelope.ErrorDetail error = new ErrorEnvelope.ErrorDetail(
                "VALIDATION_ERROR",
                "Request validation failed",
                details
        );
        return ResponseEntity.badRequest().body(new ErrorEnvelope(error));
    }

    // 2. Intercept Business Rules Failures (HTTP 409)
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorEnvelope> handleBusinessRule(BusinessRuleException ex) {
        ErrorEnvelope.ErrorDetail error = new ErrorEnvelope.ErrorDetail(
                ex.getErrorCode(),
                ex.getMessage(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorEnvelope(error));
    }
}
```

---

## 4. Logging & Observability Standards

End-to-end distributed tracing is mandatory for auditability and debugging across microservices.

### 4.1 Correlation ID Injection
Every incoming request must be mapped with a correlation or trace ID using a **Servlet Filter** and injected into the log **Mapped Diagnostic Context (MDC)**.

```java
package com.enterprise.insurance.core.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import java.io.IOException;
import java.util.UUID;

public class CorrelationIdFilter implements Filter {
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String MDC_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        
        MDC.put(MDC_KEY, correlationId);
        try {
            chain.doFilter(req, res);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
```

### 4.2 SLF4J Log Format
Configure `logback-spring.xml` to output MDC correlation values to the console and log aggregation server:
```xml
<pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [Trace:%X{correlationId}] - %msg%n</pattern>
```

---

## 5. Testing Guidelines & Slices

A strict testing structure is enforced to ensure functional correctness prior to CI/CD promotions.

1. **Unit Tests**:
   - Written for pure domain logic, rating factor engines, and business rules (e.g. `MotorRatingEngineTest`).
   - Mock all dependencies using Mockito.
2. **Controller Slice Tests (`@WebMvcTest`)**:
   - Test request-response mappings, status codes, validations, and serialization behaviors.
3. **Database Repository Slices (`@DataJpaTest`)**:
   - Verify JPA mapping constraints, custom queries, and database triggers.
   - Use Testcontainers (PostgreSQL container setup) to guarantee matches with production database behavior.

### Sample Controller Slice Test:
```java
package com.enterprise.insurance.lines.motor.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MotorPolicyController.class)
public class MotorPolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MotorPolicyService policyService;

    @Test
    public void givenInvalidPayload_whenCreateQuote_thenReturnsBadRequest() throws Exception {
        String invalidPayload = "{\"garagingAddress\": null}"; // Missing required fields

        mockMvc.perform(post("/v1/motor/policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }
}
```
