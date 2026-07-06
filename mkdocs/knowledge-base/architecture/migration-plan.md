# Migration Plan — Legacy to Platform

## Overview

This document defines the strategy and execution plan for migrating from legacy insurance systems to the new Enterprise Insurance Platform. The migration follows a **strangler fig pattern** — gradually replacing legacy functionality while maintaining business continuity.

---

## 1. Migration Principles

| Principle | Description |
|-----------|-------------|
| **Business Continuity** | No downtime during migration. Legacy and new systems run in parallel. |
| **Data Integrity** | Every record migrated must be verifiable via checksums and reconciliation. |
| **Incremental Migration** | Migrate one product line at a time (Motor first, then Health, then Travel). |
| **Rollback Capability** | Every migration phase must have a tested rollback plan. |
| **Regulatory Compliance** | SAMA reporting must be continuous — no gaps during migration. |

---

## 2. Migration Phases

### Phase 0: Foundation (Weeks 1-4)

| Activity | Owner | Duration | Deliverable |
|----------|-------|----------|-------------|
| Set up platform infrastructure | DevOps | Week 1 | K8s cluster, CI/CD, monitoring |
| Deploy metadata engine | Platform | Week 2 | Product configuration, field definitions |
| Set up event infrastructure | Platform | Week 2 | Kafka cluster, topics, schema registry |
| Deploy Keycloak | Security | Week 3 | Realm, clients, roles, users |
| Set up data pipelines | Data | Week 3-4 | CDC from legacy DB, initial sync |
| Integration testing | QA | Week 4 | WireMock stubs, integration test suite |

### Phase 1: Motor — New Business Only (Weeks 5-8)

| Activity | Owner | Duration | Deliverable |
|----------|-------|----------|-------------|
| Deploy Policy Service | Motor Team | Week 5 | Motor policy issuance API |
| Deploy Customer Service | Platform | Week 5 | Customer management API |
| Yakeen integration | Motor Team | Week 5-6 | Identity verification working |
| Najm integration | Motor Team | Week 6 | Accident history retrieval |
| MADA payment integration | Billing Team | Week 6-7 | Payment processing |
| Premium calculation engine | Motor Team | Week 7 | Rating factors, NCD, loading |
| UAT with new business | Business | Week 8 | Sign-off on new business flow |

**Scope:** All new Motor policies are issued on the new platform. Legacy system continues to service existing policies.

### Phase 2: Motor — Renewals Migration (Weeks 9-12)

| Activity | Owner | Duration | Deliverable |
|----------|-------|----------|-------------|
| Build renewal data migration | Data Team | Week 9 | ETL scripts for policy data |
| Migrate active policies | Data Team | Week 10 | 50,000 policies migrated |
| Migrate customer data | Data Team | Week 10 | 30,000 customers migrated |
| Reconciliation | Data Team | Week 10-11 | 100% data match verified |
| Renewal cutover | Motor Team | Week 11 | All renewals on new platform |
| Legacy read-only | Operations | Week 12 | Legacy system set to read-only for Motor |

**Scope:** All Motor policy renewals are processed on the new platform. Legacy system is read-only for Motor data.

### Phase 3: Motor — Claims Migration (Weeks 13-16)

| Activity | Owner | Duration | Deliverable |
|----------|-------|----------|-------------|
| Deploy Claims Service | Motor Team | Week 13 | Motor claims API |
| Migrate open claims | Data Team | Week 13-14 | 5,000 open claims migrated |
| Migrate closed claims | Data Team | Week 14 | 20,000 closed claims migrated |
| Claims reconciliation | Data Team | Week 14-15 | 100% data match verified |
| FNOL cutover | Motor Team | Week 15 | All new claims on new platform |
| Legacy decommission prep | Operations | Week 16 | Legacy Motor system ready for shutdown |

### Phase 4: Motor — Legacy Decommission (Weeks 17-18)

| Activity | Owner | Duration | Deliverable |
|----------|-------|----------|-------------|
| Final data reconciliation | Data Team | Week 17 | Complete data audit |
| Archive legacy data | Data Team | Week 17 | Cold storage archive |
| Update SAMA reporting | Compliance | Week 17 | Reporting from new platform |
| Decommission legacy | Operations | Week 18 | Legacy Motor system shutdown |
| Post-mortem | All | Week 18 | Lessons learned |

### Phase 5: Health Line (Weeks 19-30)

*Repeat phases 1-4 for Health insurance line of business.*

### Phase 6: Travel Line (Weeks 31-38)

*Repeat phases 1-4 for Travel insurance line of business.*

---

## 3. Data Migration Strategy

