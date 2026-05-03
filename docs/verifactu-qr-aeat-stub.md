# Verifactu QR and AEAT Stub

Phase 7 adds QR payload generation, local AEAT transmission attempts and reviewable system declaration drafts.

This phase does not certify legal compliance and does not send production traffic to AEAT.

## Scope

- Tenant-scoped QR payloads for existing SIF records.
- No-store SVG QR rendering for fiscal evidence screens.
- Local transmission attempt log for `STUB` and `SANDBOX` modes.
- Production transmission guard controlled by environment.
- Tenant-scoped system declaration drafts with SHA-256 payload hashes.

## Endpoints

- `GET /api/tenants/{tenantId}/verifactu/records/{recordId}/qr`
- `GET /api/tenants/{tenantId}/verifactu/records/{recordId}/qr.svg`
- `GET /api/tenants/{tenantId}/verifactu/records/{recordId}/transmissions`
- `POST /api/tenants/{tenantId}/verifactu/records/{recordId}/transmissions`
- `GET /api/tenants/{tenantId}/verifactu/system-declarations/drafts`
- `POST /api/tenants/{tenantId}/verifactu/system-declarations/drafts`

## Runtime Guards

`VERIFACTU_MODE` selects the default adapter mode when a request does not provide one.

`AEAT_PRODUCTION_ENABLED` must remain disabled unless a later validated phase adds real production credentials, endpoint validation and legal review. When production is disabled, a production transmission request returns a validation error and no external dispatch is attempted.

## Persistence

- `sif_qr_payloads` stores one deterministic payload per SIF record.
- `sif_transmission_attempts` stores request and response evidence for local attempts.
- `sif_system_declarations` stores draft declaration payloads and hashes.

No secrets, certificates, client credentials or production AEAT tokens are stored in the repository.
