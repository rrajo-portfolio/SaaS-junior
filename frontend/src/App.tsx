import { type FormEvent, useEffect, useMemo, useState } from 'react'
import {
  Activity,
  AlertTriangle,
  Bell,
  Building2,
  CheckCircle2,
  ChevronRight,
  FileCheck2,
  FileClock,
  FileText,
  Files,
  Fingerprint,
  Gauge,
  Handshake,
  Landmark,
  LockKeyhole,
  Network,
  Plus,
  Search,
  Server,
  ShieldCheck,
  UploadCloud,
  Users,
} from 'lucide-react'
import './App.css'

type ApiHealth = {
  status: string
  service: string
  checkedAt: string
}

type Membership = {
  tenantId: string
  tenantSlug: string
  tenantName: string
  role: string
}

type CurrentUser = {
  user: {
    id: string
    email: string
    displayName: string
    roles: string[]
  }
  memberships: Membership[]
}

type TenantSummary = {
  id: string
  slug: string
  name: string
  role: string
}

type CompanySummary = {
  id: string
  tenantId: string
  legalName: string
  taxId: string
  countryCode: string
  relationshipType: string
  status: string
}

type BusinessRelationship = {
  id: string
  tenantId: string
  sourceCompany: CompanySummary
  targetCompany: CompanySummary
  relationshipKind: string
  status: string
  notes?: string
  startsAt: string
}

type DocumentSummary = {
  id: string
  tenantId: string
  company: CompanySummary
  documentType: string
  title: string
  status: string
  currentVersion: number
  latestSha256: string
  latestByteSize: number
  latestFilename: string
  updatedAt: string
}

type InvoiceSummary = {
  id: string
  tenantId: string
  issuerCompany: CompanySummary
  customerCompany: CompanySummary
  invoiceNumber: string
  invoiceType: string
  status: string
  issueDate: string
  currency: string
  taxableBase: number
  taxTotal: number
  total: number
  rectifiesInvoiceId?: string
}

type SifRecordSummary = {
  id: string
  tenantId: string
  invoiceId: string
  invoiceNumber: string
  recordType: string
  sequenceNumber: number
  previousHash: string
  recordHash: string
  canonicalPayload: string
  systemVersion: string
  normativeVersion: string
  createdAt: string
}

type SifSystemDeclarationSummary = {
  id: string
  tenantId: string
  status: string
  payload: string
  payloadSha256: string
  createdAt: string
}

type EInvoiceSummary = {
  id: string
  tenantId: string
  invoiceId: string
  invoiceNumber: string
  issuerLegalName: string
  customerLegalName: string
  syntax: string
  direction: string
  exchangeStatus: string
  commercialStatus: string
  paymentStatus: string
  payloadSha256: string
  statusReason?: string
  createdAt: string
  updatedAt: string
}

type CompanyFormState = {
  legalName: string
  taxId: string
  countryCode: string
  relationshipType: string
}

type DocumentFormState = {
  companyId: string
  documentType: string
  title: string
}

type HealthState =
  | { label: 'Comprobando'; tone: 'warning' }
  | { label: 'Operativo'; tone: 'success'; payload: ApiHealth }
  | { label: 'Sin conexion'; tone: 'danger' }

const apiRoot = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api').replace(/\/$/, '')
const demoUserEmail = 'ana.admin@fiscalsaas.local'
const initialCompanyForm: CompanyFormState = {
  legalName: '',
  taxId: '',
  countryCode: 'ES',
  relationshipType: 'CLIENT',
}
const initialDocumentForm: DocumentFormState = {
  companyId: '',
  documentType: 'INVOICE_RECEIVED',
  title: '',
}

const fiscalStatus = [
  { label: 'Tenant guard', value: 'Activo', icon: Network, tone: 'success' },
  { label: 'AEAT adapter', value: 'Stub seguro', icon: Landmark, tone: 'warning' },
  { label: 'B2B e-invoice', value: 'Relaciones activas', icon: FileCheck2, tone: 'neutral' },
]

const auditTrail = [
  'Migracion V2 de identidad aplicada',
  'Memberships activos resueltos por usuario',
  'Cabecera X-Tenant-Id validada por endpoint',
  'CRUD de empresas validado por rol y tenant',
]

function apiUrl(path: string) {
  return `${apiRoot}${path}`
}

