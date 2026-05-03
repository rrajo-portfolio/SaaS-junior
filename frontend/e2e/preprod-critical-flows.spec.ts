import { expect, test } from '@playwright/test'

const tenantId = '10000000-0000-0000-0000-000000000001'
const headers = {
  'X-User-Email': 'ana.admin@fiscalsaas.local',
  'X-Tenant-Id': tenantId,
}

test('creates and renders fiscal invoice, e-invoice and SIF evidence', async ({ page, request }) => {
  const baseURL = process.env.PLAYWRIGHT_BASE_URL ?? 'http://127.0.0.1:18080'
  const apiRoot = `${baseURL.replace(/\/$/, '')}/api`
  const invoiceNumber = `E2E-${Date.now()}`

  const invoiceResponse = await request.post(`${apiRoot}/tenants/${tenantId}/invoices`, {
    headers,
    data: {
      issuerCompanyId: '40000000-0000-0000-0000-000000000001',
      customerCompanyId: '40000000-0000-0000-0000-000000000002',
      invoiceNumber,
      invoiceType: 'ISSUED',
      issueDate: '2026-05-03',
      currency: 'EUR',
      lines: [
        {
          description: 'Playwright E2E fiscal flow',
          quantity: 1,
          unitPrice: 210,
          taxRate: 21,
        },
      ],
    },
  })
  expect(invoiceResponse.ok()).toBeTruthy()
  const invoice = await invoiceResponse.json()

  const issuedResponse = await request.patch(`${apiRoot}/tenants/${tenantId}/invoices/${invoice.id}/status`, {
    headers,
    data: { status: 'ISSUED' },
  })
  expect(issuedResponse.ok()).toBeTruthy()

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

  await expect(page.getByRole('region', { name: 'Facturacion fiscal' }).getByText(invoiceNumber)).toBeVisible()
  await expect(page.getByRole('region', { name: 'Factura electronica B2B' }).getByText(invoiceNumber)).toBeVisible()
  await expect(page.getByRole('region', { name: 'Verifactu SIF' }).getByText(invoiceNumber)).toBeVisible()
  await expect(page.getByRole('region', { name: 'Factura electronica B2B' }).getByText('UBL').first()).toBeVisible()
})
