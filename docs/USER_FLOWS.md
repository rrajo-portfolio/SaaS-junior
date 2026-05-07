# User Flows

## Select Tenant

1. Open the frontend.
2. Review the active user, tenant switcher, tenant branding preview and role cards.
3. Select a tenant.
4. Confirm that company, document, invoice, e-invoice, SIF, demo seed and global-search data update for that tenant.

## Review Preproduction Status

1. Open the dashboard.
2. Check roles and permission boundaries.
3. Check frontend, backend, MySQL and proxy status.
4. Check the environment label, build version and smoke-test marker.
5. Check demo seed coverage and confirm the screen says no real data is required.
6. Use global search to find a company, customer, invoice, document or hash in the loaded tenant.

## Create And Search Company

1. Select a tenant.
2. Use the company search to find an existing company by name or tax ID.
3. Clear the search.
4. Create a company with legal name, tax ID, country and relationship type.
5. Open the company detail.
6. Edit basic company fields and save.
7. Review the company summary, invoice status chart, outstanding amount and internal notifications.
8. Switch tenant and confirm the company is not visible there.

## Upload Document

1. Select a tenant.
2. Open a company detail.
3. Go to the documents section.
4. Upload a file with a title and document type.
5. Confirm it appears in the company document list.
6. Filter documents by title, type or hash.
7. Use download or events to inspect stored evidence.

Documents are not invoices. To create a fiscal invoice, use the invoice section.

## Create, Edit And Issue Invoice

1. Select a tenant.
2. Open a company detail.
3. Complete fiscal settings and ensure the company has an active invoice series.
4. Select PDF template and local SIF mode.
5. Create or select a customer recipient.
6. Filter customers by name, tax ID or email if the list is long.
7. Go to the invoices section.
8. Create an invoice with issuer, customer, number, dates and at least one line.
9. Use discounts or withholding if needed. The backend recalculates totals.
10. Save the invoice as draft.
11. Edit line values while the invoice is still `DRAFT`.
12. Issue the invoice.
13. Confirm the fiscal number is assigned and the edit action is disabled after issue.
14. Review invoice traceability and timeline.

## Collect, Correct Or Cancel Invoice

1. Select an issued invoice.
2. Register a manual payment and confirm payment status changes.
3. Filter payment rows when there are many payment records.
4. Download the invoice PDF generated from immutable snapshots and confirm the PDF hash is visible.
5. If the issued invoice has an error, create a corrective draft instead of editing the issued invoice.
6. If the invoice must be invalidated locally, use local cancellation.

Corrective and cancellation actions are local/preprod. They do not send legal records to AEAT.

## Generate Local E-invoice

1. Create and issue an invoice.
2. Select the issued invoice.
3. Go to the e-invoice section.
4. Generate local UBL e-invoice evidence.
5. Confirm LOCAL STUB status, artifact ID and payload hash are visible.

This flow is local/preprod. It does not send the invoice to an external provider.

## Register Local SIF

1. Create and issue an invoice.
2. Select the issued invoice.
3. Go to the SIF local section.
4. Register local SIF evidence.
5. Confirm LOCAL STUB status, artifact ID, sequence and hash are visible.

This flow is local/preprod. It is not a legal production Verifactu submission.

## Export Evidence Pack

1. Open a company detail.
2. Review recent audit events.
3. Filter audit events by event, actor or hash.
4. Export an audit CSV for spreadsheet review if needed.
5. Generate an evidence pack.
6. Download the ZIP and inspect the README, invoice CSV, SIF JSON, audit JSON and checksum manifest.

Evidence packs are for local/preprod traceability. They do not certify production legal compliance.