### 3.1 Migration Approach

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Legacy DB     │────▶│  CDC Pipeline   │────▶│   New Platform  │
│  (PostgreSQL)   │     │  (Debezium)     │     │  (PostgreSQL)   │
└─────────────────┘     └─────────────────┘     └─────────────────┘
         │                       │                        │
         │                       │                        │
         ▼                       ▼                        ▼
  ┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
  │  Full Export    │     │  Change Events  │     │  Reconciliation │
  │  (pg_dump)      │     │  (Kafka)        │     │  (Checksum)     │
  └─────────────────┘     └─────────────────┘     └─────────────────┘
```

### 3.2 Data Mapping

| Legacy Table | New Table | Mapping Complexity | Notes |
|-------------|-----------|-------------------|-------|
| `legacy.customers` | `core.customers` | Medium | NIN encryption, Arabic name split |
| `legacy.policies` | `core.policies` | High | JSONB restructuring, line-specific split |
| `legacy.claims` | `core.claims` | High | JSONB restructuring, motor details split |
| `legacy.vehicles` | `motor.motor_vehicles` | Low | Direct mapping |
| `legacy.drivers` | `motor.motor_drivers` | Low | Direct mapping |
| `legacy.payments` | `core.financial_transactions` | Medium | Status mapping, gateway refs |
| `legacy.documents` | `core.documents` | Low | Storage path migration |

### 3.3 ETL Script Example

```sql
-- Migration: Legacy policies to new platform
-- Phase: Initial bulk load

BEGIN;

-- Create staging table
CREATE TEMP TABLE staging_policies AS
SELECT
    lp.id AS legacy_id,
    lp.policy_number,
    CASE lp.product_type
        WHEN 'MOTOR_COMP' THEN 'MOTOR_COMP'
        WHEN 'MOTOR_TPL' THEN 'MOTOR_TPL'
        ELSE 'MOTOR_COMP'
    END AS product_code,
    'MOTOR' AS line_of_business,
    nc.new_customer_id AS customer_id,
    CASE lp.status
        WHEN 'ACTIVE' THEN 'ACTIVE'
        WHEN 'CANCELLED' THEN 'CANCELLED'
        WHEN 'EXPIRED' THEN 'EXPIRED'
        WHEN 'DRAFT' THEN 'DRAFT'
    END AS status,
    lp.effective_date,
    lp.expiry_date,
    lp.premium AS annual_premium,
    'SAR' AS currency,
    lp.tax_amount,
    lp.commission_amount,
    lp.commission_rate,
    jsonb_build_object(
        'vehicle', jsonb_build_object(
            'sequence_number', lv.sequence_number,
            'plate_number', lv.plate_number,
            'make', lv.make,
            'model', lv.model,
            'model_year', lv.model_year,
            'vehicle_value', lv.vehicle_value,
            'vehicle_use', lv.vehicle_use
        ),
        'drivers', (
            SELECT jsonb_agg(
                jsonb_build_object(
                    'driver_type', ld.driver_type,
                    'national_id', pgp_sym_encrypt(ld.national_id, '${encryption_key}'),
                    'full_name_ar', ld.full_name_ar,
                    'license_number', ld.license_number,
                    'no_claims_discount', ld.ncd,
                    'years_of_experience', ld.experience_years
                )
            )
            FROM legacy.drivers ld
            WHERE ld.policy_id = lp.id
        )
    ) AS line_specific_data,
    lp.issued_at,
    lp.cancelled_at,
    lp.cancellation_reason,
    'tenant-001' AS tenant_id,
    NOW() AS created_at,
    NOW() AS updated_at,
    '00000000-0000-0000-0000-000000000000'::UUID AS created_by,
    0 AS version
FROM legacy.policies lp
JOIN legacy.customers lc ON lc.id = lp.customer_id
JOIN customer_mapping nc ON nc.legacy_id = lc.id
LEFT JOIN legacy.vehicles lv ON lv.policy_id = lp.id
WHERE lp.line_of_business = 'MOTOR'
  AND lp.created_at >= '2024-01-01';

-- Insert into new platform
INSERT INTO core.policies (
    policy_number, product_code, line_of_business,
    customer_id, status, effective_date, expiry_date,
    annual_premium, currency, tax_amount,
    commission_amount, commission_rate,
    line_specific_data, tenant_id,
    issued_at, cancelled_at, cancellation_reason,
    created_at, updated_at, created_by, version
)
SELECT
    policy_number, product_code, line_of_business,
    customer_id, status, effective_date, expiry_date,
    annual_premium, currency, tax_amount,
    commission_amount, commission_rate,
    line_specific_data, tenant_id,
    issued_at, cancelled_at, cancellation_reason,
    created_at, updated_at, created_by, version
FROM staging_policies;

-- Record migration audit
INSERT INTO migration.audit_log (
    batch_id, entity_type, records_migrated,
    checksum, migrated_at
) VALUES (
    'BATCH-001', 'POLICY',
    (SELECT COUNT(*) FROM staging_policies),
    (SELECT MD5(string_agg(policy_number || status || annual_premium::text, ',' ORDER BY policy_number))
     FROM staging_policies),
    NOW()
);

