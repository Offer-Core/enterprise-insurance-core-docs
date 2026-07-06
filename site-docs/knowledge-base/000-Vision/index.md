# Vision & Strategy

## Mission

Build a **modern, configuration-driven enterprise insurance platform** purpose-built for the Saudi Arabia market — enabling insurers to launch products faster, serve customers digitally, and comply with SAMA regulatory requirements without the cost and rigidity of legacy systems.

---

## Platform Vision

The Enterprise Insurance Platform is the operating backbone for Saudi insurance companies, providing:

- **End-to-end digital policy lifecycle** — from quote to issuance, endorsement, renewal, and cancellation
- **Claims management** — intake, assessment, approval, and settlement
- **Billing and payments** — integrated with MADA, SADAD, and bank transfer
- **Customer and agent portals** — bilingual (Arabic/English), mobile-first experience
- **Regulatory compliance** — built-in SAMA reporting, audit trail, and data residency

---

## Strategic Goals

### 1. Rapid Product Launch
Configure new insurance products (motor, health, travel, property) by defining metadata — not writing code. Business analysts can set field definitions, validation rules, underwriting parameters, and form layouts through the admin portal.

### 2. Saudi Market Alignment
- Native Arabic (RTL) UI with dual-language support
- Integration with Saudi national systems: Yakeen (identity), Najm (accident history)
- SAMA cybersecurity framework compliance
- Data residency in the Saudi Arabia region

### 3. Operational Excellence
Every capability is instrumented from day one:
- Structured logs, Prometheus metrics, distributed traces
- Health dashboards for business KPIs (policy issuance rate, claim settlement time)
- Automated alerting for integration failures and SLA breaches

### 4. Developer-Friendly Platform
- API-first design with OpenAPI 3.0 specifications
- Domain-driven code organisation with clear service boundaries
- Automated CI/CD pipeline from commit to production
- Infrastructure-as-code for environment reproducibility

### 5. Flexible Deployment for Every Insurer
- **Cloud SaaS**: A fully managed, multi-tenant or single-tenant offering for rapid onboarding.
- **Self-Hosted**: A deployable package for clients requiring on-premises data residency.
- **Single Codebase**: Both deployment models use the same source code and configuration mechanisms.

---

## Platform Principles

1. **Configuration over code** — new products, fields, and validation rules are defined through a **metadata-driven UI (Configuration Workbench)** and stored as **immutable events**, eliminating the need for code deployments or database migrations for schema changes.
2. **Domain integrity** — each service owns its data; no cross-service direct DB access
3. **API-first** — all capabilities exposed as versioned REST APIs before building any UI
4. **Secure by default** — JWT-secured, RBAC-enforced, audit-logged from the first line
5. **Observable from day one** — every service ships with logs, metrics, and traces
6. **Saudi-first localisation** — Arabic language, Hijri calendar, RTL layout as a primary concern, not an afterthought
7. **Resilient integrations** — all external dependencies wrapped with circuit breakers and fallbacks

---

## Key Technology Choices

| Component | Choice | Rationale |
| :--- | :--- | :--- |
| **Backend** | Spring Boot 3.x (Java 17+) | Enterprise-grade, high performance, strong typing |
| **Frontend** | Angular 17+ (Standalone) | Enterprise SPA, Material Design, RTL support |
| **Database** | PostgreSQL 15+ with JSONB | ACID compliance, flexible schema, single source of truth |
| **Configuration** | Event-Sourced Metadata | Immutable audit trail, zero-downtime changes |
| **Dynamic Fields** | JSONB column (`data`) | No ALTER TABLE, backward compatibility |
| **Caching** | Redis | Low-latency reads for metadata views |
| **Real-Time Updates** | WebSocket + Redis Pub/Sub | Instant UI reactivity to configuration changes |
| **Deployment** | Docker + Kubernetes | Cloud-native and self-hosted support |

---

## Target Users

| User Type | Role | Primary Interface |
| :--- | :--- | :--- |
| **Customer** | Policyholders, claimants | Customer portal (Angular SPA) |
| **Agent / Broker** | Insurance sales intermediaries | Agent portal (Angular SPA) |
| **Underwriter** | Policy review and approval | Operations portal (Angular SPA) |
| **Claims Handler** | Claims processing and settlement | Operations portal (Angular SPA) |
| **Business Analyst** | Product and form configuration | Admin portal (Angular SPA) |
| **Platform Engineer** | Service development and deployment | Spring Boot services + CI/CD |
| **Operations** | Monitoring, incident response | Grafana dashboards + runbooks |
| **Core Engineering Team** | Build the metadata engine, APIs, and core services | Spring Boot, PostgreSQL, Kafka |
| **Frontend Engineering Team** | Build portals, workbenches, and dynamic forms | Angular, TypeScript, WebSocket |
| **DevOps Engineer** | CI/CD, Kubernetes, monitoring, self-hosted packaging | Terraform, Docker, Prometheus, Grafana |
| **QA Engineer** | Automate testing, performance/security testing | Selenium, JMeter, OWASP ZAP |

---

## Build vs. Buy Decision

We are building a custom platform because:

1. **No vendor** (e.g., Guidewire, Duck Creek) offers out-of-the-box integration with Saudi national systems (Yakeen, Najm, NPHIES).
2. Existing solutions are **rigid and costly** to customize for the metadata-driven, rapid-configuration model we require.
3. Commercial products do not offer the **hybrid deployment model** (self-hosted + cloud) essential for the Saudi market.

---

## Roadmap (Initial Phases)

### Phase 0 — Metadata Foundation
- Build the Metadata Engine (event-sourced configuration)
- Build the Configuration Workbench (Developer UI)
- Implement PostgreSQL JSONB data layer with GIN indexes
- Establish event-sourced metadata tables and caching

### Phase 1 — Platform Foundation
- Platform documentation and architecture design
- Database schema design (core, metadata, motor schemas)
- CI/CD pipeline and Kubernetes infrastructure
- Authentication service (Keycloak + OIDC)
- Core domain services: Policy, Claims, Billing (Motor line)

### Phase 2 — Motor Line
- Motor Comprehensive and Motor TPL product launch
- Yakeen integration (identity verification)
- Najm integration (accident history)
- MADA/SADAD payment integration
- Customer and agent portal (Angular)
- SAMA motor reporting

### Phase 3 — Multi-Line Expansion
- Health insurance line (individual and group)
- Travel insurance line
- Property insurance line
- Cross-line reporting and analytics dashboard

### Phase 4 — Platform Maturity
- Partner API (B2B integration layer)
- Native mobile applications (iOS / Android)
- AI-assisted underwriting recommendations
- Advanced analytics and BI integration

---

## Success Metrics

| Metric | Target |
|---|---|
| Policy issuance end-to-end time | < 5 minutes (motor) |
| Platform API availability | 99.9% uptime |
| Claims registration time | < 3 minutes |
| SAMA regulatory report delivery | 100% on-time |
| UI page load time (P95) | < 2 seconds |
| New product configuration time | < 1 day (without code changes) |
