# Architecture Decisions (ADRs)

This document records key architectural decisions and the rationale behind them.

---

## ADR-001: Event-Sourced Metadata for Configuration Changes

**Status:** Accepted  
**Date:** 2026-07-06  
**Domain:** Data Architecture, Metadata Engine

### Context

The platform needs to support dynamic configuration changes (product definitions, field definitions, form layouts, workflow definitions) without downtime. Changes must be fully auditable, reversible, and traceable.

### Decision

All metadata configuration changes will be stored as **immutable events** in an event store (`event_store.metadata_events`). The current state is materialized via snapshots (`event_store.snapshot_store`). This follows the event sourcing pattern applied specifically to the metadata/configuration domain.

### Consequences

**Positive:**
- Full audit trail of every configuration change (who, what, when)
- Point-in-time reconstruction of configuration state
- Zero-downtime configuration changes (no direct table mutations)
- Event-driven cache invalidation for downstream consumers
- Natural versioning of configuration

**Negative:**
- Additional infrastructure (event store tables, snapshot management)
- Eventual consistency between event write and snapshot materialization
- Requires snapshot compaction strategy for long-running aggregates

### Alternatives Considered

| Alternative | Reason for Rejection |
|---|---|
| Direct CRUD on metadata tables | No audit trail, no versioning, no point-in-time recovery |
| Database triggers + audit table | Tight coupling, hard to reconstruct state at a point in time |
| Outbox pattern with Kafka | Over-engineered for metadata; event store in PostgreSQL is sufficient |

---

## ADR-002: Hybrid Persistence — Strongly Typed Core + JSONB Extensions

**Status:** Accepted  
**Date:** 2026-07-06  
**Domain:** Data Architecture

### Context

The platform must support multiple lines of business (Motor, Health, Travel) with different data shapes while maintaining a stable core schema. New lines should be onboarded without schema changes to core tables.

### Decision

Use a **hybrid persistence model**:
1. **Core entities** (customer, policy, claim, financial transaction) are strongly typed with dedicated columns
2. **Line-specific data** is stored in `JSONB` columns (`line_specific_data`, `dynamic_attributes`) on core tables
3. **Line-specific entities** (vehicles, drivers, health members) use dedicated tables in line-owned schemas (`motor.*`, `health.*`)
4. **Field definitions** are stored in `metadata.field_definitions` and drive UI rendering and validation

### Consequences

**Positive:**
- Core schema remains stable across lines of business
- JSONB supports nested structures naturally (no EAV complexity)
- GIN indexes enable efficient querying of JSONB data
- Line teams own their schema migrations independently
- UI-driven field additions without code changes

**Negative:**
- JSONB lacks native referential integrity (enforced at application layer)
- Querying JSONB is less performant than typed columns for high-volume queries
- Requires discipline to avoid "JSONB dump" anti-pattern

### Alternatives Considered

| Alternative | Reason for Rejection |
|---|---|
| Entity-Attribute-Value (EAV) | Complex queries, poor performance, no nested structure support |
| Fully dynamic (all JSONB) | Loses type safety, no FK constraints, harder to maintain |
| Schema-per-line-of-business | Duplication of core entities, cross-line reporting complexity |

---

## ADR-003: Multi-Tenant Isolation via Row-Level Security

**Status:** Accepted  
**Date:** 2026-07-06  
**Domain:** Data Architecture, Security

### Context

The platform serves multiple insurance companies (tenants) in the Saudi market. Data isolation is a regulatory requirement. The architecture must support both SaaS (multi-tenant) and self-hosted (single-tenant) deployment models.

### Decision

Use **Row-Level Security (RLS)** on PostgreSQL as the default isolation mechanism for SaaS deployments. Each table includes a `tenant_id` column, and RLS policies filter rows based on the current session's tenant context.

For self-hosted deployments, use **schema-per-tenant** or **database-per-tenant** isolation.

### Consequences

**Positive:**
- Single database instance, lower operational overhead
- RLS policies are enforced at the database level (cannot be bypassed by application)
- Tenant context is set at connection time, not in application code
- Schema-per-tenant option available for clients with strict isolation requirements

**Negative:**
- RLS adds query overhead (policy evaluation per row)
- All queries must include `tenant_id` or rely on RLS (can mask bugs)
- Connection pooling must set tenant context per request

