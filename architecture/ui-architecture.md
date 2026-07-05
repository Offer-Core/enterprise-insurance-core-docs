# UI Architecture

## Purpose

The user experience must support policy operations, underwriting, claims, and admin workflows while remaining flexible enough to expose dynamic business fields from configuration.

## Core UI Layers

### 1. Portal Experience

The platform should support:
- customer self-service
- agent and broker workflows
- internal underwriting and claims operations
- administrator configuration screens

### 2. Dynamic Form Renderer

UI screens should not be hard-coded for every field. Instead, the front end should render forms from metadata.

Capabilities:
- render text, number, select, date, lookup, and boolean fields
- apply validation rules from configuration
- support conditional visibility and requiredness
- support Arabic and English labels and localization

### 3. Metadata-Driven Configuration

Business users should be able to create or modify fields, forms, and rule-driven behavior without a full software release.

## Screen Types

- Policy creation screen
- Claims intake screen
- Underwriting review screen
- Billing summary screen
- Administration and configuration console

## UI Principles

- Consistent component patterns across all modules
- Separation of domain UI from configuration UI
- Role-based access to screens and actions
- Localized content for the Saudi Arabia market
