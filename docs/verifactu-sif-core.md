# Verifactu SIF Core

Phase 6 introduces an internal SIF evidence engine. It is not a certification of legal compliance and does not transmit data to AEAT.

## Scope

- Tenant-scoped SIF records for issued invoices.
- Append-only registration and cancellation records.
- Per-tenant sequence numbers.
- SHA-256 hash chain with previous hash and canonical payload.
- Event log for record creation, cancellation and export creation.
- JSON export batches for readable evidence.
- MySQL migration and Testcontainers coverage.

## Invariants

- Existing SIF records are not updated by the application API.
- A cancellation creates a later `CANCELLATION` record referencing the original `REGISTRATION` record.
- The first record for a tenant uses `GENESIS` as the previous hash.
- `recordHash = SHA-256(previousHash + "\n" + canonicalPayload)`.
- Only `ISSUED` invoices can be registered.
- Production AEAT transmission remains disabled and out of scope.

## Endpoints

- `GET /api/tenants/{tenantId}/verifactu/records`
- `POST /api/tenants/{tenantId}/verifactu/records`
- `GET /api/tenants/{tenantId}/verifactu/records/verify`
- `PATCH /api/tenants/{tenantId}/verifactu/records/{recordId}/cancel`
- `GET /api/tenants/{tenantId}/verifactu/records/{recordId}/events`
- `GET /api/tenants/{tenantId}/verifactu/exports`
- `POST /api/tenants/{tenantId}/verifactu/exports`
- `GET /api/tenants/{tenantId}/verifactu/exports/{exportId}`