COMMIT;
```

### 3.4 Reconciliation Query

```sql
-- Reconciliation: Compare legacy vs new platform
SELECT
    'POLICY' AS entity_type,
    COUNT(*) AS total_records,
    COUNT(CASE WHEN match_status = 'MATCH' THEN 1 END) AS matched,
    COUNT(CASE WHEN match_status = 'MISMATCH' THEN 1 END) AS mismatched,
    COUNT(CASE WHEN match_status = 'MISSING_IN_NEW' THEN 1 END) AS missing_in_new,
    COUNT(CASE WHEN match_status = 'MISSING_IN_LEGACY' THEN 1 END) AS missing_in_legacy
FROM (
    SELECT
        COALESCE(lp.policy_number, np.policy_number) AS policy_number,
        CASE
            WHEN lp.policy_number IS NULL THEN 'MISSING_IN_LEGACY'
            WHEN np.policy_number IS NULL THEN 'MISSING_IN_NEW'
            WHEN lp.premium != np.annual_premium THEN 'MISMATCH'
            WHEN lp.status != np.status THEN 'MISMATCH'
            ELSE 'MATCH'
        END AS match_status
    FROM legacy.policies lp
    FULL OUTER JOIN core.policies np
        ON np.policy_number = lp.policy_number
    WHERE COALESCE(lp.line_of_business, 'MOTOR') = 'MOTOR'
) subquery;
```

---

## 4. Rollback Plan

### 4.1 Rollback Triggers

| Trigger | Threshold | Action |
|---------|-----------|--------|
| Data mismatch rate > 0.1% | After migration | Stop migration, revert to legacy |
| Error rate > 2% on new platform | During UAT | Route traffic back to legacy |
| Performance degradation > 20% | During UAT | Scale up new platform, investigate |
| Security vulnerability found | Any time | Halt migration, patch, resume |

### 4.2 Rollback Procedure

```bash
# 1. Stop new traffic
kubectl scale deployment api-gateway -n insurance-ingress --replicas=0

# 2. Revert DNS to legacy
kubectl patch ingress insurance-ingress -n insurance-ingress \
  -p '{"spec":{"rules":[{"host":"api.insurance.sa","http":{"paths":[{"path":"/api/v1/policies","pathType":"Prefix","backend":{"service":{"name":"legacy-gateway","port":{"number":8080}}}}]}}]}}'

# 3. Verify legacy is serving traffic
curl -f https://api.insurance.sa/api/v1/policies/health

# 4. Notify stakeholders
./scripts/notify.sh "Rollback to legacy completed. Investigating root cause."

# 5. Post-mortem
# Document what went wrong, fix, and plan re-migration
```

---

## 5. Cutover Checklist

### 5.1 Pre-Cutover (T-7 days)

- [ ] All integration tests passing
- [ ] Performance tests meeting SLAs
- [ ] Security scan completed (no CRITICAL/HIGH findings)
- [ ] Data migration dry run completed
- [ ] Reconciliation report shows 100% match
- [ ] Rollback plan reviewed and approved
- [ ] Stakeholders notified of cutover date
- [ ] Operations team briefed on monitoring dashboards
- [ ] Support team trained on new platform
- [ ] SAMA reporting continuity verified

### 5.2 Cutover Day

- [ ] 08:00 — Final data sync from legacy
- [ ] 08:30 — Reconciliation check
- [ ] 09:00 — Deploy new platform services
- [ ] 09:15 — Health check: all services green
- [ ] 09:30 — Route 10% traffic to new platform
- [ ] 10:00 — Monitor error rates, latency
- [ ] 10:30 — Route 50% traffic
- [ ] 11:00 — Route 100% traffic
- [ ] 11:30 — Verify all business flows
- [ ] 12:00 — Legacy set to read-only
- [ ] 14:00 — Post-cutover review

### 5.3 Post-Cutover (T+7 days)

- [ ] Monitor error rates daily
- [ ] Verify SAMA report generation
- [ ] Run full reconciliation
- [ ] Collect user feedback
- [ ] Document lessons learned
- [ ] Plan next phase

---

## 6. Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|------------|--------|------------|
| Data loss during migration | Low | Critical | Checksum verification at every step |
| Performance degradation | Medium | High | Load testing, auto-scaling, circuit breakers |
| Integration failure (Yakeen/Najm) | Medium | High | Circuit breaker, fallback to manual |
| Regulatory reporting gap | Low | Critical | Dual reporting during migration |
| User adoption resistance | Medium | Medium | Training, UAT, phased rollout |
| Legacy system decommission delay | Medium | Medium | Parallel run until confidence is high |

---

## Document Maintenance

| Aspect | Detail |
|--------|--------|
| Last Updated | 2026-07-06 |
| Owner | Enterprise Architecture Team |
| Review Cycle | Weekly during active migration phases |
| Related Documents | [Data Architecture](data-architecture.md), [Deployment Architecture](deployment-architecture.md) |
