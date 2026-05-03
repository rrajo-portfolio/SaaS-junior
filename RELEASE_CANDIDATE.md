# Release Candidate

RC date: 2026-05-03

Status: READY_FOR_GATEKEEPER

## Scope

This release candidate includes:

- Spring Boot backend with Flyway migrations V1-V8.
- React frontend operational dashboard.
- Tenant identity, companies/B2B, fiscal documents, invoicing, Verifactu/SIF, QR/AEAT stub and B2B e-invoice flows.
- Docker local/preprod runtime.
- Jenkins local CI/CD controller with JCasC.
- kind Kubernetes preprod with Nginx reverse proxy/cache.
- Preprod backup, log and health scripts.

## Validation Evidence

| Check | Result |
|---|---|
| Backend `mvnw -DskipTests=false verify` | PASS |
| Frontend lint | PASS |
| Frontend unit tests | PASS |
| Frontend build | PASS |
| Local Playwright | PASS |
| Docker Compose local/preprod config | PASS |
| Jenkins controller bootstrap | PASS |
| Jenkins pipeline build #8 | PASS |
| Kubernetes node/pod readiness | PASS |
| Kubernetes smoke script | PASS |
| Preprod Playwright critical flows | PASS |
| Nginx syntax and cache headers | PASS |
| Preprod health report script | PASS |
| Preprod log collection script | PASS |
| Preprod MySQL backup script | PASS |
| Forbidden tracked file check | PASS |

## Known Exclusions

- No production deployment.
- No production AEAT dispatch.
- No legal Verifactu certification.
- No committed secrets, env files, kubeconfig, certs or database dumps.
- No SonarQube runtime.
- No Keycloak/OIDC runtime.
- No TLS/cert-manager.
- No centralized monitoring, alerting or tracing.

## Release Notes

The RC is suitable for local preproduction demonstration and gatekeeper review. It is not suitable for production without the excluded controls.
