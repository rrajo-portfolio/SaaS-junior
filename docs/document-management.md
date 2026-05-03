# Document Management

Phase 4 adds tenant-scoped fiscal document metadata, versioned binaries, SHA-256 checksums and append-only audit events.

## Endpoints

- `GET /api/tenants/{tenantId}/documents`
- `POST /api/tenants/{tenantId}/documents`
- `POST /api/tenants/{tenantId}/documents/{documentId}/versions`
- `GET /api/tenants/{tenantId}/documents/{documentId}/download`
- `GET /api/tenants/{tenantId}/documents/{documentId}/events`

Document uploads use `multipart/form-data` with:

- `companyId`
- `documentType`
- `title`
- `file`

## Storage

The current preproduction adapter stores binaries in a mounted filesystem volume and records a storage key in MySQL. Each uploaded version stores:

- original filename
- content type
- byte size
- SHA-256 checksum
- storage key
- uploader user id

The adapter is intentionally local/preprod only. S3-compatible MinIO can be wired later behind the same service boundary without changing the document API.

## Security

- Tenant path and `X-Tenant-Id` must match.
- Upload and version creation require a write role.
- Downloads return `Cache-Control: no-store`.
- Nginx already disables cache for `/api/documents`, `/api/fiscal`, `/api/verifactu`, `/api/einvoice` and the tenant e-invoice routes.
- Tests use synthetic data only.