### Alternatives Considered

| Alternative | Reason for Rejection |
|---|---|
| Application-level filtering | Risk of data leakage if filter is forgotten; not defense-in-depth |
| Database-per-tenant | Higher operational cost, suitable only for enterprise self-hosted |
| Schema-per-tenant | Good for self-hosted, but adds migration complexity for SaaS |

---

## ADR-004: Data Quality Framework with Metadata-Driven Rules

**Status:** Accepted  
**Date:** 2026-07-06  
**Domain:** Data Architecture, Operations

### Context

Regulatory compliance (SAMA) requires data accuracy, completeness, and timeliness. The platform needs a systematic approach to data quality that is configurable, measurable, and auditable.

### Decision

Implement a **metadata-driven data quality framework**:
1. Quality rules are defined in `metadata.data_quality_rules` with type, severity, and logic
2. Rules are enforced at multiple levels: schema (DDL constraints), application (Bean Validation), and scheduled (cron jobs)
3. Quality metrics are exposed via Prometheus and visualized in Grafana
4. Failures trigger alerts via AlertManager

### Consequences

**Positive:**
- Quality rules are configurable without code changes
- Consistent enforcement across all lines of business
- Measurable quality metrics for regulatory reporting
- Early detection of data issues

**Negative:**
- Additional infrastructure for scheduled quality checks
- Rule definition requires domain expertise
- False positives can cause alert fatigue

---

## ADR-005: Data Product Catalog with Versioned Contracts

**Status:** Accepted  
**Date:** 2026-07-06  
**Domain:** Data Architecture, Integration

### Context

Multiple services and teams consume data from different domains. Without clear contracts, schema changes can break downstream consumers. The platform needs a mechanism to document, version, and communicate data schemas.

### Decision

Each domain publishes **data contracts** in `core.data_contracts` with:
- Schema definition (JSON Schema)
- SLA (availability, latency, freshness)
- Data quality expectations
- Known consumers

Contracts are versioned using semantic versioning. Breaking changes require a new contract version and consumer notification.

### Consequences

**Positive:**
- Clear ownership and expectations for data products
- Breaking changes are explicitly managed
- Consumers can discover available data products
- Supports data mesh / data-as-a-product paradigm

**Negative:**
- Requires discipline to keep contracts up to date
- Initial overhead to define and publish contracts
- Contract enforcement requires tooling (schema registry)

---

## ADR-006: Reporting Schema with Materialized Views

**Status:** Accepted  
**Date:** 2026-07-06  
**Domain:** Data Architecture, Operations

### Context

Business dashboards and SAMA regulatory reports require aggregated data that is expensive to compute from transactional tables in real-time. The platform needs a dedicated reporting layer.

### Decision

Create a `reporting` schema with:
1. **Daily snapshot tables** for policy, claims, and financial aggregates
2. **Materialized views** for KPI dashboards (refreshed periodically)
3. **SAMA reporting table** for regulatory report generation and submission tracking

### Consequences

**Positive:**
- Reporting queries don't impact transactional performance
- Materialized views provide fast dashboard loading
- SAMA reports have dedicated tracking (status, submission, acknowledgment)
- Historical snapshots enable trend analysis

**Negative:**
- Data freshness is bounded by refresh schedule (daily for snapshots)
- Additional storage for snapshot tables
- Snapshot refresh jobs need monitoring

---

## ADR-007: Saudi-Specific Reference Data Management

**Status:** Accepted  
**Date:** 2026-07-06  
**Domain:** Data Architecture

### Context

The platform needs to manage Saudi-specific reference data: administrative regions, SAMA codes, occupation classifications, vehicle plate types, etc. This data is used across multiple domains and must be centrally managed.

### Decision

Create a `metadata.reference_data` table for all Saudi-specific reference data. Each reference type (e.g., `SAUDI_CITY`, `SAMA_CODE`, `OCCUPATION`, `PLATE_TYPE`) has its own namespace with codes, Arabic/English labels, and hierarchical relationships.

### Consequences

**Positive:**
- Single source of truth for reference data
- Arabic/English bilingual support
- Hierarchical relationships (parent-child) for region/city structures
- Easy to extend with new reference types

