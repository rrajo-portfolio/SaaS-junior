import { expect, type Page, type Route, test } from '@playwright/test'

const tenantNorte = '10000000-0000-0000-0000-000000000001'
const tenantCobalto = '10000000-0000-0000-0000-000000000002'
const owner = company('40000000-0000-0000-0000-000000000001', tenantNorte, 'Norte Asesores SL', 'B12345678', 'OWNER')
const alba = company('40000000-0000-0000-0000-000000000002', tenantNorte, 'Alba Retail Group SL', 'B87654321', 'CLIENT')
const cobalto = company('40000000-0000-0000-0000-000000000003', tenantCobalto, 'Cobalto Industrial SA', 'B22222222', 'OWNER')

test('runs the local SaaS fiscal flow end to end', async ({ page }) => {
  const state = createApiState()
  await mockApi(page, state)

  await page.goto('/')
  await expect(page).toHaveTitle(/Fiscal SaaS/)
  await expect(page.getByRole('heading', { name: 'SaaS fiscal operativo' })).toBeVisible()
  await expect(page.getByRole('region', { name: 'Facturas por empresa' })).toBeVisible()
  await expect(page.getByRole('region', { name: 'Control de preproduccion' })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Separacion operativa' })).toBeVisible()
  await expect(page.getByRole('region', { name: 'Estado del sistema' })).toBeVisible()
  await expect(page.getByText('Datos demo controlados')).toBeVisible()
  await expect(page.getByRole('region', { name: 'Resumen operativo de empresa' })).toBeVisible()

  await page.getByRole('searchbox', { name: 'Buscar en tenant' }).fill('Alba')
  await expect(page.getByRole('button', { name: /Empresa Alba Retail Group SL/ })).toBeVisible()
  await page.getByRole('searchbox', { name: 'Buscar en tenant' }).fill('')

  await page.getByRole('searchbox', { name: 'Buscar empresa' }).fill('Alba')
  await expect(page.getByRole('button', { name: /Alba Retail Group SL/ })).toBeVisible()
  await expect(page.getByRole('button', { name: /Norte Asesores SL/ })).toHaveCount(0)
  await page.getByRole('searchbox', { name: 'Buscar empresa' }).fill('')

  await page.getByLabel('Nombre legal').first().fill('Playwright Cliente SL')
  await page.getByLabel('NIF/CIF').first().fill('B44556677')
  await page.getByRole('button', { name: 'Crear', exact: true }).click()
  await expect(page.getByRole('heading', { name: 'Playwright Cliente SL' })).toBeVisible()

  await page.getByLabel('Direccion fiscal').fill('Calle Playwright 1')
  await page.getByLabel('Ciudad', { exact: true }).fill('Madrid')
  await page.getByLabel('Provincia', { exact: true }).fill('Madrid')
  await page.getByLabel('Codigo postal').fill('28001')
  await page.getByRole('button', { name: 'Guardar fiscalidad' }).click()
  await expect(page.getByText('Configuracion fiscal guardada')).toBeVisible()

  await page.getByRole('textbox', { name: 'Cliente', exact: true }).fill('Cliente Final QA SL')
  await page.getByLabel('NIF cliente').fill('B77889900')
  await page.getByLabel('Email cliente').fill('qa@example.local')
  await page.getByLabel('Direccion cliente').fill('Avenida Test 2')
  await page.getByLabel('Ciudad cliente').fill('Madrid')
  await page.getByLabel('CP cliente').fill('28002')
  await page.getByRole('button', { name: 'Crear cliente' }).click()
  await expect(page.getByText('Cliente creado y seleccionado')).toBeVisible()
  await page.getByRole('searchbox', { name: 'Buscar cliente' }).fill('Final QA')
  await expect(page.getByRole('button', { name: /Cliente Final QA SL/ })).toBeVisible()
  await page.getByRole('searchbox', { name: 'Buscar cliente' }).fill('')

  const invoiceNumber = `PW-${Date.now()}`
  await page.getByLabel('Numero').fill(invoiceNumber)
  await page.getByLabel('Descripcion').fill('Servicio Playwright')
  await page.getByLabel('Cantidad').fill('1')
  await page.getByLabel('Precio').fill('100')
  await page.getByLabel('IVA %').fill('21')
  await page.getByLabel('Dto %').fill('0')
  await page.getByLabel('Ret %').fill('0')
  await expect(page.getByRole('region', { name: 'Facturas por empresa' }).getByText('121,00')).toBeVisible()
  await page.getByRole('button', { name: 'Guardar borrador' }).click()
  await expect(page.getByText('Factura borrador creada')).toBeVisible()
  await expect(page.getByText(invoiceNumber).first()).toBeVisible()

  await page.getByRole('button', { name: 'Editar' }).click()
  await page.getByLabel('Precio').fill('150')
  await page.getByRole('button', { name: 'Guardar borrador' }).click()
  await expect(page.getByText('Borrador actualizado')).toBeVisible()
  await expect(page.getByRole('region', { name: 'Facturas por empresa' }).getByText('181,50')).toBeVisible()

  await page.getByRole('button', { name: 'Emitir' }).click()
  await expect(page.getByText('Factura emitida', { exact: true })).toBeVisible()
  await expect(page.getByRole('region', { name: 'Facturas por empresa' }).getByText(/F-2026-/).first()).toBeVisible()
  await expect(page.getByRole('region', { name: 'Facturas por empresa' }).locator('.invoice-row .badge').filter({ hasText: 'ISSUED' })).toBeVisible()
  await expect(page.getByRole('region', { name: 'Facturas por empresa' }).getByRole('button', { name: 'Editar' })).toBeDisabled()

  await page.getByLabel('Pago', { exact: true }).fill('181.5')
  await page.getByRole('button', { name: 'Registrar pago' }).click()
  await expect(page.locator('.form-message').filter({ hasText: 'Pago registrado' })).toBeVisible()

  await page.getByRole('button', { name: 'PDF' }).click()
  await expect(page.getByText('PDF descargado')).toBeVisible()
  await expect(page.getByText(/PDF hash/)).toBeVisible()
  await expect(page.getByText(/cccccccccccc/)).toBeVisible()

  await page.getByRole('region', { name: 'Factura electronica local' }).getByRole('button', { name: 'Generar' }).click()
  await expect(page.getByText('E-invoice local generada')).toBeVisible()
  await expect(page.getByRole('region', { name: 'Factura electronica local' }).getByText('GENERATED')).toBeVisible()
  await expect(page.getByRole('region', { name: 'Factura electronica local' }).getByText(/LOCAL STUB/)).toBeVisible()

  await page.getByRole('region', { name: 'Registro SIF local' }).getByRole('button', { name: 'Registrar' }).click()
  await expect(page.getByText('Registro SIF local creado')).toBeVisible()
  await expect(page.getByRole('region', { name: 'Registro SIF local' }).getByText(/Secuencia/)).toBeVisible()
  await expect(page.getByRole('region', { name: 'Timeline de factura' })).toBeVisible()

  await page.getByRole('button', { name: 'Generar ZIP' }).click()
  await expect(page.getByText('Paquete de evidencia generado')).toBeVisible()

  await page.getByRole('tab', { name: /Cobalto Industrial/ }).click()
  await expect(page.getByRole('button', { name: /Playwright Cliente SL/ })).toHaveCount(0)
  await expect(page.getByRole('button', { name: /Cobalto Industrial SA/ })).toBeVisible()
})

test('matches the complete app guide sections and controls', async ({ page }) => {
  const state = createApiState()
  state.documents[tenantNorte].push(documentSummary('doc-guide-1', tenantNorte, owner))
  state.auditEvents[owner.id] = [auditEvent('audit-guide-1', tenantNorte, owner.id, 'GUIDE_TRACE_CREATED')]

  await mockApi(page, state)
  await page.goto('/')

  await expect(page).toHaveTitle(/Fiscal SaaS/)
  await expect(page.getByRole('heading', { name: 'SaaS fiscal operativo' })).toBeVisible()

  const navigation = page.getByLabel('Navegacion principal')
  await expect(navigation.getByRole('link', { name: 'Panel' })).toHaveAttribute('href', '#dashboard')
  await expect(navigation.getByRole('link', { name: 'Empresas' })).toHaveAttribute('href', '#companies')
  await expect(navigation.getByRole('link', { name: 'Documentos' })).toHaveAttribute('href', '#documents')
  await expect(navigation.getByRole('link', { name: 'Facturas' })).toHaveAttribute('href', '#invoices')
  await expect(navigation.getByRole('link', { name: 'E-invoice' })).toHaveAttribute('href', '#einvoice')
  await expect(navigation.getByRole('link', { name: 'SIF local' })).toHaveAttribute('href', '#sif')

  await expect(page.getByRole('searchbox', { name: 'Buscar empresa' })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Actualizar' })).toBeVisible()
  await expect(page.getByRole('region', { name: 'Estado de servicios' })).toContainText('fiscal-saas-backend')
  await expect(page.getByRole('region', { name: 'Contexto de tenant' })).toContainText('Norte Asesores')
  await expect(page.getByLabel('Branding configurable por tenant')).toBeVisible()
  await expect(page.getByRole('tab', { name: /Norte Asesores/ })).toHaveAttribute('aria-selected', 'true')

  await expect(page.getByRole('region', { name: 'Control de preproduccion' })).toContainText('Separacion operativa')
  await expect(page.getByRole('region', { name: 'Estado del sistema' })).toContainText('Ultimo smoke')
  await expect(page.getByText('Datos demo controlados')).toBeVisible()
  await expect(page.getByText('Buscador global')).toBeVisible()
  await page.getByRole('searchbox', { name: 'Buscar en tenant' }).fill('Alba')
  await expect(page.getByRole('button', { name: /Empresa Alba Retail Group SL/ })).toBeVisible()
  await page.getByRole('searchbox', { name: 'Buscar en tenant' }).fill('')

  await expect(page.getByRole('region', { name: 'Resumen SaaS' })).toContainText('Empresas')
  await expect(page.getByRole('list', { name: 'Empresas del tenant' })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Nueva empresa' })).toBeVisible()
  await expect(page.getByRole('button', { name: 'Crear', exact: true })).toBeVisible()

  const detail = page.getByLabel('Detalle de empresa')
  await expect(detail).toContainText('Datos basicos')
  await expect(detail.getByRole('button', { name: 'Guardar', exact: true })).toBeVisible()
  await expect(detail.getByRole('button', { name: 'Desactivar', exact: true })).toBeVisible()

  const summary = page.getByRole('region', { name: 'Resumen operativo de empresa' })
  await expect(summary).toContainText('Emitidas')
  await expect(summary).toContainText('Pendiente')
  await expect(summary.getByRole('button', { name: 'Exportar CSV' })).toBeVisible()
  await expect(page.getByLabel('Dashboard grafico de facturas')).toBeVisible()
  await expect(page.getByLabel('Notificaciones internas')).toBeVisible()

  const fiscal = page.getByRole('region', { name: 'Configuracion fiscal de empresa' })
  await expect(fiscal.getByRole('button', { name: 'Guardar fiscalidad' })).toBeVisible()
  await expect(fiscal.getByRole('button', { name: 'Crear serie' })).toBeVisible()
  await expect(fiscal).toContainText('F-2026-')

  const customers = page.getByRole('region', { name: 'Clientes de facturacion' })
  await expect(customers.getByRole('searchbox', { name: 'Buscar cliente' })).toBeVisible()
  await expect(customers.getByRole('button', { name: 'CSV' })).toBeVisible()
  await expect(customers.getByRole('button', { name: 'Crear cliente' })).toBeVisible()
  await expect(page.getByRole('region', { name: 'Datos operativos por empresa' }).getByRole('link', { name: 'Facturas' })).toHaveAttribute('href', '#invoices')

  const documents = page.getByRole('region', { name: 'Documentos por empresa' })
  await expect(documents.getByRole('searchbox', { name: 'Buscar documento' })).toBeVisible()
  await expect(documents.getByRole('button', { name: 'Subir' })).toBeVisible()
  await expect(documents.getByRole('button', { name: 'Descargar' })).toBeVisible()
  await documents.getByRole('button', { name: 'Eventos' }).click()
  await expect(documents).toContainText('DOCUMENT_VIEWED')
  await expect(documents).toContainText('Evento demo para documento')

  const invoices = page.getByRole('region', { name: 'Facturas por empresa' })
  await expect(invoices.getByRole('searchbox', { name: 'Buscar factura' })).toBeVisible()
  await expect(invoices.getByLabel('Estado')).toBeVisible()
  await expect(invoices.getByRole('button', { name: 'Guardar borrador' })).toBeVisible()
  await expect(invoices.getByRole('button', { name: 'Linea' })).toBeVisible()

  const invoiceNumber = `GUIA-${Date.now()}`
  await page.getByLabel('Numero').fill(invoiceNumber)
  await page.getByLabel('Descripcion').fill('Servicio guia funcional')
  await page.getByLabel('Cantidad').fill('1')
  await page.getByLabel('Precio').fill('80')
  await page.getByLabel('IVA %').fill('21')
  await expect(page.getByLabel('Totales calculados')).toContainText('96,80')
  await invoices.getByRole('button', { name: 'Guardar borrador' }).click()
  await expect(page.getByText('Factura borrador creada')).toBeVisible()
  await expect(invoices.getByText(invoiceNumber).first()).toBeVisible()
  await expect(invoices.getByRole('button', { name: 'Ver' })).toBeVisible()
  await expect(invoices.getByRole('button', { name: 'Editar' })).toBeEnabled()
  await expect(invoices.getByRole('button', { name: 'Emitir' })).toBeEnabled()
  await expect(invoices.getByRole('button', { name: 'PDF' })).toBeDisabled()
  await expect(invoices.getByRole('button', { name: 'Rectificar' })).toBeDisabled()
  await expect(invoices.getByRole('button', { name: 'Anular' })).toBeDisabled()

  await invoices.getByRole('button', { name: 'Editar' }).click()
  await expect(invoices.getByRole('button', { name: 'Cancelar' })).toBeVisible()
  await invoices.getByRole('button', { name: 'Cancelar' }).click()
  await invoices.getByRole('button', { name: 'Emitir' }).click()
  await expect(page.getByText('Factura emitida', { exact: true })).toBeVisible()
  await expect(invoices.getByRole('button', { name: 'PDF' })).toBeEnabled()
  await expect(invoices.getByRole('button', { name: 'Rectificar' })).toBeEnabled()
  await expect(invoices.getByRole('button', { name: 'Anular' })).toBeEnabled()

  await expect(page.getByLabel('Cobros y estado de factura')).toContainText('Factura seleccionada')
  await expect(page.getByRole('region', { name: 'Timeline de factura' })).toBeVisible()
  await page.getByLabel('Pago', { exact: true }).fill('96.8')
  await page.getByRole('button', { name: 'Registrar pago' }).click()
  await expect(page.getByText('Pago registrado')).toBeVisible()
  await page.getByRole('button', { name: 'PDF' }).click()
  await expect(page.getByText('PDF descargado')).toBeVisible()
  await expect(page.getByText(/PDF hash/)).toBeVisible()

  const einvoice = page.getByRole('region', { name: 'Factura electronica local' })
  await einvoice.getByRole('button', { name: 'Generar' }).click()
  await expect(einvoice).toContainText('LOCAL STUB')
  await expect(einvoice).toContainText('Artifact ID')

  const sif = page.getByRole('region', { name: 'Registro SIF local' })
  await sif.getByRole('button', { name: 'Registrar' }).click()
  await expect(sif).toContainText('LOCAL STUB')
  await expect(sif).toContainText('Secuencia')

  const evidence = page.getByRole('region', { name: 'Auditoria y exportacion de evidencia' })
  await expect(evidence.getByRole('searchbox', { name: 'Buscar auditoria' })).toBeVisible()
  await expect(evidence.getByRole('button', { name: 'CSV' })).toBeVisible()
  await evidence.getByRole('button', { name: 'Generar ZIP' }).click()
  await expect(page.getByText('Paquete de evidencia generado')).toBeVisible()
  await expect(page.getByRole('region', { name: 'Timeline de empresa' })).toBeVisible()

  await expect(page.getByRole('region', { name: 'Plataforma SaaS' })).toContainText('Aislamiento y evidencias')
  await page.getByRole('tab', { name: /Cobalto Industrial/ }).click()
  await expect(page.getByRole('button', { name: /Cobalto Industrial SA/ })).toBeVisible()
  await expect(page.getByRole('button', { name: /Norte Asesores SL/ })).toHaveCount(0)
})

type Company = ReturnType<typeof company>

type ApiState = {
  companies: Record<string, Company[]>
  documents: Record<string, unknown[]>
  invoices: Record<string, Invoice[]>
  fiscalSettings: Record<string, unknown>
  invoiceSeries: Record<string, Array<{ id: string; companyId: string; code: string; prefix: string; nextNumber: number; padding: number; active: boolean }>>
  customers: Record<string, unknown[]>
  payments: Record<string, unknown[]>
  auditEvents: Record<string, unknown[]>
  exports: Record<string, unknown[]>
  einvoices: Record<string, unknown[]>
  sifRecords: Record<string, unknown[]>
}

type Invoice = {
  id: string
  tenantId: string
  issuerCompany: Company
  customerCompany: Company
  invoiceNumber: string
  fiscalNumber?: string
  seriesCode?: string
  invoiceType: string
  status: string
  issueDate: string
  dueDate?: string
  currency: string
  taxableBase: number
  taxTotal: number
  total: number
  payableTotal?: number
  paymentStatus?: string
  paidAmount?: number
  outstandingAmount?: number
  lines: Array<{
    lineNumber: number
    description: string
    quantity: number
    unitPrice: number
    taxRate: number
    lineBase: number
    taxAmount: number
    lineTotal: number
  }>
  taxes: unknown[]
}

function createApiState(): ApiState {
  return {
    companies: {
      [tenantNorte]: [owner, alba],
      [tenantCobalto]: [cobalto],
    },
    documents: {
      [tenantNorte]: [],
      [tenantCobalto]: [],
    },
    invoices: {
      [tenantNorte]: [],
      [tenantCobalto]: [],
    },
    fiscalSettings: {},
    invoiceSeries: {},
    customers: {},
    payments: {},
    auditEvents: {},
    exports: {},
    einvoices: {
      [tenantNorte]: [],
      [tenantCobalto]: [],
    },
    sifRecords: {
      [tenantNorte]: [],
      [tenantCobalto]: [],
    },
  }
}

async function mockApi(page: Page, state: ApiState) {
  await page.route('**/api/**', async (route) => {
    const request = route.request()
    const url = new URL(request.url())
    const path = url.pathname.replace('/api', '')
    const method = request.method()

    if (path === '/health') {
      return fulfill(route, { status: 'ok', service: 'fiscal-saas-backend', checkedAt: new Date().toISOString() })
    }
    if (path === '/me') {
      return fulfill(route, {
        user: {
          id: '20000000-0000-0000-0000-000000000001',
          email: 'ana.admin@fiscalsaas.local',
          displayName: 'Ana Admin',
          roles: ['platform_admin'],
        },
        memberships: [],
      })
    }
    if (path === '/tenants') {
      return fulfill(route, [
        { id: tenantNorte, slug: 'norte-asesores', name: 'Norte Asesores', role: 'platform_admin' },
        { id: tenantCobalto, slug: 'cobalto-industrial', name: 'Cobalto Industrial', role: 'auditor' },
      ])
    }
    if (path === '/platform/plans') {
      return fulfill(route, [{
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
      }])
    }
    if (path === '/platform/tenants') {
      return fulfill(route, [
        { id: tenantNorte, slug: 'norte-asesores', displayName: 'Norte Asesores', status: 'ACTIVE', planCode: 'starter', subscriptionStatus: 'trialing' },
        { id: tenantCobalto, slug: 'cobalto-industrial', displayName: 'Cobalto Industrial', status: 'ACTIVE', planCode: 'starter', subscriptionStatus: 'active' },
      ])
    }

    const tenantId = path.split('/')[2]
    if (path.endsWith('/business-relationships')) {
      return fulfill(route, [])
    }
    if (path.endsWith('/companies') && method === 'GET') {
      const search = (url.searchParams.get('search') ?? '').toLowerCase()
      const companies = (state.companies[tenantId] ?? []).filter((item) => (
        !search || item.legalName.toLowerCase().includes(search) || item.taxId.toLowerCase().includes(search)
      ))
      return fulfill(route, companies)
    }
    if (path.endsWith('/companies') && method === 'POST') {
      const body = request.postDataJSON()
      const created = company(`company-${Date.now()}`, tenantId, body.legalName, body.taxId, body.relationshipType)
      state.companies[tenantId].push(created)
      return fulfill(route, created, 201)
    }
    if (path.includes('/companies/') && path.endsWith('/fiscal-settings') && method === 'GET') {
      const companyId = path.split('/')[4]
      return fulfill(route, state.fiscalSettings[companyId] ?? fiscalSettings(companyId, state.companies[tenantId].find((item) => item.id === companyId) ?? owner))
    }
    if (path.includes('/companies/') && path.endsWith('/fiscal-settings') && method === 'PUT') {
      const companyId = path.split('/')[4]
      const saved = { id: `settings-${companyId}`, companyId, ...request.postDataJSON() }
      state.fiscalSettings[companyId] = saved
      pushAudit(state, tenantId, companyId, 'FISCAL_SETTINGS_UPDATED')
      return fulfill(route, saved)
    }
    if (path.includes('/companies/') && path.endsWith('/invoice-series') && method === 'GET') {
      const companyId = path.split('/')[4]
      state.invoiceSeries[companyId] ??= [series(companyId)]
      return fulfill(route, state.invoiceSeries[companyId])
    }
    if (path.includes('/companies/') && path.endsWith('/invoice-series') && method === 'POST') {
      const companyId = path.split('/')[4]
      const created = { id: `series-${Date.now()}`, companyId, ...request.postDataJSON() }
      state.invoiceSeries[companyId] ??= []
      state.invoiceSeries[companyId].push(created)
      return fulfill(route, created, 201)
    }
    if (path.includes('/companies/') && path.endsWith('/customers') && method === 'GET') {
      const companyId = path.split('/')[4]
      return fulfill(route, state.customers[companyId] ?? [])
    }
    if (path.includes('/companies/') && path.endsWith('/customers') && method === 'POST') {
      const companyId = path.split('/')[4]
      const created = { id: `customer-${Date.now()}`, companyId, status: 'ACTIVE', ...request.postDataJSON() }
      state.customers[companyId] ??= []
      state.customers[companyId].push(created)
      pushAudit(state, tenantId, companyId, 'CUSTOMER_CREATED')
      return fulfill(route, created, 201)
    }
    if (path.includes('/companies/') && path.endsWith('/audit-events') && method === 'GET') {
      const companyId = path.split('/')[4]
      return fulfill(route, state.auditEvents[companyId] ?? [])
    }
    if (path.includes('/companies/') && path.endsWith('/exports') && method === 'GET') {
      const companyId = path.split('/')[4]
      return fulfill(route, state.exports[companyId] ?? [])
    }
    if (path.includes('/companies/') && path.endsWith('/exports/evidence-pack') && method === 'POST') {
      const companyId = path.split('/')[4]
      const created = {
        id: `export-${Date.now()}`,
        companyId,
        status: 'COMPLETED',
        exportType: 'EVIDENCE_PACK',
        sha256: 'eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee',
        createdAt: new Date().toISOString(),
      }
      state.exports[companyId] ??= []
      state.exports[companyId].unshift(created)
      return fulfill(route, created, 201)
    }
    if (path.includes('/exports/') && path.endsWith('/download') && method === 'GET') {
      return route.fulfill({
        status: 200,
        contentType: 'application/zip',
        body: 'zip',
      })
    }
    if (path.includes('/companies/') && method === 'PATCH') {
      const companyId = path.split('/').at(-1)
      const body = request.postDataJSON()
      const updated = state.companies[tenantId].find((item) => item.id === companyId)
      Object.assign(updated ?? {}, body)
      return fulfill(route, updated)
    }
    if (path.includes('/documents/') && path.endsWith('/download') && method === 'GET') {
      return route.fulfill({
        status: 200,
        contentType: 'application/pdf',
        headers: { 'content-disposition': 'attachment; filename="documento-demo.pdf"' },
        body: '%PDF-1.4',
      })
    }
    if (path.includes('/documents/') && path.endsWith('/events') && method === 'GET') {
      return fulfill(route, [
        {
          id: 'document-event-guide-1',
          eventType: 'DOCUMENT_VIEWED',
          details: 'Evento demo para documento',
          createdAt: new Date().toISOString(),
        },
      ])
    }
    if (path.includes('/documents')) {
      if (method === 'POST') {
        const created = {
          id: `doc-${Date.now()}`,
          tenantId,
          company: state.companies[tenantId].at(-1),
          documentType: 'EVIDENCE',
          title: 'Contrato demo',
          status: 'ACTIVE',
          currentVersion: 1,
          latestSha256: 'abc123abc123abc123abc123abc123abc123abc123abc123abc123abc123abcd',
          latestByteSize: 128,
          latestFilename: 'contrato.txt',
          updatedAt: new Date().toISOString(),
        }
        state.documents[tenantId].push(created)
        return fulfill(route, created, 201)
      }
      return fulfill(route, state.documents[tenantId] ?? [])
    }
    if (path.endsWith('/invoices') && method === 'GET') {
      return fulfill(route, state.invoices[tenantId] ?? [])
    }
    if (path.endsWith('/invoices') && method === 'POST') {
      const body = request.postDataJSON()
      const created = invoiceFromBody(tenantId, body, state)
      state.invoices[tenantId].unshift(created)
      return fulfill(route, created, 201)
    }
    if (path.includes('/invoices/') && method === 'GET' && !path.endsWith('/payments') && !path.endsWith('/pdf')) {
      const invoiceId = path.split('/').at(-1)
      return fulfill(route, state.invoices[tenantId].find((item) => item.id === invoiceId))
    }
    if (path.includes('/invoices/') && path.endsWith('/issue') && method === 'POST') {
      const invoiceId = path.split('/').at(-2)
      const invoice = state.invoices[tenantId].find((item) => item.id === invoiceId)
      if (invoice) {
        invoice.status = 'ISSUED'
        invoice.seriesCode = '2026'
        invoice.fiscalNumber = `F-2026-${String(state.invoices[tenantId].length).padStart(6, '0')}`
        invoice.paymentStatus = 'UNPAID'
        invoice.outstandingAmount = invoice.total
      }
      return fulfill(route, invoice)
    }
    if (path.includes('/invoices/') && path.endsWith('/payments') && method === 'GET') {
      const invoiceId = path.split('/').at(-2) ?? ''
      return fulfill(route, state.payments[invoiceId] ?? [])
    }
    if (path.includes('/invoices/') && path.endsWith('/payments') && method === 'POST') {
      const invoiceId = path.split('/').at(-2) ?? ''
      const invoice = state.invoices[tenantId].find((item) => item.id === invoiceId)
      const body = request.postDataJSON()
      const payment = { id: `payment-${Date.now()}`, invoiceId, ...body }
      state.payments[invoiceId] ??= []
      state.payments[invoiceId].unshift(payment)
      if (invoice) {
        invoice.paidAmount = (invoice.paidAmount ?? 0) + body.amount
        invoice.outstandingAmount = round(Math.max(0, invoice.total - invoice.paidAmount))
        invoice.paymentStatus = invoice.outstandingAmount === 0 ? 'PAID' : 'PARTIALLY_PAID'
      }
      return fulfill(route, payment, 201)
    }
    if (path.includes('/invoices/') && path.endsWith('/pdf') && method === 'GET') {
      return route.fulfill({
        status: 200,
        contentType: 'application/pdf',
        headers: {
          'Access-Control-Expose-Headers': 'X-Content-SHA256',
          'X-Content-SHA256': 'cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc',
        },
        body: '%PDF-1.4',
      })
    }
    if (path.includes('/invoices/') && path.endsWith('/create-corrective') && method === 'POST') {
      const invoiceId = path.split('/').at(-2)
      const original = state.invoices[tenantId].find((item) => item.id === invoiceId)
      const corrective = original ? { ...original, id: `corrective-${Date.now()}`, invoiceNumber: `${original.invoiceNumber}-RECT`, status: 'DRAFT' } : undefined
      if (corrective) state.invoices[tenantId].unshift(corrective)
      return fulfill(route, corrective, 201)
    }
    if (path.includes('/invoices/') && path.endsWith('/cancel-local') && method === 'POST') {
      const invoiceId = path.split('/').at(-2)
      const invoice = state.invoices[tenantId].find((item) => item.id === invoiceId)
      if (invoice) invoice.status = 'CANCELLED_LOCAL'
      return fulfill(route, invoice)
    }
    if (path.includes('/invoices/') && method === 'PATCH' && path.endsWith('/status')) {
      const invoiceId = path.split('/').at(-2)
      const invoice = state.invoices[tenantId].find((item) => item.id === invoiceId)
      if (invoice) {
        invoice.status = request.postDataJSON().status
      }
      return fulfill(route, invoice)
    }
    if (path.includes('/invoices/') && method === 'PATCH') {
      const invoiceId = path.split('/').at(-1)
      const index = state.invoices[tenantId].findIndex((item) => item.id === invoiceId)
      const updated = invoiceFromBody(tenantId, request.postDataJSON(), state, invoiceId)
      state.invoices[tenantId][index] = updated
      return fulfill(route, updated)
    }
    if (path.endsWith('/einvoices') && method === 'GET') {
      return fulfill(route, state.einvoices[tenantId] ?? [])
    }
    if (path.endsWith('/einvoices') && method === 'POST') {
      const body = request.postDataJSON()
      const invoice = state.invoices[tenantId].find((item) => item.id === body.invoiceId)
      const message = {
        id: `einvoice-${Date.now()}`,
        tenantId,
        invoiceId: body.invoiceId,
        invoiceNumber: invoice?.invoiceNumber,
        issuerLegalName: invoice?.issuerCompany.legalName,
        customerLegalName: invoice?.customerCompany.legalName,
        syntax: 'UBL',
        direction: 'OUTBOUND',
        exchangeStatus: 'GENERATED',
        commercialStatus: 'PENDING',
        paymentStatus: 'UNPAID',
        payloadSha256: 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      }
      state.einvoices[tenantId].unshift(message)
      return fulfill(route, message, 201)
    }
    if (path.endsWith('/verifactu/records') && method === 'GET') {
      return fulfill(route, state.sifRecords[tenantId] ?? [])
    }
    if (path.endsWith('/verifactu/records') && method === 'POST') {
      const body = request.postDataJSON()
      const invoice = state.invoices[tenantId].find((item) => item.id === body.invoiceId)
      const record = {
        id: `sif-${Date.now()}`,
        tenantId,
        invoiceId: body.invoiceId,
        invoiceNumber: invoice?.invoiceNumber,
        recordType: 'REGISTRATION',
        sequenceNumber: state.sifRecords[tenantId].length + 1,
        previousHash: 'GENESIS',
        recordHash: '1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef',
        canonicalPayload: '{"recordType":"REGISTRATION"}',
        systemVersion: '0.1.0',
        normativeVersion: 'CURRENT_AS_OF_2026_05_03',
        createdAt: new Date().toISOString(),
      }
      state.sifRecords[tenantId].unshift(record)
      return fulfill(route, record, 201)
    }
    if (path.endsWith('/verifactu/system-declarations/drafts')) {
      return fulfill(route, [])
    }

    return fulfill(route, [])
  })
}

function fulfill(route: Route, body: unknown, status = 200) {
  return route.fulfill({
    status,
    contentType: 'application/json',
    body: JSON.stringify(body),
  })
}

function company(id: string, tenantId: string, legalName: string, taxId: string, relationshipType: string) {
  return {
    id,
    tenantId,
    legalName,
    taxId,
    countryCode: 'ES',
    relationshipType,
    status: 'ACTIVE',
  }
}

function documentSummary(id: string, tenantId: string, companyValue: Company) {
  return {
    id,
    tenantId,
    company: companyValue,
    documentType: 'EVIDENCE',
    title: 'Documento demo de guia',
    status: 'ACTIVE',
    currentVersion: 1,
    latestSha256: `hash-${id}-aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa`,
    latestByteSize: 512,
    latestFilename: 'documento-demo.pdf',
    updatedAt: new Date().toISOString(),
  }
}

function auditEvent(id: string, tenantId: string, companyId: string, eventType: string) {
  return {
    id,
    tenantId,
    companyId,
    actorEmail: 'ana.admin@fiscalsaas.local',
    eventType,
    entityType: 'UI',
    entityId: companyId,
    eventHash: 'ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff',
    occurredAt: new Date().toISOString(),
  }
}

function fiscalSettings(companyId: string, companyValue: Company) {
  return {
    id: `settings-${companyId}`,
    companyId,
    legalName: companyValue.legalName,
    tradeName: companyValue.legalName,
    nif: companyValue.taxId,
    vatNumber: companyValue.taxId,
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

function series(companyId: string) {
  return {
    id: `series-${companyId}`,
    companyId,
    code: '2026',
    prefix: 'F-2026-',
    nextNumber: 1,
    padding: 6,
    active: true,
  }
}

function pushAudit(state: ApiState, tenantId: string, companyId: string, eventType: string) {
  state.auditEvents[companyId] ??= []
  state.auditEvents[companyId].unshift({
    id: `audit-${Date.now()}`,
    tenantId,
    companyId,
    actorEmail: 'ana.admin@fiscalsaas.local',
    eventType,
    entityType: 'UI',
    entityId: companyId,
    eventHash: 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa',
    occurredAt: new Date().toISOString(),
  })
}

function invoiceFromBody(tenantId: string, body: {
  issuerCompanyId: string
  customerCompanyId: string
  customerId?: string
  invoiceNumber: string
  invoiceType: string
  issueDate: string
  dueDate?: string
  currency: string
  lines: Array<{ description: string; quantity: number; unitPrice: number; taxRate: number; discountPercent?: number; withholdingPercent?: number }>
}, state: ApiState, invoiceId = `invoice-${Date.now()}`): Invoice {
  const invoiceLines = body.lines.map((line, index) => {
    const gross = round(line.quantity * line.unitPrice)
    const lineBase = round(gross - gross * ((line.discountPercent ?? 0) / 100))
    const taxAmount = round(lineBase * (line.taxRate / 100))
    const withholdingAmount = round(lineBase * ((line.withholdingPercent ?? 0) / 100))
    return {
      lineNumber: index + 1,
      description: line.description,
      quantity: line.quantity,
      unitPrice: line.unitPrice,
      taxRate: line.taxRate,
      discountPercent: line.discountPercent ?? 0,
      withholdingPercent: line.withholdingPercent ?? 0,
      withholdingAmount,
      lineBase,
      taxAmount,
      lineTotal: round(lineBase + taxAmount - withholdingAmount),
    }
  })
  const taxableBase = round(invoiceLines.reduce((total, line) => total + line.lineBase, 0))
  const taxTotal = round(invoiceLines.reduce((total, line) => total + line.taxAmount, 0))
  const withholdingTotal = round(invoiceLines.reduce((total, line) => total + line.withholdingAmount, 0))
  const total = round(taxableBase + taxTotal - withholdingTotal)
  return {
    id: invoiceId,
    tenantId,
    issuerCompany: state.companies[tenantId].find((item) => item.id === body.issuerCompanyId) ?? state.companies[tenantId][0],
    customerCompany: state.companies[tenantId].find((item) => item.id === body.customerCompanyId) ?? state.companies[tenantId][0],
    invoiceNumber: body.invoiceNumber,
    invoiceType: body.invoiceType,
    status: 'DRAFT',
    issueDate: body.issueDate,
    dueDate: body.dueDate,
    currency: body.currency,
    taxableBase,
    taxTotal,
    total,
    payableTotal: total,
    paymentStatus: 'UNPAID',
    paidAmount: 0,
    outstandingAmount: 0,
    lines: invoiceLines,
    taxes: [],
  }
}

function round(value: number) {
  return Math.round((value + Number.EPSILON) * 100) / 100
}
