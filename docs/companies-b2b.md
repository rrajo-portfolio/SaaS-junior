# Companies And B2B Relationships

Phase 3 adds tenant-scoped company management for owners, clients and suppliers.

## Scope

- `GET /api/tenants/{tenantId}/companies`
- `POST /api/tenants/{tenantId}/companies`
- `GET /api/tenants/{tenantId}/companies/{companyId}`
- `PATCH /api/tenants/{tenantId}/companies/{companyId}`
- `DELETE /api/tenants/{tenantId}/companies/{companyId}`
- `GET /api/tenants/{tenantId}/business-relationships`
- `POST /api/tenants/{tenantId}/business-relationships`
- `PATCH /api/tenants/{tenantId}/business-relationships/{relationshipId}`
- `DELETE /api/tenants/{tenantId}/business-relationships/{relationshipId}`

Deletes are soft deactivations. Fiscal history is not physically removed.

## Tenant Guard

Every tenant-scoped endpoint requires:

- `X-User-Email`
- `X-Tenant-Id`

The path tenant and `X-Tenant-Id` must match. The authenticated user must be an active member of the tenant.

## Write Roles

Write access is limited to:

- `platform_admin`
- `tenant_admin`
- `fiscal_manager`
- `accountant`

Auditor, readonly and client users can read but cannot mutate company data.

## Tax Identifier Validation

The backend applies basic validation only:

- Spanish `ES` identifiers accept NIF, NIE and CIF-like formats.
- Non-Spanish identifiers must look like a VAT value prefixed by the country code.

This is not a legal or fiscal certification. Stricter checksum and registry validation can be added in a later compliance hardening phase.