**Negative:**
- Requires data seeding and maintenance
- Reference data changes need coordination across domains

---

## ADR-008: Data Retention and Archival Strategy

**Status:** Accepted  
**Date:** 2026-07-06  
**Domain:** Data Architecture, Operations

### Context

SAMA regulations require specific data retention periods. The platform must manage data lifecycle — from active storage through archival to purging — while maintaining regulatory compliance.

### Decision

Implement a **tiered data retention strategy**:

| Tier | Storage | Access Pattern | Retention |
|---|---|---|---|
| Hot (Active) | PostgreSQL | Real-time queries | Current + 1 year |
| Warm (Archive) | S3-compatible cold storage | Occasional, with restore | Up to retention limit |
| Cold (Purge) | Deleted | N/A | After retention period |

Retention schedules are defined per data category in the data governance policy. PII is anonymized before archival.

### Consequences

**Positive:**
- Regulatory compliance with SAMA retention requirements
- Cost optimization (hot storage is expensive)
- Clear data lifecycle management

**Negative:**
- Archival and purge jobs need monitoring
- Restore from warm storage has latency
- Anonymization logic must be verified

---

## ADR-009: Data Classification and Sensitivity Tagging

**Status:** Accepted  
**Date:** 2026-07-06  
**Domain:** Data Architecture, Security

### Context

The platform handles sensitive data (NIN, IBAN, health information) that requires different levels of protection. SAMA regulations require data classification and appropriate controls per classification level.

### Decision

Implement a **data classification model**:
1. Classification levels defined in `metadata.data_classification` (PUBLIC, INTERNAL, CONFIDENTIAL, PII, RESTRICTED)
2. Each field in `metadata.field_definitions` has a `sensitivity` attribute
3. PII fields are encrypted at the application level (AES-256-GCM)
4. Access control is enforced per classification level via RBAC

### Consequences

**Positive:**
- Clear data handling requirements per classification level
- Encryption applied consistently to all PII fields
- Audit trail for access to sensitive data
- Regulatory compliance with SAMA data classification requirements

**Negative:**
- Classification must be maintained as new fields are added
- Application-level encryption adds complexity for search and reporting
- Encryption key management requires Vault or similar tool

---

## ADR-010: Schema Ownership and Migration Isolation

**Status:** Accepted  
**Date:** 2026-07-06  
**Domain:** Data Architecture, DevOps

### Context

Multiple teams (Platform, Motor, Health, Data/Analytics) need to evolve the database schema independently. Cross-schema migrations create coupling and deployment conflicts.

### Decision

Each schema is owned by a single team. Migrations are organized by schema:
- `db/migration/core/` — Platform team
- `db/migration/metadata/` — Platform team
- `db/migration/event_store/` — Platform team
- `db/migration/motor/` — Motor line team
- `db/migration/reporting/` — Data/Analytics team

**Rule:** No migration script may modify tables outside its owning schema. Cross-schema changes require coordination and a joint migration.

### Consequences

**Positive:**
- Teams can evolve their schema independently
- No deployment conflicts between teams
- Clear ownership and accountability
- Faster iteration for line teams

**Negative:**
- Cross-schema changes require coordination overhead
- Schema-level foreign keys across schemas are allowed but need careful management
- Requires CI/CD pipeline to run migrations per schema

---

## ADR Index

| ADR | Title | Status |
|---|---|---|
| ADR-001 | Event-Sourced Metadata for Configuration Changes | Accepted |
| ADR-002 | Hybrid Persistence — Strongly Typed Core + JSONB Extensions | Accepted |
| ADR-003 | Multi-Tenant Isolation via Row-Level Security | Accepted |
| ADR-004 | Data Quality Framework with Metadata-Driven Rules | Accepted |
| ADR-005 | Data Product Catalog with Versioned Contracts | Accepted |
| ADR-006 | Reporting Schema with Materialized Views | Accepted |
| ADR-007 | Saudi-Specific Reference Data Management | Accepted |
| ADR-008 | Data Retention and Archival Strategy | Accepted |
| ADR-009 | Data Classification and Sensitivity Tagging | Accepted |
| ADR-010 | Schema Ownership and Migration Isolation | Accepted |
