# API Map

This map lists the product APIs used by the frontend for the local/preprod fiscal SaaS flow.

| Resource | Method | Endpoint | Request | Response | Used by UI | Status |
|---|---|---|---|---|---|---|
| Health | GET | `/api/health` | none | service status | top status strip | active |
| Current user | GET | `/api/me` | demo header or bearer token | user and memberships | session context | active |
| Tenants | GET | `/api/tenants` | demo header or bearer token | tenant list | tenant switcher | active |
| Plans | GET | `/api/platform/plans` | platform/demo auth | plan list | SaaS plan panel | active |
| Platform tenants | GET | `/api/platform/tenants` | platform/demo auth | tenant admin list | SaaS governance panel | active |
| Companies | GET | `/api/tenants/{tenantId}/companies?search={text}` | `X-Tenant-Id` must match path | companies filtered by tenant and search | company search/list | active |
| Company detail | GET | `/api/tenants/{tenantId}/companies/{companyId}` | `X-Tenant-Id` must match path | company | company detail | active |
| Create company | POST | `/api/tenants/{tenantId}/companies` | `legalName`, `taxId`, `countryCode`, `relationshipType` | company | new company form | active |
| Update company | PATCH | `/api/tenants/{tenantId}/companies/{companyId}` | company patch fields | company | company edit form | active |
| Deactivate company | DELETE | `/api/tenants/{tenantId}/companies/{companyId}` | none | 204 | deactivate action | active |
| Fiscal settings | GET | `/api/tenants/{tenantId}/companies/{companyId}/fiscal-settings` | tenant auth | fiscal settings | fiscal config panel | active |
| Save fiscal settings | PUT | `/api/tenants/{tenantId}/companies/{companyId}/fiscal-settings` | fiscal identity, address, defaults | fiscal settings | fiscal config form | active |
| Invoice series | GET | `/api/tenants/{tenantId}/companies/{companyId}/invoice-series` | tenant auth | series list | series chips | active |
| Create invoice series | POST | `/api/tenants/{tenantId}/companies/{companyId}/invoice-series` | code, prefix, next number, padding | series | series form | active |
| Customers | GET | `/api/tenants/{tenantId}/companies/{companyId}/customers?search={text}` | tenant auth | customer list | customer selector | active |
| Create customer | POST | `/api/tenants/{tenantId}/companies/{companyId}/customers` | customer fiscal/contact/address fields | customer | customer form | active |
| Audit events | GET | `/api/tenants/{tenantId}/companies/{companyId}/audit-events` | tenant auth | hash-chained audit events | evidence panel | active |
| Evidence exports | GET | `/api/tenants/{tenantId}/companies/{companyId}/exports` | tenant auth | export list | evidence panel | active |
| Create evidence export | POST | `/api/tenants/{tenantId}/companies/{companyId}/exports/evidence-pack` | none | completed export job | evidence ZIP action | active |
| Download evidence export | GET | `/api/tenants/{tenantId}/companies/{companyId}/exports/{exportId}/download` | tenant auth | ZIP stream | evidence download | active |
| Relationships | GET | `/api/tenants/{tenantId}/business-relationships` | tenant auth | relationship list | tenant evidence | active |
| Documents | GET | `/api/tenants/{tenantId}/documents?companyId={companyId}` | tenant auth | document list | company documents | active |
| Upload document | POST | `/api/tenants/{tenantId}/documents` | multipart `companyId`, `documentType`, `title`, `file` | document | document upload | active |
| Document download | GET | `/api/tenants/{tenantId}/documents/{documentId}/download` | tenant auth | file stream | download action | active |
| Document events | GET | `/api/tenants/{tenantId}/documents/{documentId}/events` | tenant auth | audit events | document events | active |
| Invoices | GET | `/api/tenants/{tenantId}/invoices?companyId={companyId}&status={status}&search={text}` | tenant auth | invoice list | company invoices | active |
| Invoice detail | GET | `/api/tenants/{tenantId}/invoices/{invoiceId}` | tenant auth | invoice with lines and taxes | invoice selection | active |
| Create invoice | POST | `/api/tenants/{tenantId}/invoices` | issuer, customer, number, type, date, currency, lines | DRAFT invoice | invoice editor | active |
| Update invoice draft | PATCH | `/api/tenants/{tenantId}/invoices/{invoiceId}` | full invoice draft payload | invoice | draft edit form | active |
| Update invoice status | PATCH | `/api/tenants/{tenantId}/invoices/{invoiceId}/status` | `status` | invoice | issue action | active |
| Issue invoice | POST | `/api/tenants/{tenantId}/invoices/{invoiceId}/issue` | optional `seriesId`, `issueRequestId` | issued invoice with fiscal number | issue action | active |
| Create corrective | POST | `/api/tenants/{tenantId}/invoices/{invoiceId}/create-corrective` | none | corrective draft | corrective action | active |
| Cancel local invoice | POST | `/api/tenants/{tenantId}/invoices/{invoiceId}/cancel-local` | reason | cancelled local invoice | cancel action | active |
| Invoice PDF | GET | `/api/tenants/{tenantId}/invoices/{invoiceId}/pdf` | tenant auth | PDF stream plus SHA-256 header | PDF action | active |
| Invoice payments | GET | `/api/tenants/{tenantId}/invoices/{invoiceId}/payments` | tenant auth | payment list | payment history | active |
| Register payment | POST | `/api/tenants/{tenantId}/invoices/{invoiceId}/payments` | amount, date, method, reference | payment | payment form | active |
| E-invoices | GET | `/api/tenants/{tenantId}/einvoices` | tenant auth | e-invoice messages | invoice evidence panel | active |
| Generate e-invoice | POST | `/api/tenants/{tenantId}/einvoices` | `invoiceId`, `syntax` | e-invoice message | generate local UBL | active |
| SIF records | GET | `/api/tenants/{tenantId}/verifactu/records` | tenant auth | SIF record list | SIF evidence panel | active |
| Register SIF | POST | `/api/tenants/{tenantId}/verifactu/records` | `invoiceId` | SIF registration | register local SIF | active |
| SIF system declaration drafts | GET | `/api/tenants/{tenantId}/verifactu/system-declarations/drafts` | tenant auth | declaration drafts | governance hash evidence | active |

## Contract Rules

- Operational data is always scoped by tenant.
- The backend requires the path tenant and `X-Tenant-Id` header to match in demo mode.
- Companies, documents and invoices are never global UI objects.
- Documents are files associated with a company; they do not create fiscal invoices automatically.
- Invoices are created as `DRAFT`, can be edited only while `DRAFT`, and can then be issued with fiscal settings and an active invoice series.
- Issuing assigns a fiscal number, stores issuer/customer/totals snapshots and blocks free editing.
- Issued invoices can receive manual payments, generate PDF, create local corrective drafts or be cancelled locally.
- Evidence exports are local/preprod ZIPs. They do not prove production legal compliance.
- E-invoice and SIF generation require an `ISSUED` invoice and remain local/preprod.
