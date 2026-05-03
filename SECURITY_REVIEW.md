# Security Review

Review date: 2026-05-03

Scope: preproduction release candidate for the Fiscal SaaS repository, including backend, frontend, Docker, Jenkins, Kubernetes, Nginx, storage, fiscal evidence, e-invoice and local operational scripts.

## Result

Status: READY_FOR_GATEKEEPER

No repository-tracked secrets, environment files, private keys, kubeconfigs, certificates or database dumps were detected by the tracked-file policy check.

## Controls

| Area | Status | Evidence |
|---|---|---|
| Secrets | PASS | `.gitignore` blocks `.env`, `.env.*`, secret material, dumps, logs and backups. Runtime values are injected from local env, Jenkins runtime env or Kubernetes Secret. |
| Authentication boundary | PARTIAL | Local preprod uses header-based demo auth (`X-User-Email`, `X-Tenant-Id`). Keycloak/OIDC is documented as future production hardening and is not represented as completed production auth. |
| Tenant isolation | PASS | Tenant-scoped endpoints require path tenant and `X-Tenant-Id` match; repositories query by tenant id. |
| Authorization | PASS | Write paths are limited to platform/tenant/fiscal/accountant roles depending on module. Readonly/auditor roles are constrained by service checks. |
| Document storage | PASS | Files are tenant scoped, checksummed with SHA-256 and stored outside git. Nginx disables cache for private document APIs. |
| SIF/Verifactu evidence | PASS | Hash chain, QR payloads, export verification and AEAT production guard are implemented for preprod. |
| B2B e-invoice | PASS | UBL and Facturae payload adapters, status events and payment events are tenant scoped. |
| Nginx | PASS | Static cache limited to versioned assets; API, auth, documents, fiscal, Verifactu and e-invoice responses are no-store. Security headers and upload limit are configured. |
| Kubernetes | PASS | Namespace, probes, services, NodePort, optional Ingress and ingress/egress NetworkPolicies are present. |
| Jenkins | PASS | JCasC controller, generated job and Jenkinsfile validated with a successful Jenkins build. Docker socket use is local-only and documented as high risk. |
| Backups | PASS | `scripts/backup-mysql-preprod.ps1` creates local ignored MySQL dumps with `mysqldump --no-tablespaces`. |
| Logs | PASS | `scripts/collect-preprod-logs.ps1` collects local ignored pod logs. |
| Monitoring | PARTIAL | Actuator health, Kubernetes probes and `scripts/preprod-health-report.ps1` are present. Metrics, alerting and tracing are not implemented. |

## Residual Risks

- This release candidate is not a legal Verifactu certification.
- AEAT production transmission remains disabled by default.
- Header-based demo auth is suitable only for local/preproduction validation.
- TLS, cert-manager, external ingress controller, centralized monitoring, alerting and off-host backups are not completed.
- Jenkins mounts the Docker socket and must remain local-only unless redesigned with isolated agents.
