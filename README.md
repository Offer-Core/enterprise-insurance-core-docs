# Enterprise Core Insurance Platform

## Overview

The Enterprise Core Insurance Platform is a comprehensive, cloud-native insurance
management system designed to support the full insurance lifecycle. This platform
provides a unified solution for policy administration, claims management, billing,
and customer engagement.

## Architecture

The platform follows a modern, event-driven microservices architecture built on
Spring Boot and MongoDB. It implements Domain-Driven Design (DDD) principles and
hexagonal architecture patterns to ensure maintainability and scalability.

### Key Components

- **Core Insurance Engine**: Handles policy administration, underwriting, and rating
- **Claims Management**: End-to-end claims processing with workflow automation
- **Billing & Payments**: Flexible billing schedules and payment processing
- **Customer Portal**: Self-service capabilities for policyholders
- **Agent Portal**: Full-featured interface for insurance agents and brokers
- **Admin Console**: Comprehensive administration and reporting tools

## Technology Stack

### Backend
- Java 17+ with Spring Boot 3.x
- MongoDB for primary data storage
- Redis for caching and session management
- Apache Kafka for event streaming
- Keycloak for identity and access management
- Apache APISIX for API gateway

### Frontend
- Angular 16+ with TypeScript
- NgRx for state management
- Angular Material for UI components
- D3.js for data visualization

### Infrastructure
- Kubernetes for container orchestration
- Docker for containerization
- Terraform for infrastructure as code
- Prometheus and Grafana for monitoring
- ELK Stack for logging and analytics

## Documentation Structure

This repository contains comprehensive documentation organized as follows:

- **000-Vision**: Product vision, mission, and strategic goals
- **100-Business**: Business context, processes, and requirements
- **200-Domains**: Domain models and business capabilities
- **300-Architecture**: System architecture and design patterns
- **Architecture Docs**: Start here for the living Markdown architecture set in [knowledge-base/architecture/README.md](knowledge-base/architecture/README.md)
- **400-Standards**: Development standards and best practices
- **500-API**: API design and specifications
- **600-Database**: Database design and data models
- **700-UI**: User interface design and guidelines
- **800-Integrations**: Integration patterns and external systems
- **900-Decisions**: Architecture decision records
- **950-Templates**: Documentation templates
- **990-AI**: AI-assisted development guidelines

## Getting Started

### Prerequisites

- Java 17 or later
- Node.js 18 or later
- Docker Desktop
- MongoDB 6.0+
- IDE with Java and TypeScript support

### Quick Start

1. Clone this repository
2. Run `docker-compose up -d` to start dependencies
3. Build the backend: `./gradlew build`
4. Build the frontend: `npm install && npm run build`
5. Start the application: `./gradlew bootRun`

## Development Guidelines

### Code Quality

- Follow Java and TypeScript coding standards
- Write unit tests for all business logic
- Maintain test coverage above 80%
- Use static code analysis tools

### Security

- Implement proper authentication and authorization
- Encrypt sensitive data at rest and in transit
- Follow OWASP security guidelines
- Regular security audits and penetration testing

### DevOps

- Infrastructure as Code (IaC) approach
- Continuous Integration and Deployment (CI/CD)
- Environment-based configuration
- Automated testing and quality gates

## Contributing

Please read our [Contributing Guidelines](docs/guides/contributing.md) before
submitting pull requests.

## License

This project is proprietary software. All rights reserved.

## Support

For support inquiries, please contact the Enterprise Architecture Team.

## Version History

| Version | Date | Description |
|---------|------|-------------|
| 1.0.0   | 2024-01-01 | Initial release |

