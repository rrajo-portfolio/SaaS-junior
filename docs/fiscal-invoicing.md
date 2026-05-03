# Fiscal Invoicing

Phase 5 adds base invoice modelling and deterministic tax calculations.

## Scope

- Tenant-scoped invoices.
- Issuer and customer companies must belong to the tenant.
- Invoice lines calculate taxable base, tax amount and line total.
- Invoice tax summaries are grouped by tax rate.
- Rectifying invoices can reference the invoice being rectified.
- Status transitions are stored on the invoice row.

## Out Of Scope

- Verifactu/SIF record generation.
- AEAT transmission.
- B2B e-invoice serialization through the phase 8 module.
- Legal certification of tax compliance.

Those concerns are handled by later phases.

## Endpoints

- `GET /api/tenants/{tenantId}/invoices`
- `GET /api/tenants/{tenantId}/invoices/{invoiceId}`
- `POST /api/tenants/{tenantId}/invoices`
- `PATCH /api/tenants/{tenantId}/invoices/{invoiceId}/status`
