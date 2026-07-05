# Architecture Decisions

This document captures the key Architecture Decision Records (ADRs) for the Enterprise Insurance Platform.
Each ADR describes the context, decision, rationale, and consequences.

---

## ADR-001: Java 21 + Spring Boot 3.x as the Core Backend Stack

**Status:** Accepted  
**Date:** 2026-07

### Context
The platform requires a stable, enterprise-grade backend capable of supporting complex domain logic (policy lifecycle, claims, billing) with strong ecosystem support for persistence, security, messaging, and observability.

### Decision
Use Java 21 (LTS) with Spring Boot 3.3+ for all backend services.

### Rationale
- Java 21 LTS provides long-term support and virtual thread support (Project Loom) for high throughput
- Spring Boot 3.x provides production-ready auto-configuration for all required capabilities (JPA, Security, Kafka, Actuator)
- Strong ecosystem for insurance domain (DDD, event-driven patterns)
- Alignment with Saudi Arabia market preference for enterprise Java solutions
- Team familiarity and extensive community support

### Consequences
- ✅ Rich ecosystem and mature libraries
- ✅ Strong security with Spring Security OAuth2
- ✅ Native GraalVM compilation available for future cold-start optimisation
- ⚠️ Higher memory footprint than Go/Node.js (mitigated by JVM tuning)

---

## ADR-002: Angular + PrimeNG + TailwindCSS for the Frontend

**Status:** Accepted  
**Date:** 2026-07

### Context
The platform requires a sophisticated web UI that supports dual-language (Arabic/English), RTL/LTR layouts, complex dynamic forms, and a premium design aesthetic for both internal operations staff and customers.

### Decision
Use Angular 18+ with PrimeNG component library and TailwindCSS for styling.

### Rationale
- Angular's strong typing and DI framework suits enterprise-scale form-heavy applications
- PrimeNG provides RTL-ready, accessibility-compliant components out of the box (DataTable, Calendar, Dropdown, etc.)
- TailwindCSS enables rapid, consistent styling without CSS bloat
- Angular's reactive forms align naturally with the metadata-driven form rendering architecture
- PrimeNG's Arabic locale support satisfies Saudi market requirements

### Consequences
- ✅ Comprehensive RTL/LTR support with minimal custom CSS
- ✅ Rich UI components reduce build time for data-heavy screens
- ✅ Strong TypeScript typing reduces runtime errors
- ⚠️ Bundle size requires careful lazy-loading strategy

---

## ADR-003: Metadata-Driven Extensibility

**Status:** Accepted  
**Date:** 2026-07

### Context
Insurance products (motor, health, travel, property) differ significantly in their required fields, validation rules, and workflow steps. Hard-coding each product's fields creates a tight coupling that slows product configuration and requires developer involvement for every new product variant.

### Decision
Implement a metadata-driven field and form system where product fields, validation rules, and form sections are defined in database configuration tables (`metadata.field_definitions`, `metadata.product_configurations`) and rendered dynamically by the Angular UI engine.

### Rationale
- Business analysts can configure new products and fields without code deployments
- New insurance lines can be onboarded by adding metadata — not code
- Reduces time-to-market for product variations and endorsements
- Aligns with the platform's configuration-driven domain model principle

### Consequences
- ✅ Business team autonomy for product configuration
- ✅ Faster product launch cycles
- ✅ Reduced developer involvement in product-level changes
- ⚠️ Metadata schema must be versioned and migrated carefully
- ⚠️ Complex validation logic still requires developer involvement for non-standard rules

---

## ADR-004: JSONB for Dynamic Attribute Storage

**Status:** Accepted  
**Date:** 2026-07

### Context
Line-specific fields (e.g., motor vehicle details, Najm results, health declarations) vary by product and evolve over time. Two alternatives were considered: a generic Entity-Attribute-Value (EAV) model, or structured JSONB columns.

### Decision
Store dynamic and line-specific attributes as JSONB columns (`line_specific_data`, `dynamic_attributes`) on core entities (policies, claims).

### Rationale
- JSONB is natively indexed (GIN index) and queryable — avoiding full-table scans
- Simpler than EAV: one column per entity, not hundreds of EAV rows per policy
- PostgreSQL JSONB operators (`->`, `->>`, `@>`, `#>`) support rich queries
- Nested structures can represent complex motor/health data naturally
- Avoids premature normalisation for fields that may change or be dropped

### Consequences
- ✅ No schema migration required to add new optional fields
- ✅ GIN-indexed JSONB supports performant attribute queries
- ✅ Supports nested objects (Najm result, Yakeen response cache)
- ⚠️ Not strongly typed in the database — application must validate structure
- ⚠️ Reporting on JSONB fields requires JSONB operators or materialised views

---

## ADR-005: Schema Isolation for Line-Specific Data

**Status:** Accepted  
**Date:** 2026-07

### Context
As the platform grows to support multiple insurance lines (motor, health, travel), tables specific to each line should not pollute the shared core schema, which could create migration conflicts between teams.

### Decision
Line-specific tables are stored in dedicated PostgreSQL schemas (`motor`, `health`, `travel`). The `core` schema contains only shared kernel entities.

