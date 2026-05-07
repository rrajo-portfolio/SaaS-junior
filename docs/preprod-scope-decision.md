# Preproduction Scope Decision

Status: FUNCTIONAL_SCOPE_COMPLETE

NO_MORE_ESSENTIAL_PRODUCT_FEATURES

The functional MVP is complete for local/preproduction review. The remaining work is preproduction infrastructure, exposure control and operational hardening, not product feature implementation.

## Covered Product Scope

- Tenant and company isolation.
- Company search and management.
- Company fiscal settings.
- Fiscal invoice series.
- Customer records per company.
- Draft invoice create and edit flow.
- Fiscal issue flow with transactional fiscal number, snapshots and idempotency key.
- Local cancellation and corrective draft flow.
- Manual payments and outstanding balance.
- Local PDF artifact with content hash.
- Audit events and evidence ZIP export.
- Secure document upload checks.
- Local e-invoice and SIF/Verifactu stubs with no real legal dispatch.
- Frontend workflows for the above.
- API, UI, user-flow, configuration and demo documentation.

## Non-Blocking Improvements

SHOULD:

- Add seeded demo data reset automation for repeatable reviewer sessions.
- Add longer Playwright journeys for corrective invoice and local cancellation.
- Add downloadable audit report summaries outside the ZIP package.
- Add read-only/auditor focused UI filtering.

NICE_TO_HAVE:

- OCR-assisted invoice extraction.
- Facturae/UBL XSD validation against official schemas.
- Real payment provider integration.
- Real AEAT/Verifactu provider integration.
- Advanced tenant billing automation.

## Next Phase

The correct next phase is FASE PREPRODUCCION.

The application must not be exposed to external reviewers in demo-header mode. Shared preproduction requires OIDC authentication, TLS, a controlled public origin, backup/restore evidence and smoke-test evidence.
