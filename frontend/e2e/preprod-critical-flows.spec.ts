import { expect, test } from '@playwright/test'

const tenantId = '10000000-0000-0000-0000-000000000001'
const issuerCompanyId = '40000000-0000-0000-0000-000000000001'
const customerCompanyId = '40000000-0000-0000-0000-000000000002'
const headers = {
  'X-User-Email': 'ana.admin@fiscalsaas.local',
  'X-Tenant-Id': tenantId,
}

test('creates and renders fiscal invoice, e-invoice and SIF evidence', async ({ page, request }) => {
  const baseURL = process.env.PLAYWRIGHT_BASE_URL ?? 'http://127.0.0.1:18080'
  const apiRoot = `${baseURL.replace(/\/$/, '')}/api`
  const invoiceNumber = `E2E-${Date.now()}`
  const issueRequestId = `issue-${Date.now()}`

  const customersResponse = await request.get(`${apiRoot}/tenants/${tenantId}/companies/${issuerCompanyId}/customers`, {
    headers,
  })
  expect(customersResponse.ok()).toBeTruthy()
  const customers = await customersResponse.json()
  const customerId = customers[0]?.id
  expect(customerId).toBeTruthy()

  const invoiceResponse = await request.post(`${apiRoot}/tenants/${tenantId}/invoices`, {
    headers,
    data: {
      issuerCompanyId,
      customerCompanyId,
      customerId,
      invoiceNumber,
      invoiceType: 'ISSUED',
      issueDate: '2026-05-03',
      dueDate: '2026-06-03',
      currency: 'EUR',
      lines: [
        {
          description: 'Playwright E2E fiscal flow',
          quantity: 1,
          unitPrice: 210,
          taxRate: 21,
          discountPercent: 0,
          withholdingPercent: 0,
        },
      ],
    },
  })
  expect(invoiceResponse.ok()).toBeTruthy()
  const invoice = await invoiceResponse.json()

  const issuedResponse = await request.post(`${apiRoot}/tenants/${tenantId}/invoices/${invoice.id}/issue`, {
    headers,
    data: { issueRequestId },
  })
  expect(issuedResponse.ok()).toBeTruthy()
  const issuedInvoice = await issuedResponse.json()
  expect(issuedInvoice.fiscalNumber).toBeTruthy()

  const pdfResponse = await request.get(`${apiRoot}/tenants/${tenantId}/invoices/${invoice.id}/pdf`, {
    headers,
  })
  expect(pdfResponse.ok()).toBeTruthy()
  expect(pdfResponse.headers()['x-content-sha256']).toBeTruthy()

  const einvoiceResponse = await request.post(`${apiRoot}/tenants/${tenantId}/einvoices`, {
    headers,
    data: { invoiceId: invoice.id, syntax: 'UBL' },
  })
  expect(einvoiceResponse.ok()).toBeTruthy()

  const sifResponse = await request.post(`${apiRoot}/tenants/${tenantId}/verifactu/records`, {
    headers,
    data: { invoiceId: invoice.id },
  })
  expect(sifResponse.ok()).toBeTruthy()

  const declarationResponse = await request.post(`${apiRoot}/tenants/${tenantId}/verifactu/system-declarations/drafts`, {
    headers,
  })
  expect(declarationResponse.ok()).toBeTruthy()

  await page.goto('/')
  await page.getByRole('tab', { name: /Norte Asesores/ }).click()
  await page.getByRole('list', { name: 'Empresas del tenant' }).getByRole('button', { name: /Norte Asesores SL/ }).click()

  const invoiceRow = page.locator('.invoice-row', { hasText: issuedInvoice.fiscalNumber })
  await expect(invoiceRow).toBeVisible()
  await invoiceRow.getByRole('button', { name: 'Ver' }).click()
  await expect(page.getByRole('region', { name: 'Facturas por empresa' }).getByText(issuedInvoice.fiscalNumber).first()).toBeVisible()
  await expect(page.getByRole('region', { name: 'Factura electronica local' }).getByText('UBL').first()).toBeVisible()
  await expect(page.getByRole('region', { name: 'Registro SIF local' }).getByText('REGISTRATION').first()).toBeVisible()
  await expect(page.getByRole('region', { name: 'Auditoria y exportacion de evidencia' }).getByText('INVOICE_ISSUED').first()).toBeVisible()
})
