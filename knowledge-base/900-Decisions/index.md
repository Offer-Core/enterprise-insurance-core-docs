# Architecture Decision Records (ADRs)

This section contains all Architecture Decision Records (ADRs) for the Enterprise Insurance Platform.
ADRs document significant technical decisions, their context, rationale, and consequences to serve as a permanent reference for the team.

---

## How to Read ADRs

Each ADR follows this structure:

| Field | Description |
|---|---|
| **Status** | `Proposed` → `Accepted` → `Deprecated` / `Superseded by ADR-XXX` |
| **Date** | When the decision was made |
| **Context** | The problem or situation that required a decision |
| **Decision** | The chosen approach |
| **Rationale** | Why this option was selected over alternatives |
| **Consequences** | ✅ Benefits and ⚠️ trade-offs |

---

## Decision Summary

| ADR | Title | Status |
|---|---|---|
| [ADR-001](../architecture/architecture-decisions.md) | Java 21 + Spring Boot 3.x as Core Backend Stack | ✅ Accepted |
| [ADR-002](../architecture/architecture-decisions.md) | Angular + PrimeNG + TailwindCSS for Frontend | ✅ Accepted |
| [ADR-003](../architecture/architecture-decisions.md) | Metadata-Driven Extensibility | ✅ Accepted |
| [ADR-004](../architecture/architecture-decisions.md) | JSONB for Dynamic Attribute Storage | ✅ Accepted |
| [ADR-005](../architecture/architecture-decisions.md) | Schema Isolation for Line-Specific Data | ✅ Accepted |
| [ADR-006](../architecture/architecture-decisions.md) | Event-Driven Integration via Kafka | ✅ Accepted |
| [ADR-007](../architecture/architecture-decisions.md) | Keycloak as Identity Provider | ✅ Accepted |
| [ADR-008](../architecture/architecture-decisions.md) | PostgreSQL 16 as Primary Database | ✅ Accepted |
| [ADR-009](../architecture/architecture-decisions.md) | Flyway for Database Migration Management | ✅ Accepted |
| [ADR-010](../architecture/architecture-decisions.md) | Resilience4j for Integration Resilience | ✅ Accepted |

---

## Technology Stack Summary

The following stack decisions are the result of the ADRs above:

### Backend

| Concern | Technology | ADR |
|---|---|---|
| Language | Java 21 (LTS) | ADR-001 |
| Framework | Spring Boot 3.3+ | ADR-001 |
| Database | PostgreSQL 16 | ADR-008 |
| DB Migration | Flyway 10.x | ADR-009 |
| ORM | Spring Data JPA (Hibernate 6) | ADR-001 |
| Messaging | Apache Kafka | ADR-006 |
| Cache | Redis 7 | - |
| Security | Spring Security + Keycloak | ADR-007 |
| Resilience | Resilience4j | ADR-010 |
| Observability | Micrometer + Prometheus + Loki + Tempo | - |
| Build | Maven 3.9+ | - |
| Containerisation | Docker + Kubernetes | - |

### Frontend

| Concern | Technology | ADR |
|---|---|---|
| Framework | Angular 18+ | ADR-002 |
| UI Components | PrimeNG 17+ | ADR-002 |
| Styling | TailwindCSS 3.x | ADR-002 |
| State Management | Angular Signals / RxJS | - |
| Internationalisation | ngx-translate | - |
| HTTP Client | Angular HttpClient + interceptors | - |
| Forms | Angular Reactive Forms | ADR-003 |
| Build | Angular CLI + Vite | - |

---

## How to Add a New ADR

1. Use the next available ADR number
2. Add the decision to [architecture-decisions.md](../architecture/architecture-decisions.md)
3. Add a row to the Decision Summary table above
4. Status must start as `Proposed` — move to `Accepted` after team review
5. If superseding an existing ADR, mark the old one as `Superseded by ADR-XXX`

---

## Related Documents

- [Architecture Decisions (Full)](../architecture/architecture-decisions.md) — complete ADR text
- [Architecture Overview](../architecture/overview.md) — high-level platform architecture
- [Database Design](../600-Database/index.md) — schema details driven by ADR-004/005/008
- [Security Design](../200-Domains/Authentication/README.md) — security decisions from ADR-007
- [Integration Design](../800-Integrations/index.md) — integration patterns from ADR-006/010
