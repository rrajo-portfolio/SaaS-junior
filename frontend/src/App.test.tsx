import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import App from './App'

const tenantId = '10000000-0000-0000-0000-000000000001'
const issuer = {
  id: '40000000-0000-0000-0000-000000000001',
  tenantId,
  legalName: 'Norte Asesores SL',
  taxId: 'B12345678',
  countryCode: 'ES',
  relationshipType: 'OWNER',
  status: 'ACTIVE',
}
const customer = {
  id: '40000000-0000-0000-0000-000000000002',
  tenantId,
  legalName: 'Alba Retail Group SL',
  taxId: 'B87654321',
  countryCode: 'ES',
  relationshipType: 'CLIENT',
  status: 'ACTIVE',
}

describe('App', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('renders the functional fiscal SaaS shell offline', () => {
    vi.stubGlobal('fetch', vi.fn(() => Promise.reject(new Error('offline'))))

    render(<App />)

    expect(screen.getByRole('heading', { name: /saas fiscal operativo/i })).toBeInTheDocument()
    expect(screen.getByRole('searchbox', { name: /buscar empresa/i })).toBeInTheDocument()
    expect(screen.getByText(/selecciona o crea una empresa/i)).toBeInTheDocument()
  })

  it('shows tenant company detail and invoice actions when the API responds', async () => {
    vi.stubGlobal('fetch', vi.fn(apiMock))

    render(<App />)

    await waitFor(() => expect(screen.getAllByText('Operativo').length).toBeGreaterThan(0))
    await waitFor(() => expect(screen.getAllByText('Norte Asesores SL').length).toBeGreaterThan(0))

    expect(screen.getByRole('region', { name: 'Documentos por empresa' })).toBeInTheDocument()
    expect(screen.getByRole('region', { name: 'Facturas por empresa' })).toBeInTheDocument()
    expect(screen.getByRole('region', { name: 'Factura electronica local' })).toBeInTheDocument()
    expect(screen.getByRole('region', { name: 'Registro SIF local' })).toBeInTheDocument()

    fireEvent.change(screen.getByRole('searchbox', { name: /buscar empresa/i }), { target: { value: 'Alba' } })

    await waitFor(() => expect(screen.getAllByText('Alba Retail Group SL').length).toBeGreaterThan(0))
    expect(fetch).toHaveBeenCalledWith(expect.stringContaining('/companies?search=Alba'), expect.anything())
  })
})

function apiMock(input: RequestInfo | URL, init?: RequestInit) {
  const url = input.toString()
  const method = init?.method ?? 'GET'
  const payload = responseFor(url, method)

  return Promise.resolve({
    ok: true,
    blob: () => Promise.resolve(new Blob(['demo'])),
    json: () => Promise.resolve(payload),
  })
}