function authHeaders(tenantId?: string) {
  const headers: Record<string, string> = {
    'X-User-Email': demoUserEmail,
  }

  if (tenantId) {
    headers['X-Tenant-Id'] = tenantId
  }

  return headers
}

function App() {
  const [health, setHealth] = useState<HealthState>({ label: 'Comprobando', tone: 'warning' })
  const [me, setMe] = useState<CurrentUser | null>(null)
  const [tenants, setTenants] = useState<TenantSummary[]>([])
  const [activeTenantId, setActiveTenantId] = useState<string>('')
  const [companies, setCompanies] = useState<CompanySummary[]>([])
  const [relationships, setRelationships] = useState<BusinessRelationship[]>([])
  const [documents, setDocuments] = useState<DocumentSummary[]>([])
  const [invoices, setInvoices] = useState<InvoiceSummary[]>([])
  const [sifRecords, setSifRecords] = useState<SifRecordSummary[]>([])
  const [systemDeclarations, setSystemDeclarations] = useState<SifSystemDeclarationSummary[]>([])
  const [einvoices, setEinvoices] = useState<EInvoiceSummary[]>([])
  const [companyForm, setCompanyForm] = useState<CompanyFormState>(initialCompanyForm)
  const [documentForm, setDocumentForm] = useState<DocumentFormState>(initialDocumentForm)
  const [documentFile, setDocumentFile] = useState<File | null>(null)
  const [isSubmittingCompany, setIsSubmittingCompany] = useState(false)
  const [isUploadingDocument, setIsUploadingDocument] = useState(false)
  const [identityError, setIdentityError] = useState<string | null>(null)
  const [companyMutationMessage, setCompanyMutationMessage] = useState<string | null>(null)
  const [documentMutationMessage, setDocumentMutationMessage] = useState<string | null>(null)

  useEffect(() => {
    const controller = new AbortController()

    fetch(apiUrl('/health'), { signal: controller.signal })
      .then((response) => {
        if (!response.ok) {
          throw new Error('Backend health check failed')
        }
        return response.json() as Promise<ApiHealth>
      })
      .then((payload) => {
        setHealth({ label: 'Operativo', tone: 'success', payload })
      })
      .catch((error: unknown) => {
        if (error instanceof DOMException && error.name === 'AbortError') {
          return
        }
        setHealth({ label: 'Sin conexion', tone: 'danger' })
      })

    Promise.all([
      fetch(apiUrl('/me'), { headers: authHeaders(), signal: controller.signal }),
      fetch(apiUrl('/tenants'), { headers: authHeaders(), signal: controller.signal }),
    ])
      .then(async ([meResponse, tenantsResponse]) => {
        if (!meResponse.ok || !tenantsResponse.ok) {
          throw new Error('Identity bootstrap failed')
        }
        const currentUser = (await meResponse.json()) as CurrentUser
        const tenantList = (await tenantsResponse.json()) as TenantSummary[]
        setMe(currentUser)
        setTenants(tenantList)
        setActiveTenantId(tenantList[0]?.id ?? '')
      })
      .catch((error: unknown) => {
        if (error instanceof DOMException && error.name === 'AbortError') {
          return
        }
        setIdentityError('Identidad no disponible')
      })

    return () => controller.abort()
  }, [])

  useEffect(() => {
    if (!activeTenantId) {
      return
    }

    const controller = new AbortController()
    Promise.all([
      fetch(apiUrl(`/tenants/${activeTenantId}/companies`), {
        headers: authHeaders(activeTenantId),
        signal: controller.signal,
      }),
      fetch(apiUrl(`/tenants/${activeTenantId}/business-relationships`), {
        headers: authHeaders(activeTenantId),
        signal: controller.signal,
      }),
      fetch(apiUrl(`/tenants/${activeTenantId}/documents`), {
        headers: authHeaders(activeTenantId),
        signal: controller.signal,
      }),
      fetch(apiUrl(`/tenants/${activeTenantId}/invoices`), {
        headers: authHeaders(activeTenantId),
        signal: controller.signal,
      }),
      fetch(apiUrl(`/tenants/${activeTenantId}/einvoices`), {
        headers: authHeaders(activeTenantId),
        signal: controller.signal,
      }),
      fetch(apiUrl(`/tenants/${activeTenantId}/verifactu/records`), {
        headers: authHeaders(activeTenantId),
        signal: controller.signal,
      }),
      fetch(apiUrl(`/tenants/${activeTenantId}/verifactu/system-declarations/drafts`), {
        headers: authHeaders(activeTenantId),
        signal: controller.signal,
      }),
    ])
      .then(
        async ([
          companiesResponse,
          relationshipsResponse,
          documentsResponse,
          invoicesResponse,
          einvoicesResponse,
          sifRecordsResponse,
          declarationsResponse,
        ]) => {
        if (
          !companiesResponse.ok ||
          !relationshipsResponse.ok ||
          !documentsResponse.ok ||
          !invoicesResponse.ok ||
          !einvoicesResponse.ok ||
          !sifRecordsResponse.ok ||
          !declarationsResponse.ok
        ) {
          throw new Error('Company scope failed')
        }
        const tenantCompanies = (await companiesResponse.json()) as CompanySummary[]
        const tenantRelationships = (await relationshipsResponse.json()) as BusinessRelationship[]
        const tenantDocuments = (await documentsResponse.json()) as DocumentSummary[]
        const tenantInvoices = (await invoicesResponse.json()) as InvoiceSummary[]
        const tenantEinvoices = (await einvoicesResponse.json()) as EInvoiceSummary[]
        const tenantSifRecords = (await sifRecordsResponse.json()) as SifRecordSummary[]
        const tenantDeclarations = (await declarationsResponse.json()) as SifSystemDeclarationSummary[]
        setCompanies(tenantCompanies)
        setRelationships(tenantRelationships)
        setDocuments(tenantDocuments)
        setInvoices(tenantInvoices)
        setEinvoices(tenantEinvoices)
        setSifRecords(tenantSifRecords)
        setSystemDeclarations(tenantDeclarations)
      })
      .catch((error: unknown) => {
        if (error instanceof DOMException && error.name === 'AbortError') {
          return
        }
        setIdentityError('Tenant no accesible')
      })

    return () => controller.abort()
  }, [activeTenantId])

  async function createCompany(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!activeTenantId || isSubmittingCompany) {
      return
    }

    setIsSubmittingCompany(true)
    setCompanyMutationMessage(null)
    try {
      const response = await fetch(apiUrl(`/tenants/${activeTenantId}/companies`), {
        method: 'POST',
        headers: {
          ...authHeaders(activeTenantId),
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(companyForm),
      })
      if (!response.ok) {
        throw new Error('Company creation failed')
      }
      const createdCompany = (await response.json()) as CompanySummary
      setCompanies((current) => [...current, createdCompany].sort((a, b) => a.legalName.localeCompare(b.legalName)))
      setCompanyForm(initialCompanyForm)
      setCompanyMutationMessage('Empresa registrada')
    } catch {
      setCompanyMutationMessage('No se pudo registrar la empresa')
    } finally {
      setIsSubmittingCompany(false)
    }
  }

  async function uploadDocument(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!activeTenantId || !documentFile || isUploadingDocument) {
      setDocumentMutationMessage('Selecciona un archivo fiscal')
      return
    }

    const companyId = documentForm.companyId || companies[0]?.id
    if (!companyId) {
      setDocumentMutationMessage('Selecciona una empresa')
      return
    }

    setIsUploadingDocument(true)
    setDocumentMutationMessage(null)
    const formData = new FormData()
    formData.set('companyId', companyId)
    formData.set('documentType', documentForm.documentType)
    formData.set('title', documentForm.title || documentFile.name)
    formData.set('file', documentFile)

    try {
      const response = await fetch(apiUrl(`/tenants/${activeTenantId}/documents`), {
        method: 'POST',
        headers: authHeaders(activeTenantId),
        body: formData,
      })
      if (!response.ok) {
        throw new Error('Document upload failed')
      }
      const createdDocument = (await response.json()) as DocumentSummary
      setDocuments((current) => [createdDocument, ...current])
      setDocumentForm((current) => ({ ...initialDocumentForm, companyId: current.companyId }))
      setDocumentFile(null)
      setDocumentMutationMessage('Documento registrado')
    } catch {
      setDocumentMutationMessage('No se pudo registrar el documento')
    } finally {
      setIsUploadingDocument(false)
    }
  }

  const activeTenant = useMemo(
    () => tenants.find((tenant) => tenant.id === activeTenantId) ?? tenants[0],
    [activeTenantId, tenants],
  )
  const invoiceTotal = invoices.reduce((total, invoice) => total + Number(invoice.total), 0)
  const latestSifRecord = sifRecords[0]
  const latestSystemDeclaration = systemDeclarations[0]
  const latestEInvoice = einvoices[0]

  const metricCards = useMemo(
    () => [
      { label: 'Tenants asignados', value: tenants.length.toString(), trend: me?.user.displayName ?? demoUserEmail, icon: Users },
      { label: 'Empresas visibles', value: companies.length.toString(), trend: activeTenant?.name ?? 'Sin tenant activo', icon: Building2 },
      { label: 'Relaciones B2B', value: relationships.length.toString(), trend: activeTenant?.name ?? 'Sin tenant activo', icon: Handshake },
      { label: 'Facturas', value: invoices.length.toString(), trend: formatCurrency(invoiceTotal), icon: FileText },
      { label: 'E-invoices', value: einvoices.length.toString(), trend: latestEInvoice?.commercialStatus ?? 'Sin mensajes', icon: FileCheck2 },
      { label: 'Registros SIF', value: sifRecords.length.toString(), trend: latestSifRecord?.recordHash.slice(0, 12) ?? 'Sin cadena', icon: Fingerprint },
      { label: 'Documentos', value: documents.length.toString(), trend: 'SHA-256', icon: FileClock },
    ],
    [
      activeTenant,
      companies.length,
      documents.length,
      einvoices.length,
      invoiceTotal,
      invoices.length,
      latestEInvoice,
      latestSifRecord,
      me,
      relationships.length,
      sifRecords.length,
      tenants.length,
    ],
  )

  const healthDetail = useMemo(() => {
    if (health.tone !== 'success') {
      return apiRoot
    }
    return health.payload.service
  }, [health])

  const selectedDocumentCompanyId = documentForm.companyId || companies[0]?.id || ''

  return (
    <div className="app-shell">
      <aside className="sidebar" aria-label="Navegacion principal">
        <div className="brand-block">
          <div className="brand-mark" aria-hidden="true">
            <ShieldCheck size={22} />
          </div>
          <div>
            <strong>Fiscal SaaS</strong>
            <span>Preproduccion</span>
          </div>
        </div>

        <nav className="nav-list">
          <a className="active" href="#dashboard">
            <Gauge size={18} />
            <span>Dashboard</span>
          </a>
          <a href="#tenants">
            <Users size={18} />
            <span>Tenants</span>
          </a>
          <a href="#companies">
            <Building2 size={18} />
            <span>Empresas</span>
          </a>
          <a href="#documents">
            <Files size={18} />
            <span>Documentos</span>
          </a>
          <a href="#invoices">
            <FileText size={18} />
            <span>Facturas</span>
          </a>
          <a href="#einvoices">
            <FileCheck2 size={18} />
            <span>E-invoice</span>
          </a>
          <a href="#verifactu">
            <Fingerprint size={18} />
            <span>Verifactu</span>
          </a>
          <a href="#security">
            <LockKeyhole size={18} />
            <span>Auditoria</span>
          </a>
        </nav>
      </aside>

      <main className="workspace" id="dashboard">
        <header className="topbar">
          <div>
            <p className="eyebrow">Panel fiscal</p>
            <h1>Identidad y tenants</h1>
          </div>
          <div className="topbar-actions">
            <label className="search-box">
              <Search size={18} />
              <input aria-label="Buscar" placeholder="Buscar empresa, rol o tenant" />
            </label>
            <button className="icon-button" type="button" title="Notificaciones" aria-label="Notificaciones">
              <Bell size={18} />
            </button>
            <button className="primary-button" type="button">
              <UploadCloud size={18} />
              Subir documento
            </button>
          </div>
        </header>

        <section className="status-strip" aria-label="Estado de servicios">
          <div className={`service-state ${health.tone}`}>
            <Server size={18} />
            <span>{health.label}</span>
            <small>{healthDetail}</small>
          </div>
          {fiscalStatus.map((item) => {
            const Icon = item.icon
            return (
              <div className={`service-state ${item.tone}`} key={item.label}>
                <Icon size={18} />
                <span>{item.label}</span>
                <small>{item.value}</small>
              </div>
            )
          })}
        </section>

        <section className="identity-band" id="tenants" aria-label="Contexto de identidad">
          <div>
            <p className="eyebrow">Sesion</p>
            <h2>{me?.user.displayName ?? 'Usuario fiscal'}</h2>
            <span>{me?.user.email ?? demoUserEmail}</span>
          </div>
          <div className="tenant-switcher" role="tablist" aria-label="Tenant activo">
            {tenants.map((tenant) => (
              <button
                aria-selected={tenant.id === activeTenantId}
                className={tenant.id === activeTenantId ? 'selected' : ''}
                key={tenant.id}
                onClick={() => setActiveTenantId(tenant.id)}
                role="tab"
                type="button"
              >
                <Building2 size={16} />
                <span>{tenant.name}</span>
                <small>{formatRole(tenant.role)}</small>
              </button>
            ))}
          </div>
        </section>

        {identityError ? (
          <div className="warning-note identity-warning" role="status">
            <AlertTriangle size={18} />
            <span>{identityError}</span>
          </div>
        ) : null}

        <section className="metrics-grid" aria-label="Metricas principales">
          {metricCards.map((card) => {
            const Icon = card.icon
            return (
              <article className="metric-card" key={card.label}>
                <div className="metric-icon" aria-hidden="true">
                  <Icon size={20} />
                </div>
                <span>{card.label}</span>
                <strong>{card.value}</strong>
                <small>{card.trend}</small>
              </article>
            )
          })}
        </section>

        <section className="content-grid">
          <article className="panel company-panel" id="companies">
            <div className="panel-heading">
              <div>
                <p className="eyebrow">Empresas</p>
                <h2>{activeTenant?.name ?? 'Tenant activo'}</h2>
              </div>
              <button className="ghost-button" type="button">
                Ver membresias
                <ChevronRight size={16} />
              </button>
            </div>

            <form className="company-form" onSubmit={createCompany}>
              <div className="form-heading">
                <div>
                  <p className="eyebrow">Alta rapida</p>
                  <h3>Registrar cliente o proveedor</h3>
                </div>
                <button className="primary-button compact" disabled={isSubmittingCompany} type="submit">
                  <Plus size={16} />
                  {isSubmittingCompany ? 'Guardando' : 'Registrar'}
                </button>
              </div>
              <div className="form-grid">
                <label>
                  <span>Razon social</span>
                  <input
                    aria-label="Razon social"
                    onChange={(event) => setCompanyForm((current) => ({ ...current, legalName: event.target.value }))}
                    placeholder="Nueva Empresa SL"
                    value={companyForm.legalName}
                  />
                </label>
                <label>
                  <span>NIF/VAT</span>
                  <input
                    aria-label="NIF VAT"
                    onChange={(event) => setCompanyForm((current) => ({ ...current, taxId: event.target.value }))}
                    placeholder="B12345678"
                    value={companyForm.taxId}
                  />
                </label>
                <label>
                  <span>Pais</span>
                  <input
                    aria-label="Pais"
                    maxLength={2}
                    onChange={(event) => setCompanyForm((current) => ({ ...current, countryCode: event.target.value.toUpperCase() }))}
                    value={companyForm.countryCode}
                  />
                </label>
                <label>
                  <span>Relacion</span>
                  <select
                    aria-label="Relacion"
                    onChange={(event) => setCompanyForm((current) => ({ ...current, relationshipType: event.target.value }))}
                    value={companyForm.relationshipType}
                  >
                    <option value="CLIENT">Cliente</option>
                    <option value="SUPPLIER">Proveedor</option>
                    <option value="OWNER">Titular</option>
                  </select>
                </label>
              </div>
              {companyMutationMessage ? <p className="form-message">{companyMutationMessage}</p> : null}
            </form>

            <div className="data-table" role="table" aria-label="Empresas del tenant">
              <div className="table-row table-head" role="row">
                <span role="columnheader">Empresa</span>
                <span role="columnheader">Fiscal</span>
                <span role="columnheader">Relacion</span>
                <span role="columnheader">Estado</span>
                <span role="columnheader">Pais</span>
              </div>
              {companies.map((company) => (
                <div className="table-row" role="row" key={company.id}>
                  <span role="cell">
                    <strong>{company.legalName}</strong>
                    <small>{company.id}</small>
                  </span>
                  <span role="cell">{company.taxId}</span>
                  <span role="cell">
                    <StatusBadge label={formatRelationship(company.relationshipType)} />
                  </span>
                  <span role="cell">
                    <StatusBadge label={formatStatus(company.status)} />
                  </span>
                  <span role="cell">{company.countryCode}</span>
                </div>
              ))}
            </div>

            <section className="relationship-section" aria-label="Relaciones B2B">
              <div className="section-heading">
                <div>
                  <p className="eyebrow">Contrapartes</p>
                  <h3>Relaciones B2B</h3>
                </div>
                <span>{relationships.length} activas</span>
              </div>
              <div className="relationship-list">
                {relationships.map((relationship) => (
                  <article className="relationship-card" key={relationship.id}>
                    <div>
                      <strong>{relationship.sourceCompany.legalName}</strong>
                      <ChevronRight size={16} />
                      <strong>{relationship.targetCompany.legalName}</strong>
                    </div>
                    <span>{formatRelationshipKind(relationship.relationshipKind)}</span>
                    <small>{relationship.notes ?? 'Sin notas operativas'}</small>
                  </article>
                ))}
              </div>
            </section>
          </article>

          <aside className="panel audit-panel" aria-label="Auditoria">
            <div className="panel-heading">
              <div>
                <p className="eyebrow">Trazabilidad</p>
                <h2>Evidencia tecnica</h2>
              </div>
              <Activity size={20} />
            </div>
            <ol className="audit-list">
              {auditTrail.map((entry) => (
                <li key={entry}>
                  <CheckCircle2 size={17} />
                  <span>{entry}</span>
                </li>
              ))}
            </ol>
            <div className="warning-note">
              <AlertTriangle size={18} />
              <span>Produccion fiscal deshabilitada por defecto</span>
            </div>
          </aside>
        </section>

        <section className="panel invoice-panel" id="invoices" aria-label="Facturacion fiscal">
          <div className="panel-heading">
            <div>
              <p className="eyebrow">Facturas</p>
              <h2>Facturacion fiscal</h2>
            </div>
            <FileText size={20} />
          </div>

          <div className="invoice-summary" aria-label="Resumen de facturas">
            <div>
              <span>Base imponible</span>
              <strong>{formatCurrency(invoices.reduce((total, invoice) => total + Number(invoice.taxableBase), 0))}</strong>
            </div>
            <div>
              <span>Impuestos</span>
              <strong>{formatCurrency(invoices.reduce((total, invoice) => total + Number(invoice.taxTotal), 0))}</strong>
            </div>
            <div>
              <span>Total</span>
              <strong>{formatCurrency(invoiceTotal)}</strong>
            </div>
          </div>

          <div className="invoice-list">
            {invoices.length === 0 ? (
              <div className="empty-state">Sin facturas en el tenant activo</div>
            ) : (
              invoices.map((invoice) => (
                <article className="invoice-row" key={invoice.id}>
                  <div>
                    <strong>{invoice.invoiceNumber}</strong>
                    <span>{invoice.customerCompany.legalName}</span>
                  </div>
                  <StatusBadge label={formatInvoiceStatus(invoice.status)} />
                  <span>{formatInvoiceType(invoice.invoiceType)}</span>
                  <span>{invoice.issueDate}</span>
                  <strong>{formatCurrency(invoice.total)}</strong>
                </article>
              ))
            )}
          </div>
        </section>

        <section className="panel einvoice-panel" id="einvoices" aria-label="Factura electronica B2B">
          <div className="panel-heading">
            <div>
              <p className="eyebrow">B2B e-invoice</p>
              <h2>Factura electronica</h2>
            </div>
            <FileCheck2 size={20} />
          </div>

          <div className="einvoice-summary" aria-label="Resumen e-invoice">
            <div>
              <span>Mensajes</span>
              <strong>{einvoices.length}</strong>
            </div>
            <div>
              <span>Ultima sintaxis</span>
              <strong>{formatEInvoiceSyntax(latestEInvoice?.syntax)}</strong>
            </div>
            <div>
              <span>Estado comercial</span>
              <strong>{formatEInvoiceCommercialStatus(latestEInvoice?.commercialStatus)}</strong>
            </div>
            <div>
              <span>Pago</span>
              <strong>{formatEInvoicePaymentStatus(latestEInvoice?.paymentStatus)}</strong>
            </div>
          </div>

          <div className="einvoice-list">
            {einvoices.length === 0 ? (
              <div className="empty-state">Sin mensajes de factura electronica en el tenant activo</div>
            ) : (
              einvoices.map((message) => (
                <article className="einvoice-row" key={message.id}>
                  <div>
                    <strong>{message.invoiceNumber}</strong>
                    <span>{message.customerLegalName}</span>
                  </div>
                  <StatusBadge label={formatEInvoiceSyntax(message.syntax)} />
                  <span>{formatEInvoiceExchangeStatus(message.exchangeStatus)}</span>
                  <span>{formatEInvoiceCommercialStatus(message.commercialStatus)}</span>
                  <span>{formatEInvoicePaymentStatus(message.paymentStatus)}</span>
                  <code>{message.payloadSha256.slice(0, 12)}</code>
                </article>
              ))
            )}
          </div>
        </section>

        <section className="panel sif-panel" id="verifactu" aria-label="Verifactu SIF">
          <div className="panel-heading">
            <div>
              <p className="eyebrow">Verifactu</p>
              <h2>Cadena SIF</h2>
            </div>
            <Fingerprint size={20} />
          </div>

          <div className="sif-summary" aria-label="Resumen SIF">
            <div>
              <span>Registros</span>
              <strong>{sifRecords.length}</strong>
            </div>
            <div>
              <span>Ultima secuencia</span>
              <strong>{latestSifRecord?.sequenceNumber ?? 0}</strong>
            </div>
            <div>
              <span>Ultimo hash</span>
              <code>{latestSifRecord?.recordHash.slice(0, 16) ?? 'sin-registros'}</code>
            </div>
            <div>
              <span>Declaraciones</span>
              <strong>{systemDeclarations.length}</strong>
            </div>
            <div>
              <span>Ultimo borrador</span>
              <code>{latestSystemDeclaration?.payloadSha256.slice(0, 16) ?? 'sin-borrador'}</code>
            </div>
          </div>

          <div className="sif-list">
            {sifRecords.length === 0 ? (
              <div className="empty-state">Sin registros SIF en el tenant activo</div>
            ) : (
              sifRecords.map((record) => (
                <article className="sif-row" key={record.id}>
                  <strong>{record.invoiceNumber}</strong>
                  <StatusBadge label={formatSifRecordType(record.recordType)} />
                  <span>#{record.sequenceNumber}</span>
                  <code>{record.previousHash.slice(0, 10)} - {record.recordHash.slice(0, 10)}</code>
                  <small>{record.createdAt}</small>
                </article>
              ))
            )}
          </div>
        </section>

        <section className="panel document-panel" id="documents" aria-label="Centro documental">
          <div className="panel-heading">
            <div>
              <p className="eyebrow">Documentos</p>
              <h2>Centro documental</h2>
            </div>
            <FileCheck2 size={20} />
          </div>

          <form className="document-upload" onSubmit={uploadDocument}>
            <label>
              <span>Empresa</span>
              <select
                aria-label="Empresa documental"
                onChange={(event) => setDocumentForm((current) => ({ ...current, companyId: event.target.value }))}
                value={selectedDocumentCompanyId}
              >
                {companies.map((company) => (
                  <option key={company.id} value={company.id}>
                    {company.legalName}
                  </option>
                ))}
              </select>
            </label>
            <label>
              <span>Tipo</span>
              <select
                aria-label="Tipo documental"
                onChange={(event) => setDocumentForm((current) => ({ ...current, documentType: event.target.value }))}
                value={documentForm.documentType}
              >
                <option value="INVOICE_RECEIVED">Factura recibida</option>
                <option value="INVOICE_ISSUED">Factura emitida</option>
                <option value="CONTRACT">Contrato</option>
                <option value="CERTIFICATE">Certificado</option>
                <option value="TAX_REPORT">Informe fiscal</option>
                <option value="EVIDENCE">Evidencia</option>
                <option value="OTHER">Otro</option>
              </select>
            </label>
            <label>
              <span>Titulo</span>
              <input
                aria-label="Titulo documental"
                onChange={(event) => setDocumentForm((current) => ({ ...current, title: event.target.value }))}
                placeholder="Documento fiscal"
                value={documentForm.title}
              />
            </label>
            <label>
              <span>Archivo</span>
              <input aria-label="Archivo fiscal" onChange={(event) => setDocumentFile(event.target.files?.[0] ?? null)} type="file" />
            </label>
            <button className="primary-button compact" disabled={isUploadingDocument} type="submit">
              <UploadCloud size={16} />
              {isUploadingDocument ? 'Subiendo' : 'Subir'}
            </button>
          </form>
          {documentMutationMessage ? <p className="form-message">{documentMutationMessage}</p> : null}

          <div className="document-list">
            {documents.length === 0 ? (
              <div className="empty-state">Sin documentos en el tenant activo</div>
            ) : (
              documents.map((document) => (
                <article className="document-row" key={document.id}>
                  <div>
                    <strong>{document.title}</strong>
                    <span>{document.company.legalName}</span>
                  </div>
                  <span>{formatDocumentType(document.documentType)}</span>
                  <span>v{document.currentVersion}</span>
                  <small>{formatBytes(document.latestByteSize)}</small>
                  <code>{document.latestSha256.slice(0, 12)}</code>
                </article>
              ))
            )}
          </div>
        </section>
      </main>
    </div>
  )
}

