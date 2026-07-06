# Architecture Overview

## Objective

The insurance platform is designed as a modular, document-friendly architecture that supports policy administration, claims processing, billing, and customer engagement for the Saudi Arabia market. The architecture is built for **extensibility** — new lines of business (Health, Travel, Malpractice) can be onboarded with minimal changes to the core platform.

## Architectural Principles

| Principle | Description |
|---|---|
| **Domain-driven design** | Clear business boundaries with domain ownership |
| **Loose coupling** | Services communicate via APIs and events, not shared databases |
| **Secure-by-default** | Zero-trust, OIDC authentication, RBAC, encryption at rest and in transit |
| **Observable operations** | Structured logs, metrics, distributed traces, and alerting |
| **Environment-based configuration** | Dev/staging/production isolation with ConfigMap and Secret injection |
| **Saudi regulatory alignment** | SAMA compliance, data residency, Hijri dates, Arabic-first UX |
| **Dynamic extensibility** | New fields, entities, and workflows introduced through configuration and UI-driven metadata |
| **Event-sourced metadata** | All configuration changes stored as immutable events for full audit trail and zero-downtime changes |
| **Data as a Product** | Each domain exposes data products with defined schemas, SLAs, and ownership |
| **Data Quality by Design** | Quality rules defined in metadata, enforced at write time, monitored continuously |

## High-Level View

The platform is organized around a set of core capabilities:

- **Policy lifecycle management** — quoting, binding, endorsements, renewals, cancellations
- **Claims processing and workflow** — registration, investigation, adjudication, payment
- **Billing and payment orchestration** — premium collection, refunds, commissions, reconciliation
- **Customer and agent experience** — self-service portal, agent dashboard, dynamic forms
- **Shared platform services** — identity, notification, document generation, audit
- **Metadata engine** — configuration-driven field definitions, form layouts, validation rules
- **Event store** — immutable event log for metadata changes and domain events
- **Reporting & analytics** — daily snapshots, KPI dashboards, SAMA regulatory reports

## Schema Architecture

The database is partitioned into domain-aligned schemas, each owned by a specific team:

| Schema | Owner | Purpose |
|---|---|---|
| `core` | Platform team | Shared kernel entities (customers, policies, claims, financial transactions) |
| `metadata` | Platform team | Configuration-driven field, product, and workflow definitions |
| `event_store` | Platform team | Immutable event log for metadata changes and domain events |
| `motor` | Motor line team | Motor-specific extension tables |
| `health` | Health line team (future) | Health-specific extension tables |
| `travel` | Travel line team (future) | Travel-specific extension tables |
| `reporting` | Data/Analytics team | Materialized views, reporting snapshots, KPI aggregations |
| `reference` | Platform team | Saudi-specific reference data (cities, codes, etc.) |

## Current Direction

The initial architecture focuses on a service-oriented model with clear domain ownership, event-based communication where appropriate, and API-first integration patterns tailored to Saudi Arabia operations and regulatory requirements. The platform is designed so business teams can extend data models, forms, and workflows dynamically from the UI without requiring a full development cycle for every change.

Key architectural decisions driving the current direction:

1. **Event-sourced metadata** — All configuration changes are stored as immutable events, enabling zero-downtime changes, full audit trail, and point-in-time reconstruction
2. **Hybrid persistence** — Core entities are strongly typed; line-specific fields use JSONB; dedicated tables for line-specific entities
3. **Pluggable extension points** — Rating engines, underwriting rules, and workflow definitions are registered per line of business via DI
4. **Multi-tenant by design** — Row-level security (RLS) for SaaS; schema-per-tenant for self-hosted deployments
5. **Data quality framework** — Rules defined in metadata, enforced at write time, monitored via scheduled jobs
6. **Data product catalog** — Each domain publishes data contracts with defined schemas, SLAs, and consumers

## Related Documents

- [Domain Architecture](domain-architecture.md) — bounded contexts, core domains, and domain responsibilities
- [Data Architecture](data-architecture.md) — persistence model, schemas, indexing, and migration approach
- [Integration Architecture](integration-architecture.md) — APIs, events, and external system connections
- [API Architecture](api-architecture.md) — API layers, gateway, communication patterns, and topology
- [UI Architecture](ui-architecture.md) — dynamic forms, portal structure, and configurable experience
- [Security Architecture](security-architecture.md) — authentication, authorization, data protection, and controls
- [Operations and Observability](operations-observability.md) — monitoring, alerts, and service health
- [Deployment Architecture](deployment-architecture.md) — environments, runtime topology, and CI/CD
- [Architecture Decisions](architecture-decisions.md) — key ADRs and design rationale
