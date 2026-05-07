# Release Candidate

RC date: 2026-05-07

Status: READY_FOR_GATEKEEPER

## Scope

This release candidate includes:

- Spring Boot backend with Flyway migrations V1-V10.
- React frontend operational workspace centered on tenant, company detail, documents, invoices, local e-invoice and local SIF evidence.
- Tenant identity, companies/B2B, fiscal documents, invoicing, Verifactu/SIF, QR/AEAT stub and B2B e-invoice flows.
- Docker local/preprod runtime.
- Jenkins local CI/CD controller with JCasC.
- kind Kubernetes preprod with Nginx reverse proxy/cache.
- Keycloak local OIDC runtime with importable realm and public PKCE frontend client.
- SaaS tenant lifecycle and subscription plan administration primitives.
- Prometheus metrics endpoint and local Prometheus runtime.
- Company search by name/tax ID, company detail editing, document actions by company, invoice draft edit/issue from UI, and state-aware e-invoice/SIF local actions.
- Fiscal settings per company, invoice series, dedicated customers, issued invoice snapshots, fiscal numbering, discounts, withholding, due dates and manual payments.
- Local PDF artifact generation, corrective draft creation, local cancellation, hash-chained audit events and local evidence ZIP exports.
- Preprod backup, log and health scripts.

## Validation Evidence

| Check | Result |
|---|---|
| Backend `mvnw -DskipTests=false verify` | PASS, 36 tests |
| Frontend lint | PASS |
| Frontend unit tests | PASS |
| Frontend build | PASS |
| Local Playwright full SaaS flow | PASS, including fiscal settings, customer, issue, payment, PDF, e-invoice, SIF and evidence export |
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