function StatusBadge({ label }: { label: string }) {
  const normalized = label.toLowerCase()
  const tone = normalized.includes('activo') || normalized.includes('cliente') || normalized.includes('titular') ? 'success' : 'warning'

  return <span className={`badge ${tone}`}>{label}</span>
}

function formatRole(role?: string) {
  if (!role) {
    return 'Sin rol'
  }
  return role
    .split('_')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ')
}

function formatRelationship(value: string) {
  const labels: Record<string, string> = {
    OWNER: 'Titular',
    CLIENT: 'Cliente',
    SUPPLIER: 'Proveedor',
  }
  return labels[value] ?? value
}

function formatRelationshipKind(value: string) {
  const labels: Record<string, string> = {
    CLIENT_MANAGEMENT: 'Gestion cliente',
    SUPPLIER_PORTAL: 'Portal proveedor',
    GROUP_COMPANY: 'Grupo empresarial',
    ADVISORY: 'Asesoria',
    DOCUMENT_EXCHANGE: 'Intercambio documental',
  }
  return labels[value] ?? value
}

function formatStatus(value: string) {
  return value === 'ACTIVE' ? 'Activo' : value
}

function formatDocumentType(value: string) {
  const labels: Record<string, string> = {
    INVOICE_ISSUED: 'Factura emitida',
    INVOICE_RECEIVED: 'Factura recibida',
    CONTRACT: 'Contrato',
    CERTIFICATE: 'Certificado',
    TAX_REPORT: 'Informe fiscal',
    EVIDENCE: 'Evidencia',
    OTHER: 'Otro',
  }
  return labels[value] ?? value
}

