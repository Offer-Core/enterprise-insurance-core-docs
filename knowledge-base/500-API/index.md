# API Design Standards

This section captures the practical API design standards, endpoint conventions, response contracts, and implementation guidance for the insurance platform.

For a system-level view of API gateways, security topologies, and architectural layers, see the [API Architecture](../architecture/api-architecture.md).

## Purpose

Use this document as the entry point and reference for API design decisions. All development teams must adhere to these standards when building services for policy, claims, billing, underwriting, and customer workflows to ensure consistency, security, and interoperability.

---

## Core API Principles

1. **API-First Design**
   - Define and align on capability contracts with stakeholders before beginning backend service implementation.
2. **Contract-First Development**
   - Use OpenAPI specifications as the single source of truth for both service producers and consumers.
3. **Secure by Default**
   - Enforce authentication, role-based authorization, payload validation, and secure audit logging at every API endpoint boundary.
4. **Graceful Evolution**
   - Evolve contracts additively to avoid breaking changes. Retain backward compatibility wherever possible.

---

## HTTP REST API Conventions

For synchronous request-response actions, the platform standard is REST over HTTP/S.

### 1. Endpoint Naming & Structure
- **Nouns in Plural Form:** Use plural nouns to designate resources. Do not use verbs in resource paths.
  - *Correct:* `GET /policies`, `POST /claims`
  - *Incorrect:* `GET /getPolicy`, `POST /submitClaim`
- **Hierarchical Paths:** Map sub-resources under their parent domain.
  - `GET /policies/{policyId}/documents`
- **Lower Kebab-Case:** Use lowercase kebab-case for paths and query parameters.
  - `GET /underwriting/rules`
  - `GET /policies?page-size=10`

### 2. Standard HTTP Methods
- `GET`: Retrieve a resource or a list. Must be safe and idempotent (no side effects).
- `POST`: Create a new resource, or trigger a non-idempotent action (e.g., initiating adjudication).
- `PUT`: Replace an existing resource entirely, or create if not present (idempotent).
- `PATCH`: Perform a partial update to a resource.
- `DELETE`: Remove a resource.

### 3. Response Status Codes
Services must return standard HTTP status codes indicating the result of the invocation:
- **200 OK**: Request completed successfully.
- **201 Created**: Resource created successfully (must include a `Location` header pointing to the new resource).
- **202 Accepted**: Request accepted for asynchronous processing (e.g., batch jobs, complex underwriting evaluations).
- **400 Bad Request**: Input validation failed, or payload is malformed.
- **401 Unauthorized**: No valid authentication token provided.
- **403 Forbidden**: Authenticated caller lacks permissions (roles/scopes) for the operation.
- **404 Not Found**: The requested resource does not exist.
- **409 Conflict**: State conflict (e.g., trying to endorse a cancelled policy).
- **500 Internal Server Error**: Unexpected server-side failure.

---

## Contract and Documentation Standards

- **OpenAPI Specification (OAS):** All REST APIs must be defined using OpenAPI 3.0 or 3.1.
- **Self-Documenting Specs:** Include descriptive summaries, parameter types, authentication requirements, rate-limiting headers, and detailed schemas.
- **Examples:** Provide complete, representative JSON payload examples for both successful responses and error states.

---

## Standard Error Response Payload

In the event of an error (4xx or 5xx status codes), services must return a standardized JSON envelope structure to simplify client-side handling.

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "The request payload is invalid",
    "details": [
      "policyNumber is required",
      "issueDate must be in the future"
    ]
  }
}
```

**Common Error Codes:**
- `VALIDATION_ERROR`: Input payload failed schema validations.
- `RESOURCE_NOT_FOUND`: Target resource was not found.
- `UNAUTHORIZED_ACCESS`: Missing or invalid security token.
- `INSUFFICIENT_PERMISSIONS`: Caller lacks role or scope access.
- `RESOURCE_CONFLICT`: Operation conflicts with current resource state.
- `INTERNAL_FAILURE`: An unhandled exception occurred on the server.

---

## Versioning Policy

To manage changes while maintaining stability for partner integrations:
- **URI Versioning:** Include the major version as a prefix in the URL path.
  - Format: `/v1/policies`
- **Breaking Changes:** Require incrementing the major version (e.g., `/v2/policies`). A change is breaking if it removes/renames fields, changes status codes, or alters payload structures.
- **Non-Breaking Changes:** (Adding fields, adding endpoints, optional query params) must be deployed under the same major version without breaking existing integrations.

---

## Security Expectations

- **Token-Based Authentication:** All API calls must include an OAuth2 bearer token in the `Authorization` header.
- **Fine-Grained Authorization:** Enforce Role-Based Access Control (RBAC) and verify specific OAuth scopes (e.g., `policy:read`, `claims:write`) per operation.
- **Input Sanitization:** Sanitize all incoming fields to protect against injection attacks (SQL, XSS, etc.).
- **Trace Context Propagation:** APIs must extract and propagate the standard trace header (`X-B3-TraceId` or standard W3C trace context) to ensure end-to-end observability.

---

## Metadata-Driven Extension APIs

The platform leverages dynamic metadata endpoints to allow the user interface to render fields, validation rules, and custom workflow sequences on-the-fly (supporting the motor-first extensibility specified in [ADR-003](../architecture/architecture-decisions.md#adr-003-metadata-driven-extensibility)).

Developers implementing dynamic metadata extensions must expose:
- `GET /metadata/entities` - Lists all configurable core and extended entity definitions.
- `POST /metadata/entities` - Registers a new entity schema or dynamic attributes definition.
- `GET /metadata/entities/{entityCode}/fields` - Returns UI form rules, field types, and labels for a specific business line.
- `POST /metadata/forms` - Configures dynamic step-by-step layout definitions for portals.
- `GET /metadata/rules` - Evaluates or retrieves rule criteria metadata (e.g., premium discount calculations, basic underwriting bounds).

---

## Related Documents

- [API Architecture](../architecture/api-architecture.md) - System-level topology and gateway routing
- [Architecture Overview](../architecture/README.md) - Platform structure and design principles
- [Architecture Decisions](../architecture/architecture-decisions.md) - ADRs and decision log

## Quick Links

- [Vision & Strategy](../000-Vision/index.md)
- [Business Context](../100-Business/index.md)
- [Domain Models](../200-Domains/index.md)
- [Architecture](../architecture/README.md)
- [Standards](../400-Standards/index.md)
- [Database Design](../600-Database/index.md)
- [User Interface](../700-UI/index.md)
- [Integrations](../800-Integrations/index.md)
- [Decisions](../900-Decisions/index.md)
- [Templates](../950-Templates/index.md)
- [AI Assistance](../990-AI/index.md)

## Last Updated

This content was last updated on 2026-07-05.

## Maintainers

- Enterprise Architecture Team
- Platform Engineering Team

## Questions?

For questions about API design standards or contract reviews, contact the Enterprise Architecture Team.
