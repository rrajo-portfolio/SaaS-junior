# UI Map

The current frontend is a single React workspace with anchor sections rather than separate routed pages. The product navigation is still modeled around these logical routes.

| Logical route | Current section | Purpose |
|---|---|---|
| `/` | `#dashboard` | SaaS fiscal workspace, tenant context and status |
| `/tenants` | `#tenants` | Active tenant switcher and current session |
| `/companies` | `#companies` | Search, list and create companies |
| `/companies/:companyId` | company detail panel | Edit selected company and operate on its data |
| `/companies/:companyId/fiscal-settings` | fiscal config panel | Maintain issuer fiscal identity, default payment terms and local SIF mode |
| `/companies/:companyId/invoice-series` | series controls | Create and inspect active fiscal numbering series |
| `/companies/:companyId/customers` | `#customers` | Create/select invoice recipients with fiscal address snapshots |
| `/companies/:companyId/documents` | `#documents` | Upload, list, download and inspect document events |
| `/companies/:companyId/invoices` | `#invoices` | List, filter, create and edit invoices for the company |
| `/companies/:companyId/invoices/:invoiceId` | selected invoice in `#invoices` | Issue invoice, download PDF, register payments, cancel local or create corrective draft |
| `/companies/:companyId/e-invoice` | `#einvoice` | Generate and view local e-invoice evidence from selected invoice |
| `/companies/:companyId/sif` | `#sif` | Register and view local SIF/Verifactu evidence from selected invoice |
| `/companies/:companyId/evidence` | evidence operations panel | View audit events and generate/download local evidence packs |
| `/platform/tenants` | SaaS governance panel | View plan and tenant administration evidence |

## Screen Rules

- The active tenant is visible before operational data.
- A company must be selected before documents, invoices, e-invoice or SIF actions are meaningful.
- The company detail panel is the product center; invoices are not operated as isolated dashboard rows.
- Action buttons are state-aware: invoice edit is disabled after issue, and e-invoice/SIF actions are disabled until an invoice is issued.
- Issuing depends on fiscal settings and an active series. The UI exposes both before invoice actions.
- Customer records are separate from companies and are used to populate invoice recipient snapshots.
- Payment status is commercial state and does not change the immutable fiscal issue state.
- Local/preprod disclaimers are visible near e-invoice and SIF actions.
