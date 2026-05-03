# B2B E-Invoice

Phase 8 adds tenant-scoped B2B e-invoice preparation workflows.

This module serializes internal invoice data into UBL or Facturae-oriented XML payloads for development and review. It does not assert official interoperability with the public solution or private platforms.

## Scope

- One e-invoice message per issued fiscal invoice.
- Adapter pattern for `UBL` and `FACTURAE` payload generation.
- Exchange status tracking: generated, sent, received.
- Commercial status tracking: pending, accepted, rejected.
- Payment status events: partial payment and full payment.
- Append-only audit events for message generation and status changes.
- No-store XML payload download.

## Endpoints

- `GET /api/tenants/{tenantId}/einvoices`
- `POST /api/tenants/{tenantId}/einvoices`
- `GET /api/tenants/{tenantId}/einvoices/{messageId}`
- `GET /api/tenants/{tenantId}/einvoices/{messageId}/payload`
- `PATCH /api/tenants/{tenantId}/einvoices/{messageId}/status`
- `GET /api/tenants/{tenantId}/einvoices/{messageId}/events`
- `GET /api/tenants/{tenantId}/einvoices/{messageId}/payment-events`
- `POST /api/tenants/{tenantId}/einvoices/{messageId}/payment-events`

## Guards

- Messages can only be created from invoices in `ISSUED` status.
- Payload responses are marked no-store.
- Tenant and role checks match the fiscal invoice and Verifactu modules.
- The repository stores no certificates, signing keys, platform credentials or public-solution tokens.

## Current Limits

- Payloads are preparation drafts and must pass official conformance testing before production use.
- No external B2B platform dispatch is implemented in this phase.
- No electronic signature workflow is implemented in this phase.
