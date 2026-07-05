# API Architecture

## Purpose

The platform exposes a set of internal and external APIs that support policy, claims, billing, underwriting, and administration workflows for the Saudi Arabia MVP.

## Architectural Principles

- API-first design for all domain capabilities
- Versioned contracts for backward compatibility
- Clear separation between internal application APIs and external partner APIs
- Use of asynchronous events for long-running workflows
- Strong authentication and authorization at every boundary

## API Layers

### 1. Edge/API Gateway

The API Gateway is the single entry point for external clients and partner systems.

Responsibilities:
- request routing
- authentication and authorization
- throttling and rate limiting
- request/response transformation
- observability and tracing

### 2. Domain APIs

Each line or bounded context exposes its own API surface.

Suggested modules:
- Policy API
- Claims API
- Billing API
- Underwriting API
- Customer API

### 3. Integration APIs

These are adapter-style APIs for external systems such as payment providers, document services, and regulatory reporting interfaces.

## API Style

- REST for synchronous operations
- Kafka or event bus for asynchronous integration
- OpenAPI definitions for API contracts
- JSON payloads with consistent envelope structure

## Suggested Endpoint Groups

- `/customers`
- `/policies`
- `/claims`
- `/billing/transactions`
- `/underwriting/rules`
- `/metadata/entities`
- `/metadata/forms`

## Versioning Strategy

- URI versioning such as `/v1/`
- Breaking changes require a new major version
- Non-breaking changes remain compatible within the same major version

## Dynamic Extension API

The metadata layer must expose APIs for runtime extensibility.

Example endpoints:
- `GET /metadata/entities`
- `POST /metadata/entities`
- `GET /metadata/entities/{entityCode}/fields`
- `POST /metadata/forms`
- `GET /metadata/rules`

These endpoints allow the UI to render dynamic forms and drive workflow configuration without changing backend code.
