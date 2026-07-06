# Business Context

This section defines the business landscape, stakeholder needs, market drivers, and operational requirements that shape the Enterprise Insurance Platform.

---

## Table of Contents

1. [Market Overview](#market-overview)
2. [Business Drivers](#business-drivers)
3. [Stakeholder Map](#stakeholder-map)
4. [Key Business Processes](#key-business-processes)
5. [Regulatory Landscape](#regulatory-landscape)
6. [Success Criteria](#success-criteria)
7. [Glossary of Business Terms](#glossary-of-business-terms)

---

## Market Overview

### Saudi Insurance Market Context

The Saudi insurance sector is undergoing rapid digital transformation driven by Vision 2030. Key market dynamics:

| Factor | Context |
| :--- | :--- |
| **Market Growth** | Insurance sector revenue grew ~10.2% in 2025, reaching SAR 71.2 billion |
| **Profitability Pressure** | Industry loss ratio at 89.3% — insurers are digitizing to reduce costs |
| **Regulatory Push** | SAMA mandates digital transformation and enhanced reporting |
| **Competitive Landscape** | Incumbents (Tawuniya, etc.) face pressure from new InsurTech entrants |

### Target Market Segments

| Segment | Characteristics | Platform Fit |
| :--- | :--- | :--- |
| **Large Insurers** | >SAR 200M revenue, multiple lines, complex IT | Self-hosted deployment, full customization |
| **Mid-Size Insurers** | SAR 50-200M revenue, 2-3 lines of business | Cloud SaaS or self-hosted, configuration-driven |
| **New Entrants** | Newly licensed, digital-first | Cloud SaaS, rapid product launch |

---

## Business Drivers

### 1. Speed to Market
- **Current Problem:** Launching a new product takes 6-12 months with legacy systems
- **Platform Solution:** Configuration-driven approach enables **new product launch in <1 day** without code changes
- **Business Impact:** Insurers can respond to market opportunities faster and test new products with lower risk

### 2. Regulatory Compliance
- **Current Problem:** Manual reporting and fragmented systems create compliance risk
- **Platform Solution:** Built-in SAMA reporting, automated audit trails, and real-time regulatory integration
- **Business Impact:** Reduced compliance costs, 100% on-time reporting, lower regulatory fines

### 3. Customer Experience
- **Current Problem:** Siloed channels and slow processes frustrate customers
- **Platform Solution:** Omnichannel experience with mobile-first portals, real-time updates
- **Business Impact:** Higher Net Promoter Score (NPS), improved retention, digital acquisition

### 4. Operational Efficiency
- **Current Problem:** Manual processes increase cost-to-serve
- **Platform Solution:** Automated underwriting, claims processing, and policy servicing
- **Business Impact:** 40-60% reduction in manual processing time, lower expense ratios

### 5. Competitive Differentiation
- **Current Problem:** All insurers offer similar products
- **Platform Solution:** Rapid product innovation via configuration
- **Business Impact:** First-mover advantage in new insurance lines

---

## Stakeholder Map

### Internal Stakeholders

| Stakeholder | Role | Key Concerns |
| :--- | :--- | :--- |
| **Board & Executives** | Strategic oversight | ROI, market share, regulatory risk, competitive position |
| **Product Management** | Define product requirements | Feature prioritization, market fit, competitive analysis |
| **Business Analysts** | Configure products | Ease of configuration, rapid iteration, self-service tools |
| **Underwriters** | Risk assessment | Accurate pricing, real-time risk data, automated workflows |
| **Claims Handlers** | Process claims | Efficient workflow, fast settlement, fraud detection |
| **Operations** | Day-to-day processes | System reliability, manual work reduction, integration stability |
| **IT & Engineering** | Build and maintain | Technical quality, maintainability, performance, security |
| **Compliance** | Regulatory adherence | Audit trails, SAMA compliance, data privacy |
| **Sales & Marketing** | Client acquisition | Product capabilities, competitive positioning, sales enablement |

### External Stakeholders

| Stakeholder | Role | Key Concerns |
| :--- | :--- | :--- |
| **Customers** | Policyholders & claimants | Easy experience, fast claims, transparency, accessibility |
| **Agents/Brokers** | Distribution channel | Easy onboarding, commission management, self-service tools |
| **Regulators (SAMA)** | Oversight and enforcement | Compliance, reporting, consumer protection |
| **Najm** | Accident history provider | Data accuracy, integration stability |
| **Yakeen** | Identity verification | Reliable authentication, data privacy |
| **NPHIES** | Health eClaims exchange | FHIR compliance, secure data exchange |

---

## Key Business Processes

### 1. Policy Lifecycle

| Stage | Description | Key Systems | Business Impact |
| :--- | :--- | :--- | :--- |
| **Quote** | Generate premium based on risk factors | Underwriting engine, pricing rules | Speed to quote determines win rate |
| **Bind** | Capture and lock the policy | Policy service, Yakeen integration | Completion rate and drop-off analysis |
| **Issue** | Issue policy document and start coverage | Document generation, notification service | First contact resolution and satisfaction |
| **Servicing** | Changes to policy during its term | Endorsements service | Ease and speed of self-service changes |
| **Renewal** | Continue coverage for a new term | Renewal engine, pricing recalculation | Retention rate and total cost of renewal |
| **Cancellation** | End policy before expiry | Cancellation, refund and proration logic | Reason tracking for data analysis |

### 2. Claims Lifecycle

| Stage | Description | Key Systems | Business Impact |
| :--- | :--- | :--- | :--- |
| **Intake** | Record claim details | Claims service, Najm integration | First notice of loss, urgency and sentiment |
| **Investigation** | Assess claim validity | Workflow engine, assessment rules | Proper investigation to prevent fraud |
| **Approval** | Approve or deny the claim | Workflow and decision engine | Response time and speed-to-decision |
| **Settlement** | Pay the claim amount | Payments and settlement service | Customer satisfaction, time-to-cash |
| **Closure** | Finalize and archive | Document management, archiving | Next best action for customer retention |

### 3. Billing & Payments

| Stage | Description | Key Systems | Business Impact |
| :--- | :--- | :--- | :--- |
| **Invoice** | Generate and send invoices | Billing service, notification service | Accuracy, timeliness, and billing satisfaction |
| **Payment** | Process payments | Payment gateway, MADA/SADAD | Effective payment, dunning and collection |
| **Reconciliation** | Match payments to invoices | Accounting integration, reconciliation | Balance sheet integrity and cash flow accuracy |

---

## Regulatory Landscape

### SAMA Regulatory Requirements

| Requirement | Description | Platform Implementation |
| :--- | :--- | :--- |
| **Data Residency** | All policyholder data must reside in KSA | Cloud SaaS uses Saudi data centers; self-hosted meets requirement naturally |
| **Data Privacy** | PII must be protected according to SAMA standards | Field-level encryption, access controls, audit logging |
| **Audit Trail** | Every transaction must be traceable to a user | Immutable audit log, user attribution, timestamped events |
| **Cybersecurity** | SAMA cybersecurity framework compliance | JWT authentication, RBAC, secure coding practices |
| **Reporting** | Mandatory reports to SAMA | Automated report generation, data extraction APIs |
| **Approval** | Insurers must obtain SAMA approval for cloud vendor contracts | Documentation and compliance support for clients |

### National System Integrations

| System | Purpose | Integration Requirement |
| :--- | :--- | :--- |
| **Yakeen** | Identity verification | Real-time API call during policy binding |
| **Najm** | Accident history lookup | API integration for claims processing |
| **NPHIES** | Health eClaims exchange (FHIR) | Mandatory for health insurance lines |
| **IDI** | Inherent Defects Insurance | Required for building permits |

---

## Success Criteria

### Business KPIs

| Metric | Target | Measurement |
| :--- | :--- | :--- |
| **Time to Market (New Product)** | <1 day | From requirement to live configuration |
| **Policy Issuance Time** | <5 minutes | End-to-end motor policy |
| **Claims Settlement Time** | <3 minutes for auto-adjudicated claims | Time from intake to settlement |
| **Customer Acquisition Cost** | 20% reduction | Over traditional channels |
| **Agent Onboarding Time** | <1 hour | For digital agents/brokers |

### Platform KPIs

| Metric | Target | Measurement |
| :--- | :--- | :--- |
| **API Availability** | 99.9% uptime | Monthly uptime measurement |
| **API Response Time (P95)** | <500ms | Monitoring metrics |
| **Regulatory Reporting** | 100% on-time | Delivery of all SAMA reports |
| **UI Load Time (P95)** | <2 seconds | Frontend performance monitoring |
| **New Feature Adoption** | >80% within 90 days of release | Usage analytics |

---

## Glossary of Business Terms

| Term | Definition |
| :--- | :--- |
| **Policyholder** | The individual or entity that owns the insurance policy |
| **Insured** | The person or property covered by the policy |
| **Premium** | The amount paid by the policyholder for coverage |
| **Sum Insured** | The maximum amount payable under a policy |
| **Loss Ratio** | Claims paid divided by premiums earned |
| **Expense Ratio** | Operating expenses divided by premiums earned |
| **Combined Ratio** | Loss ratio + Expense ratio (profitability indicator) |
| **Takaful** | Islamic cooperative insurance compliant with Shariah law |
| **Endorsement** | A change to an existing policy |
| **Grace Period** | Time after premium due date before policy lapses |
| **Underwriting** | The process of evaluating risk and determining premium |
| **Claims Runoff** | Claims incurred but not yet settled |
| **Reinsurance** | Insurance purchased by insurers to manage risk |
| **SAMA** | Saudi Central Bank (regulator) |
| **Vision 2030** | Saudi Arabia's strategic framework for economic diversification |

---

## Document Maintenance

| Aspect | Detail |
| :--- | :--- |
| **Last Updated** | [Current Date] |
| **Owner** | Enterprise Architecture Team |
| **Review Cycle** | Quarterly, or when major business changes occur |
| **Approval** | Architecture Review Board |

---

## Questions?

For questions about business context or requirements:

- **Enterprise Architecture Team**: architecture@[company].com
- **Product Management**: product@[company].com
- **Compliance**: compliance@[company].com
