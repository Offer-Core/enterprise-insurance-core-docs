# Architecture Documentation

This folder contains the core architecture documentation for the insurance platform.

## Purpose

These documents provide a shared, Markdown-based view of the system structure, responsibilities, integrations, security controls, and deployment model. They are intended to support planning, review, and implementation alignment.

## Document Map

- [Architecture Overview](overview.md) — purpose, goals, principles, and high-level structure
- [Domain Architecture](domain-architecture.md) — bounded contexts, core domains, and domain responsibilities
- [Motor-First Extensible Architecture](domain-architecture.md) — MVP Motor architecture with explicit extension points for future lines
- [Integration Architecture](integration-architecture.md) — APIs, events, and external system connections
- [API Architecture](api-architecture.md) — API layers, versioning, gateway, and contract strategy
- [Data Architecture](data-architecture.md) — persistence model, schemas, and migration approach
- [UI Architecture](ui-architecture.md) — dynamic forms, portal structure, and configurable experience
- [Operations and Observability](operations-observability.md) — monitoring, alerts, and service health
- [Architecture Decisions](architecture-decisions.md) — key ADRs and design rationale
- [Security Architecture](security-architecture.md) — authentication, authorization, data protection, and controls
- [Deployment Architecture](deployment-architecture.md) — environments, runtime topology, and observability

## Working Guidelines

- Keep each document focused on one architectural concern.
- Update the documents whenever a significant design decision changes.
- Prefer short, structured sections with clear ownership and status.
- Use links between documents to connect related decisions and components.
