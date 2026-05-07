# Preprod Operator Readiness

Status: IMPLEMENTED_FOR_LOCAL_PREPROD. External shared preproduction still depends on real HTTPS hosting, OIDC and secret storage.

## Product SHOULD Items

| Item | Implementation | Validation |
|---|---|---|
| Explicit roles and permissions | Dashboard cards show effective user roles, capabilities and boundaries between fiscal operations, configuration and evidence. | Frontend unit and Playwright tests load the cards. |
| System status screen | Dashboard shows frontend, backend, MySQL-through-backend, proxy mode, environment, version and latest smoke marker. | `npm test -- --run`, `npm run e2e`. |
| Controlled demo seeds | Dashboard shows tenant, company, customer, invoice, payment and evidence coverage as demo-only data. | Loaded from current tenant data. |
| Better visual traceability | Invoice panel shows fiscal number, PDF hash captured from `X-Content-SHA256`, e-invoice hash, SIF hash, snapshots and artifact IDs. | Playwright asserts the PDF hash and local stub markers. |
| Company summary panel | Company detail shows issued/draft counts, paid total, outstanding amount, chart and latest audit hash. | Frontend unit and Playwright tests load the panel. |

## Product NICE_TO_HAVE Items

| Item | Implementation |
|---|---|
| Advanced filters | Existing invoice filter plus customer, document, payment and audit filters. |
| CSV/Excel-compatible export | Client-side CSV export for invoices, customers and audit evidence. Evidence ZIP remains the official local/preprod package. |
| Invoice timeline | Selected invoice timeline shows draft, issue, payment, e-invoice and SIF events. |
| Company timeline | Evidence panel shows audit and document events for the selected company. |
| PDF templates | Fiscal settings include `standard`, `compact` and `detailed` template selection. |
| UX improvements in long forms | Compact grouped controls, inline filters and status-aware notifications. |
| Global search | Dashboard global search scans loaded companies, customers, invoices and documents. |
| Graph dashboard | Company invoice status chart is rendered without adding chart dependencies. |
| Internal notifications | Company summary surfaces missing fiscal settings, missing series, missing customers, outstanding amount and unsafe demo auth for public preprod. |
| Tenant branding | Tenant identity preview shows configurable tenant name and initials. |

## Shared Preproduction Gate

The shared-preprod gate is automated by `scripts/preprod-public-readiness.ps1`.

Required external values are documented by name only in `docs/configuration.md`. The gate now checks:

- HTTPS public origin.
- Supported TLS mode.
- OIDC auth mode.
- External secret source.
- Demo-only data mode.
- Backup mode.
- Rollback reference.
- Smoke-test mode.
- `/healthz`.
- `/api/health`.

The gate returns `READY_FOR_PUBLIC_PREPROD` only when all required external conditions are present.

## Backup And Rollback Automation

| Script | Purpose |
|---|---|
| `scripts/preprod-smoke.ps1` | Runs HTTP health checks and optionally Playwright preprod smoke against a base URL. |
| `scripts/backup-compose-preprod.ps1` | Creates MySQL dump and document-storage artifact archive for Docker Compose preprod. |
| `scripts/backup-mysql-preprod.ps1` | Creates MySQL dump from Kubernetes preprod. |
| `scripts/restore-mysql-preprod.ps1` | Restores Kubernetes MySQL from a dump when explicitly confirmed. |
| `scripts/preprod-compose-snapshot.ps1` | Tags current Docker Compose backend/frontend/nginx images as a rollback reference. |
| `scripts/preprod-compose-rollback.ps1` | Retags a previous snapshot, restarts app services and runs smoke checks when explicitly confirmed. |

Backup outputs stay under `backups/`, which is ignored by Git.

## Not Done Yet

This does not turn the system into production. The following remain blocked until a real production phase:

- AEAT production dispatch.
- Official Verifactu/SIF certification claim.
- Real e-invoice provider delivery.
- Real signature/certificate workflow.
- Real customer data.
- Public URL without HTTPS and OIDC.
- Open public registration.
- Secrets committed to Git.