function responseFor(url: string, method: string) {
  if (url.endsWith('/health')) {
    return { status: 'ok', service: 'fiscal-saas-backend', checkedAt: '2026-05-07T10:00:00Z' }
  }
  if (url.endsWith('/me')) {
    return {
      user: {
        id: '20000000-0000-0000-0000-000000000001',
        email: 'ana.admin@fiscalsaas.local',
        displayName: 'Ana Admin',
        roles: ['platform_admin'],
      },
      memberships: [],
    }
  }
  if (url.endsWith('/tenants')) {
    return [{ id: tenantId, slug: 'norte-asesores', name: 'Norte Asesores', role: 'platform_admin' }]
  }
  if (url.endsWith('/platform/plans')) {
    return [{
      code: 'starter',
      displayName: 'Starter',
      status: 'ACTIVE',
      monthlyPriceCents: 2900,
      currency: 'EUR',
      maxUsers: 3,
      maxDocuments: 250,
      maxInvoices: 100,
      includesVerifactu: true,
      includesEinvoice: true,
    }]
  }
  if (url.endsWith('/platform/tenants')) {
    return [{
      id: tenantId,
      slug: 'norte-asesores',
      displayName: 'Norte Asesores',
      status: 'ACTIVE',
      planCode: 'starter',
      subscriptionStatus: 'trialing',
    }]
  }
  if (url.includes('/business-relationships')) {
    return [{
      id: '50000000-0000-0000-0000-000000000001',
      tenantId,
      sourceCompany: issuer,
      targetCompany: customer,
      relationshipKind: 'CLIENT_MANAGEMENT',
      status: 'ACTIVE',
      notes: 'Gestion fiscal y documental recurrente',
      startsAt: '2026-01-01',
    }]
  }
  if (url.includes('/documents')) {
    if (method === 'POST') {
      return documentPayload('doc-created', customer)
    }
    return [documentPayload('doc-1', issuer)]
  }
  if (url.includes('/invoices')) {
    return [invoicePayload()]
  }
  if (url.includes('/einvoices')) {
    return [einvoicePayload()]
  }
  if (url.includes('/verifactu/records')) {
    return [sifPayload()]
  }
  if (url.includes('/verifactu/system-declarations/drafts')) {
    return [{
      id: '81000000-0000-0000-0000-000000000001',
      tenantId,
      status: 'DRAFT',
      payload: '{"certified":false}',
      payloadSha256: 'abcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcd',
      createdAt: '2026-05-07T10:00:00Z',
    }]
  }
  if (url.includes('/fiscal-settings')) {
    return {
      id: 'settings-1',
      companyId: issuer.id,
      legalName: issuer.legalName,
      tradeName: issuer.legalName,
      nif: issuer.taxId,
      vatNumber: issuer.taxId,
      addressLine1: 'Calle Fiscal 1',
      city: 'Madrid',
      province: 'Madrid',
      postalCode: '28001',
      country: 'ES',
      defaultCurrency: 'EUR',
      defaultPaymentTermsDays: 30,
      defaultVatRate: 21,
      defaultLanguage: 'es',
      pdfTemplate: 'standard',
      sifMode: 'LOCAL_ONLY',
      verifactuLabelEnabled: false,
    }
  }
  if (url.includes('/invoice-series')) {
    return [{
      id: 'series-1',
      companyId: issuer.id,
      code: '2026',
      prefix: 'F-2026-',
      nextNumber: 2,
      padding: 6,
      active: true,
    }]
  }
  if (url.includes('/customers')) {
    return [{
      id: 'customer-1',
      companyId: issuer.id,
      customerType: 'COMPANY',
      name: 'Cliente Demo SL',
      nif: 'B11223344',
      email: 'cliente@example.local',
      addressLine1: 'Calle Cliente 1',
      city: 'Madrid',
      province: 'Madrid',
      postalCode: '28002',
      country: 'ES',
      status: 'ACTIVE',
    }]
  }
  if (url.includes('/audit-events')) {
    return [{
      id: 'audit-1',
      companyId: issuer.id,
      actorEmail: 'ana.admin@fiscalsaas.local',
      eventType: 'INVOICE_CREATED',
      entityType: 'INVOICE',
      entityId: '70000000-0000-0000-0000-000000000001',
      eventHash: 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa',
      occurredAt: '2026-05-07T10:00:00Z',
    }]
  }
  if (url.includes('/exports')) {
    return []
  }
  if (url.includes('/companies')) {
    const companies = [issuer, customer]
    return url.includes('search=Alba') ? [customer] : companies
  }
  return []
}

function documentPayload(id: string, company = issuer) {
  return {
    id,
    tenantId,
    company,
    documentType: 'INVOICE_RECEIVED',
    title: 'Factura recibida demo',
    status: 'ACTIVE',
    currentVersion: 1,
    latestSha256: 'abc123abc123abc123abc123abc123abc123abc123abc123abc123abc123abcd',
    latestByteSize: 2048,
    latestFilename: 'factura.txt',
    updatedAt: '2026-05-07T10:00:00Z',
  }
}

function invoicePayload() {
  return {
    id: '70000000-0000-0000-0000-000000000001',
    tenantId,
    issuerCompany: issuer,
    customerCompany: customer,
    invoiceNumber: 'F2026-0001',
    invoiceType: 'ISSUED',
    status: 'DRAFT',
    issueDate: '2026-05-07',
    currency: 'EUR',
    taxableBase: 100,
    taxTotal: 21,
    total: 121,
    lines: [{
      lineNumber: 1,
      description: 'Servicio demo',
      quantity: 1,
      unitPrice: 100,
      taxRate: 21,
      lineBase: 100,
      taxAmount: 21,
      lineTotal: 121,
    }],
    taxes: [],
  }
}

function einvoicePayload() {
  return {
    id: '82000000-0000-0000-0000-000000000001',
    tenantId,
    invoiceId: '70000000-0000-0000-0000-000000000001',
    invoiceNumber: 'F2026-0001',
    issuerLegalName: issuer.legalName,
    customerLegalName: customer.legalName,
    syntax: 'UBL',
    direction: 'OUTBOUND',
    exchangeStatus: 'GENERATED',
    commercialStatus: 'PENDING',
    paymentStatus: 'UNPAID',
    payloadSha256: 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb',
    createdAt: '2026-05-07T10:00:00Z',
    updatedAt: '2026-05-07T10:00:00Z',
  }
}

function sifPayload() {
  return {
    id: '80000000-0000-0000-0000-000000000001',
    tenantId,
    invoiceId: '70000000-0000-0000-0000-000000000001',
    invoiceNumber: 'F2026-0001',
    recordType: 'REGISTRATION',
    sequenceNumber: 1,
    previousHash: 'GENESIS',
    recordHash: '1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef',
    canonicalPayload: '{"recordType":"REGISTRATION"}',
    systemVersion: '0.1.0',
    normativeVersion: 'CURRENT_AS_OF_2026_05_03',
    createdAt: '2026-05-07T10:00:00Z',
  }
}
