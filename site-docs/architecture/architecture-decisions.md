# Architecture Decisions

This document captures the key architectural decisions for the platform and the rationale behind them.

## ADR 1: Java and Spring Boot as the Core Platform Stack

Decision: Use Java with Spring Boot for backend services.

Reason:
- strong enterprise support
- mature ecosystem for APIs, persistence, and security
- good fit for modular service design

## ADR 2: Metadata-Driven Extensibility

Decision: Support business-driven field and workflow extension through metadata.

Reason:
- reduces the need for code changes for new business requirements
- supports future line onboarding
- keeps the core system stable while allowing line-specific evolution

## ADR 3: JSONB-Based Dynamic Attributes

Decision: Use JSONB storage for dynamic attributes rather than a fully generic EAV model.

Reason:
- simpler and more performant for the current scope
- easier to query and reason about than a universal EAV structure
- suitable for nested and future line-specific data

## ADR 4: Schema Isolation for Line-Specific Data

Decision: Keep line-specific tables and migrations isolated from the shared kernel schema.

Reason:
- reduces change risk
- avoids cross-team migration conflicts
- supports independent delivery of future lines

## ADR 5: Event-Driven Integration for Workflow Coordination

Decision: Use events for asynchronous business workflows and cross-service communication.

Reason:
- decouples services
- improves scalability
- simplifies future integration points
