# Release Candidate

RC date: 2026-05-04

Status: READY_FOR_GATEKEEPER

## Scope

This release candidate includes:

- Spring Boot backend with Flyway migrations V1-V9.
- React frontend operational dashboard.
- Tenant identity, companies/B2B, fiscal documents, invoicing, Verifactu/SIF, QR/AEAT stub and B2B e-invoice flows.
- Docker local/preprod runtime.
- Jenkins local CI/CD controller with JCasC.
- kind Kubernetes preprod with Nginx reverse proxy/cache.
- Keycloak local OIDC runtime with importable realm and public PKCE frontend client.
- SaaS tenant lifecycle and subscription plan administration primitives.
- Prometheus metrics endpoint and local Prometheus runtime.
- Preprod backup, log and health scripts.

## Validation Evidence

| Check | Result |
|---|---|
| Backend `mvnw -DskipTests=false verify` | PASS |
| Frontend lint | PASS |
| Frontend unit tests | PASS |
| Frontend build | PASS |
| Local Playwright | PASS |
| Docker Compose local/preprod/Keycloak/observability config | PASS |
| Jenkins controller bootstrap | PASS |
| Jenkinsfile configuration for backend, frontend, Playwright, Compose, Keycloak and observability | PASS |
| Kubernetes node/pod readiness | PASS |
| Kubernetes smoke script | PASS |
| Preprod Playwright critical flows | PASS |
| Nginx syntax and cache headers | PASS |
| Keycloak bootstrap and realm discovery | PASS |
| Prometheus target health | PASS |
| Preprod health report script with metrics check | PASS |
| Public preproduction readiness script | BLOCKED_BY_EXTERNAL_INFRASTRUCTURE |
| Preprod log collection script | PASS |
| Preprod MySQL backup script | PASS |
| Forbidden tracked file check | PASS |

## Known Exclusions

- No production deployment.
- No production AEAT dispatch.
- No legal Verifactu certification.
- No committed secrets, env files, kubeconfig, certs or database dumps.
- No SonarQube runtime.
- No TLS/cert-manager.
- No centralized monitoring, alerting or tracing.
- No billing provider or public payment webhooks.
- No public preproduction DNS/TLS endpoint.

## Release Notes

The RC is suitable for local preproduction demonstration and gatekeeper review. It is not suitable for production without the excluded controls.
