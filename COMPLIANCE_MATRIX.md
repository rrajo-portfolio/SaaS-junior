# Compliance Matrix

Review date: 2026-05-04

| Requirement | Status | Evidence |
|---|---|---|
| No tracked local orchestration files | PASS | Forbidden-file git check returned no tracked matches. |
| No tracked `.env` or env templates | PASS | `.env`, `.env.*`, `.env.example`, `.env.sample`, `.env.template` are ignored and untracked. |
| Variables documented without values | PASS | `docs/configuration.md` lists names and purposes only. |
| Dockerfiles created from scratch | PASS | Backend, frontend, MySQL, Nginx and Jenkins controller Dockerfiles exist under `infra/docker` and `infra/jenkins`. |
| Local Docker Compose | PASS | `infra/docker/docker-compose.local.yml` validates. |
| Preprod Docker Compose | PASS | `infra/docker/docker-compose.preprod.yml` validates with a temporary local env. |
| Kubernetes preprod | PASS | kind cluster `fiscal-saas-preprod`, namespace, workloads, services, NetworkPolicies and optional Ingress are present. |
| MySQL migrations | PASS | Flyway migrations V1 through V9 validated in backend tests and MySQL Testcontainers. |
| Jenkins from scratch | PASS | `infra/jenkins/docker-compose.jenkins.yml`, controller Dockerfile, plugins, JCasC and Jenkinsfile exist. Jenkinsfile validates backend, frontend, Playwright, Docker Compose, Keycloak and observability configuration. |
| SonarQube | NOT_INCLUDED | No SonarQube service or token is committed. Future phase required before marking PASS. |
| Keycloak/OIDC | PREPROD_FOUNDATION | Local Keycloak compose, importable realm, public PKCE frontend client and backend resource-server mode are implemented without committed secrets. |
| Nginx proxy/cache | PASS | `infra/nginx/nginx.conf` enforces no-store API and immutable cache for versioned assets only. |
| Verifactu/SIF | PREPROD_PASS | SIF hash chain, QR, AEAT stub/guard and declaration draft flows exist. Not legal certification. |
| B2B e-invoice | PREPROD_PASS | UBL/Facturae generation, status and payment evidence are implemented. |
| RGPD/data protection | PARTIAL | Tenant isolation, role checks, no secret commits and no-store private APIs exist. Formal DPA, retention automation and data subject workflows are not implemented. |
| Backups | PREPROD_PASS | Local ignored MySQL backup script exists and was validated. Off-host backup policy is not implemented. |
| Logs | PREPROD_PASS | Local ignored pod log collection script exists and was validated. Centralized logging is not implemented. |
| Monitoring | PREPROD_FOUNDATION | Actuator health, Prometheus metrics endpoint, local Prometheus compose and health report metrics check exist. Alerts and long-term retention are not implemented. |

## Gate Decision

Preproduction release candidate is ready for gatekeeper review with explicit exclusions for legal certification, SonarQube, public TLS ingress, centralized observability, billing provider integration and off-host backups.