### Rationale
- Teams can evolve their line schema independently without risking core data integrity
- Flyway migrations are owned by the team responsible for each schema
- PostgreSQL schema-level permissions allow per-team database access control
- Clear architecture boundary: core is stable, lines are variable

### Consequences
- ✅ Independent delivery for each insurance line
- ✅ No cross-team migration conflicts
- ✅ Clear data ownership
- ⚠️ Cross-schema joins (e.g., `core.policies JOIN motor.motor_vehicles`) are permitted but should be limited to read-only reporting queries

---

## ADR-006: Event-Driven Integration for Workflow Coordination

**Status:** Accepted  
**Date:** 2026-07

### Context
Business workflows span multiple services (e.g., policy issuance triggers billing, which triggers a notification). Synchronous chaining of service calls creates tight coupling and reduces resilience.

### Decision
Use Apache Kafka for asynchronous domain event publishing. Services react to events (`PolicyIssued`, `ClaimRegistered`, `PaymentReceived`) instead of calling each other directly.

### Rationale
- Decouples services — downstream consumers don't affect the producer's availability
- Kafka provides durable, ordered, replayable event log
- Simplifies future integration of new consumers (analytics, audit, partner systems)
- Supports retry and dead-letter queue patterns for resilience

### Consequences
- ✅ Loose coupling between services
- ✅ Replayable event history for audit and debugging
- ✅ New consumers can be added without modifying producers
- ⚠️ Eventual consistency — downstream services may lag briefly
- ⚠️ Requires careful idempotency design in consumers

---

## ADR-007: Keycloak as the Identity Provider

**Status:** Accepted  
**Date:** 2026-07

### Context
The platform requires centralised authentication for browser users (Angular SPA), internal service accounts (client credentials), and future B2B partner access.

### Decision
Deploy Keycloak as the OIDC-compliant identity provider. Use Authorization Code + PKCE for browser clients and Client Credentials for service-to-service authentication.

### Rationale
- Open-source, enterprise-grade, battle-tested OIDC implementation
- Native support for Saudi locale, Arabic UI, and OTP SMS integrations
- Role-based and realm-level access control aligned with platform security model
- Future-ready for Absher (Saudi national SSO) federation

### Consequences
- ✅ Standards-compliant (OAuth 2.1, OpenID Connect, PKCE)
- ✅ Rich admin UI for user and role management
- ✅ Multi-realm and federation support for B2B scenarios
- ⚠️ Additional operational complexity — Keycloak must be HA-deployed
- ⚠️ Upgrade management required

---

## ADR-008: PostgreSQL 16 as the Primary Database

**Status:** Accepted  
**Date:** 2026-07

### Context
The platform requires ACID-compliant transactional storage for financial and insurance data, with strong JSONB support for dynamic attributes.

### Decision
Use PostgreSQL 16 as the primary relational database for all services.

### Rationale
- Best-in-class JSONB support with GIN indexing
- ACID compliance for financial data integrity
- Row-level security available for multi-tenant data isolation
- Mature, well-supported in Kubernetes environments
- Flyway migration tooling has first-class PostgreSQL support
- Open-source — no vendor lock-in

### Consequences
- ✅ ACID transactions for policy/financial operations
- ✅ JSONB with GIN indexes for metadata-driven attribute storage
- ✅ No licensing cost
- ⚠️ Horizontal write scaling requires sharding (not needed at initial scale)

---

## ADR-009: Flyway for Database Migration Management

**Status:** Accepted  
**Date:** 2026-07

### Context
Schema evolution must be version-controlled, auditable, and repeatable across all environments (dev, staging, production).

### Decision
Use Flyway 10.x for all database schema migrations, with Spring Boot auto-run on startup.

### Rationale
- Versioned SQL files with checksum validation prevent drift
- Native Spring Boot integration via `spring.flyway.*` configuration
- Supports schema-separated migration paths
- Simple, file-based approach with no additional tooling required

### Consequences
- ✅ Schema history tracked in `flyway_schema_history` table
- ✅ Migrations validated before application starts
- ✅ Dev/staging/production stay in sync
- ⚠️ Baseline migration required for existing schemas

---

## ADR-010: Resilience4j for Integration Resilience

**Status:** Accepted  
**Date:** 2026-07

### Context
External integrations (Yakeen, Najm, MADA payment) may be unavailable or slow. Without protection, a downstream failure could cascade and bring down the entire platform.

### Decision
Apply Resilience4j circuit breaker, retry, and time limiter patterns to all external integration calls.

### Rationale
- Native Spring Boot integration via `resilience4j-spring-boot3`
- Composable annotations: `@CircuitBreaker`, `@Retry`, `@TimeLimiter`
- Configurable per-integration — Yakeen vs. MADA have different timeout requirements
- Metrics exposed to Prometheus for circuit breaker state monitoring
- Fallback methods allow graceful degradation (e.g., defer Yakeen verification)

### Consequences
- ✅ Platform remains operational when external systems are down
- ✅ Fallback logic enables manual override paths
- ✅ Circuit breaker state visible in Grafana dashboards
- ⚠️ Fallback business logic must be explicitly designed and tested per integration