function formatInvoiceType(value: string) {
  const labels: Record<string, string> = {
    ISSUED: 'Emitida',
    RECEIVED: 'Recibida',
    RECTIFYING: 'Rectificativa',
  }
  return labels[value] ?? value
}

function formatInvoiceStatus(value: string) {
  const labels: Record<string, string> = {
    DRAFT: 'Borrador',
    ISSUED: 'Emitida',
    RECTIFIED: 'Rectificada',
    CANCELLED: 'Anulada',
  }
  return labels[value] ?? value
}

function formatSifRecordType(value: string) {
  const labels: Record<string, string> = {
    REGISTRATION: 'Alta',
    CANCELLATION: 'Anulacion',
  }
  return labels[value] ?? value
}

function formatEInvoiceSyntax(value?: string) {
  const labels: Record<string, string> = {
    UBL: 'UBL',
    FACTURAE: 'Facturae',
  }
  return value ? labels[value] ?? value : 'Sin mensaje'
}

function formatEInvoiceExchangeStatus(value?: string) {
  const labels: Record<string, string> = {
    GENERATED: 'Generada',
    SENT: 'Enviada',
    RECEIVED: 'Recibida',
  }
  return value ? labels[value] ?? value : 'Sin intercambio'
}

function formatEInvoiceCommercialStatus(value?: string) {
  const labels: Record<string, string> = {
    PENDING: 'Pendiente',
    ACCEPTED: 'Aceptada',
    REJECTED: 'Rechazada',
  }
  return value ? labels[value] ?? value : 'Sin estado'
}

function formatEInvoicePaymentStatus(value?: string) {
  const labels: Record<string, string> = {
    UNPAID: 'Pendiente',
    PARTIALLY_PAID: 'Pago parcial',
    PAID: 'Pagada',
  }
  return value ? labels[value] ?? value : 'Sin pago'
}

function formatCurrency(value: number) {
  return new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR',
    maximumFractionDigits: 2,
  }).format(value)
}

function formatBytes(value: number) {
  if (value < 1024) {
    return `${value} B`
  }
  return `${(value / 1024).toFixed(1)} KB`
}

export default App
