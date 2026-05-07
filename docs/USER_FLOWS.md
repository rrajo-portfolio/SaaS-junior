# User Flows

## Select Tenant

1. Open the frontend.
2. Review the active user and tenant switcher.
3. Select a tenant.
4. Confirm that company, document, invoice, e-invoice and SIF counts update for that tenant.

## Create And Search Company

1. Select a tenant.
2. Use the company search to find an existing company by name or tax ID.
3. Clear the search.
4. Create a company with legal name, tax ID, country and relationship type.
5. Open the company detail.
6. Edit basic company fields and save.
7. Switch tenant and confirm the company is not visible there.

## Upload Document

1. Select a tenant.
2. Open a company detail.
3. Go to the documents section.
4. Upload a file with a title and document type.
5. Confirm it appears in the company document list.
6. Use download or events to inspect stored evidence.

Documents are not invoices. To create a fiscal invoice, use the invoice section.

## Create, Edit And Issue Invoice

1. Select a tenant.
2. Open a company detail.
3. Complete fiscal settings and ensure the company has an active invoice series.
4. Create or select a customer recipient.
5. Go to the invoices section.
6. Create an invoice with issuer, customer, number, dates and at least one line.
7. Use discounts or withholding if needed. The backend recalculates totals.
8. Save the invoice as draft.
9. Edit line values while the invoice is still `DRAFT`.
10. Issue the invoice.
11. Confirm the fiscal number is assigned and the edit action is disabled after issue.

## Collect, Correct Or Cancel Invoice

1. Select an issued invoice.
2. Register a manual payment and confirm payment status changes.
3. Download the invoice PDF generated from immutable snapshots.
4. If the issued invoice has an error, create a corrective draft instead of editing the issued invoice.
5. If the invoice must be invalidated locally, use local cancellation.

Corrective and cancellation actions are local/preprod. They do not send legal records to AEAT.

## Generate Local E-invoice

1. Create and issue an invoice.
2. Select the issued invoice.
3. Go to the e-invoice section.
4. Generate local UBL e-invoice evidence.
5. Confirm status and payload hash are visible.

This flow is local/preprod. It does not send the invoice to an external provider.

## Register Local SIF

1. Create and issue an invoice.
2. Select the issued invoice.
3. Go to the SIF local section.
4. Register local SIF evidence.
5. Confirm sequence and hash are visible.

This flow is local/preprod. It is not a legal production Verifactu submission.

## Export Evidence Pack

1. Open a company detail.
2. Review recent audit events.
3. Generate an evidence pack.
4. Download the ZIP and inspect the README, invoice CSV, SIF JSON, audit JSON and checksum manifest.

Evidence packs are for local/preprod traceability. They do not certify production legal compliance.
