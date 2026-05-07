# Local Preproduction

This project is a local/preprod fiscal SaaS. It is not production legal software and does not send data to AEAT or any external fiscal provider.

## Backend

Run from `backend/`:

```powershell
.\mvnw.cmd spring-boot:run
```

Validation:

```powershell
.\mvnw.cmd -DskipTests=false verify
```

The backend uses Flyway migrations for schema versioning. Do not create manual, unversioned database changes.

## Frontend

Run from `frontend/`:

```powershell
npm install
npm run dev -- --host 127.0.0.1 --port 5173
```

Validation:

```powershell
npm test
npm run build
npm run e2e
```

## Tenant Model

- A tenant is the SaaS customer/workspace.
- The UI sends the tenant in the URL and in `X-Tenant-Id` for demo mode.
- Switching tenant reloads companies, documents, invoices, e-invoices and SIF records.
- A company created in one tenant must not appear in another tenant.

## Fiscal Scope

- Documents are uploaded files linked to a company.
- Invoices are fiscal records created explicitly from the invoice editor.
- A company needs fiscal settings and an active invoice series before issuing invoices.
- Customer records are recipient records used for immutable invoice snapshots.
- Issued invoices can generate a local PDF, receive manual payments, create corrective drafts or be cancelled locally.
- Audit events and evidence exports are local/preprod traceability tools.
- E-invoice generation is local and only available after invoice issue.
- SIF/Verifactu registration is local and only available after invoice issue.
- No production certificate, real AEAT dispatch, legal certification or external provider is used.

## Configuration

Environment variable names and purposes are documented in `docs/configuration.md`. Real `.env` files, templates, secrets and credentials must not be committed.
