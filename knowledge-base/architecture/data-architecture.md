# Data Architecture

## Overview

The data architecture follows a layered model that keeps the core shared data stable while allowing line-specific data and dynamic extensions to evolve independently.

## Data Principles

- Shared core data is modeled as stable domain entities
- Line-specific fields are isolated in line-owned schemas or JSONB payloads
- Dynamic extensions are stored as metadata-driven attributes
- Data access is governed by domain boundaries and service ownership

## Core Data Domains

- Customer and party data
- Policy data
- Claims data
- Financial transaction data
- Audit and event data

## Persistence Strategy

### 1. Strongly Typed Core Tables

These tables store the shared kernel entities:
- customer
- policy
- claim
- financial_transaction
- audit_log

### 2. Line-Specific Tables

Motor-specific tables are stored in a separate schema or database namespace:
- motor_policy_extension
- motor_driver
- motor_vehicle
- motor_claim_detail

### 3. Dynamic Attribute Store

Dynamic fields are stored in JSONB columns such as:
- `line_specific_data`
- `dynamic_attributes`

This supports UI-driven extensions without requiring immediate schema changes.

## Data Migration Strategy

- Use Flyway or Liquibase for controlled schema changes
- Keep shared schema migrations separate from line-specific migrations
- Assign migration ownership by team and service
- Avoid shared-table changes for line-local requirements

## Data Governance

- Every entity should have an owner
- Sensitive data should be encrypted at rest and in transit
- Audit trails must be retained for policy and claims events
- Data retention policies must reflect regulatory needs for Saudi operations
