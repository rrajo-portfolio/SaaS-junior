import { type ChangeEvent, type FormEvent, useEffect, useMemo, useState } from 'react'
import {
  Activity,
  AlertTriangle,
  Building2,
  CheckCircle2,
  Download,
  FileCheck2,
  FileText,
  Files,
  Fingerprint,
  Gauge,
  LockKeyhole,
  Network,
  Plus,
  RefreshCcw,
  Save,
  Search,
  Server,
  ShieldCheck,
  UploadCloud,
  Users,
} from 'lucide-react'
import { UserManager, WebStorageStateStore, type User } from 'oidc-client-ts'
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

type TenantAdminSummary = {
  id: string
  slug: string
  displayName: string
  status: string
  planCode: string
  subscriptionStatus: string
}

type SubscriptionPlanSummary = {
  code: string
  displayName: string
  status: string
  monthlyPriceCents: number
  currency: string
  maxUsers: number
  maxDocuments: number
  maxInvoices: number
  includesVerifactu: boolean
  includesEinvoice: boolean
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

type DocumentEvent = {
  id: string
  tenantId: string
  documentId: string
  eventType: string
  details: string
  eventAt: string
}

type InvoiceLine = {
  lineNumber: number
  description: string
  quantity: number
  unitPrice: number
  taxRate: number
  discountPercent?: number
  withholdingPercent?: number
  withholdingAmount?: number
  taxCategory?: string
  lineBase: number
  taxAmount: number
  lineTotal: number
}

type InvoiceSummary = {
  id: string
  tenantId: string
  issuerCompany: CompanySummary
  customerCompany: CompanySummary
  customerId?: string
  invoiceNumber: string
  fiscalNumber?: string
  seriesCode?: string
  invoiceType: string
  status: string
  issueDate: string
  dueDate?: string
  issuedAt?: string
  currency: string
  taxableBase: number
  taxTotal: number
  withholdingTotal?: number
  grossTotal?: number
  netTotal?: number
  payableTotal?: number
  total: number
  paymentStatus?: string
  paidAmount?: number
  outstandingAmount?: number
  customerSnapshot?: string
  issuerFiscalSnapshot?: string
  totalsSnapshot?: string
  cancellationReason?: string
  rectifiesInvoiceId?: string
  lines?: InvoiceLine[]
}

type FiscalSettings = {
  id: string
  companyId: string
  legalName: string
  tradeName?: string
  nif: string
  vatNumber?: string
  addressLine1: string
  addressLine2?: string
  city: string
  province?: string
  postalCode: string
  country: string
  defaultCurrency: string
  defaultPaymentTermsDays: number
  defaultVatRate: number
  defaultLanguage: string
  pdfTemplate: string
  sifMode: string
  verifactuLabelEnabled: boolean
}

type InvoiceSeries = {
  id: string
  companyId: string
  code: string
  prefix: string
  nextNumber: number
  padding: number
  active: boolean
}

type CustomerSummary = {
  id: string
  companyId: string
  customerType: string
  name: string
  nif: string
  vatNumber?: string
  email?: string
  phone?: string
  addressLine1: string
  addressLine2?: string
  city: string
  province?: string
  postalCode: string
  country: string
  status: string
}

type AuditEventSummary = {
  id: string
  companyId: string
  actorEmail: string
  eventType: string
  entityType: string
  entityId: string
  details?: string
  eventHash: string
  occurredAt: string
}

type InvoicePaymentSummary = {
  id: string
  invoiceId: string
  amount: number
  paymentDate: string
  method: string
  reference?: string
  notes?: string
}

type EvidenceExportSummary = {
  id: string
  companyId: string
  status: string
  exportType: string
  sha256: string
  createdAt: string
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
  status: string
}

type DocumentFormState = {
  documentType: string
  title: string
}

type InvoiceLineForm = {
  description: string
  quantity: string
  unitPrice: string
  taxRate: string
  discountPercent: string
  withholdingPercent: string
}

type InvoiceFormState = {
  issuerCompanyId: string
  customerCompanyId: string
  customerId: string
  invoiceNumber: string
  invoiceType: string
  issueDate: string
  dueDate: string
  currency: string
  lines: InvoiceLineForm[]
}

type CustomerFormState = {
  customerType: string
  name: string
  nif: string
  email: string
  addressLine1: string
  city: string
  province: string
  postalCode: string
  country: string
}

type FiscalSettingsFormState = {
  legalName: string
  tradeName: string
  nif: string
  vatNumber: string
  addressLine1: string
  city: string
  province: string
  postalCode: string
  country: string
  defaultCurrency: string
  defaultPaymentTermsDays: string
  defaultVatRate: string
  pdfTemplate: string
  sifMode: string
}

type SeriesFormState = {
  code: string
  prefix: string
  nextNumber: string
  padding: string
  active: boolean
}

type HealthState =
  | { label: 'Comprobando'; tone: 'warning' }
  | { label: 'Operativo'; tone: 'success'; payload: ApiHealth }
  | { label: 'Sin conexion'; tone: 'danger' }

const apiRoot = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api').replace(/\/$/, '')
const demoUserEmail = 'ana.admin@fiscalsaas.local'
const authMode = String(import.meta.env.VITE_LOGIN_MODE ?? import.meta.env.VITE_AUTH_MODE ?? 'demo').toLowerCase()
const oidcEnabled = authMode === 'oidc'
const oidcManager = oidcEnabled
  ? new UserManager({
      authority: import.meta.env.VITE_OIDC_AUTHORITY ?? 'http://localhost:18081/realms/fiscal-saas',
      client_id: import.meta.env.VITE_OIDC_CLIENT_ID ?? 'fiscal-saas-frontend',
      redirect_uri: import.meta.env.VITE_OIDC_REDIRECT_URI ?? window.location.origin,
      post_logout_redirect_uri: import.meta.env.VITE_OIDC_POST_LOGOUT_REDIRECT_URI ?? window.location.origin,
      response_type: 'code',
      scope: import.meta.env.VITE_OIDC_SCOPE ?? 'openid profile email',
      userStore: new WebStorageStateStore({ store: window.sessionStorage }),
    })
  : null

const initialCompanyForm: CompanyFormState = {
  legalName: '',
  taxId: '',
  countryCode: 'ES',
  relationshipType: 'CLIENT',
  status: 'ACTIVE',
}

const initialDocumentForm: DocumentFormState = {
  documentType: 'INVOICE_RECEIVED',
  title: '',
}

const initialInvoiceLine: InvoiceLineForm = {
  description: 'Servicio demo',
  quantity: '1',
  unitPrice: '100',
  taxRate: '21',
  discountPercent: '0',
  withholdingPercent: '0',
}

const initialCustomerForm: CustomerFormState = {
  customerType: 'COMPANY',
  name: '',
  nif: '',
  email: '',
  addressLine1: '',
  city: '',
  province: '',
  postalCode: '',
  country: 'ES',
}

const initialFiscalSettingsForm: FiscalSettingsFormState = {
  legalName: '',
  tradeName: '',
  nif: '',
  vatNumber: '',
  addressLine1: '',
  city: '',
  province: '',
  postalCode: '',
  country: 'ES',
  defaultCurrency: 'EUR',
  defaultPaymentTermsDays: '30',
  defaultVatRate: '21',
  pdfTemplate: 'standard',
  sifMode: 'LOCAL_ONLY',
}

const initialSeriesForm: SeriesFormState = {
  code: String(new Date().getFullYear()),
  prefix: `F-${new Date().getFullYear()}-`,
  nextNumber: '1',
  padding: '6',
  active: true,
}

function apiUrl(path: string) {
  return `${apiRoot}${path}`
}

function authHeaders(tenantId?: string, accessToken?: string | null) {
  const headers: Record<string, string> = oidcEnabled && accessToken
    ? { Authorization: `Bearer ${accessToken}` }
    : { 'X-User-Email': demoUserEmail }

  if (tenantId) {
    headers['X-Tenant-Id'] = tenantId
  }

  return headers
}

function blankInvoiceForm(issuerCompanyId = '', customerCompanyId = ''): InvoiceFormState {
  return {
    issuerCompanyId,
    customerCompanyId,
    customerId: '',
    invoiceNumber: `F${new Date().getFullYear()}-${Math.floor(Date.now() / 1000).toString().slice(-6)}`,
    invoiceType: 'STANDARD',
    issueDate: new Date().toISOString().slice(0, 10),
    dueDate: '',
    currency: 'EUR',
    lines: [{ ...initialInvoiceLine }],
  }
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
  const [fiscalSettings, setFiscalSettings] = useState<FiscalSettings | null>(null)
  const [invoiceSeries, setInvoiceSeries] = useState<InvoiceSeries[]>([])
  const [customers, setCustomers] = useState<CustomerSummary[]>([])
  const [auditEvents, setAuditEvents] = useState<AuditEventSummary[]>([])
  const [invoicePayments, setInvoicePayments] = useState<InvoicePaymentSummary[]>([])
  const [evidenceExports, setEvidenceExports] = useState<EvidenceExportSummary[]>([])
  const [sifRecords, setSifRecords] = useState<SifRecordSummary[]>([])
  const [systemDeclarations, setSystemDeclarations] = useState<SifSystemDeclarationSummary[]>([])
  const [einvoices, setEinvoices] = useState<EInvoiceSummary[]>([])
  const [subscriptionPlans, setSubscriptionPlans] = useState<SubscriptionPlanSummary[]>([])
  const [platformTenants, setPlatformTenants] = useState<TenantAdminSummary[]>([])
  const [companyForm, setCompanyForm] = useState<CompanyFormState>(initialCompanyForm)
  const [companyEditForm, setCompanyEditForm] = useState<CompanyFormState>(initialCompanyForm)
  const [companySearch, setCompanySearch] = useState('')
  const [selectedCompanyId, setSelectedCompanyId] = useState('')
  const [documentForm, setDocumentForm] = useState<DocumentFormState>(initialDocumentForm)
  const [documentFile, setDocumentFile] = useState<File | null>(null)
  const [documentEvents, setDocumentEvents] = useState<Record<string, DocumentEvent[]>>({})
  const [invoiceForm, setInvoiceForm] = useState<InvoiceFormState>(blankInvoiceForm())
  const [customerForm, setCustomerForm] = useState<CustomerFormState>(initialCustomerForm)
  const [fiscalSettingsForm, setFiscalSettingsForm] = useState<FiscalSettingsFormState>(initialFiscalSettingsForm)
  const [seriesForm, setSeriesForm] = useState<SeriesFormState>(initialSeriesForm)
  const [paymentAmount, setPaymentAmount] = useState('')
  const [editingInvoiceId, setEditingInvoiceId] = useState<string | null>(null)
  const [selectedInvoiceId, setSelectedInvoiceId] = useState<string>('')
  const [invoiceStatusFilter, setInvoiceStatusFilter] = useState('')
  const [invoiceSearch, setInvoiceSearch] = useState('')
  const [isTenantDataLoading, setIsTenantDataLoading] = useState(false)
  const [isSubmittingCompany, setIsSubmittingCompany] = useState(false)
  const [isUploadingDocument, setIsUploadingDocument] = useState(false)
  const [isSavingInvoice, setIsSavingInvoice] = useState(false)
  const [identityError, setIdentityError] = useState<string | null>(null)
  const [companyMutationMessage, setCompanyMutationMessage] = useState<string | null>(null)
  const [documentMutationMessage, setDocumentMutationMessage] = useState<string | null>(null)
  const [invoiceMutationMessage, setInvoiceMutationMessage] = useState<string | null>(null)
  const [fiscalMutationMessage, setFiscalMutationMessage] = useState<string | null>(null)
  const [customerMutationMessage, setCustomerMutationMessage] = useState<string | null>(null)
  const [settingsMutationMessage, setSettingsMutationMessage] = useState<string | null>(null)
  const [exportMutationMessage, setExportMutationMessage] = useState<string | null>(null)
  const [oidcUser, setOidcUser] = useState<User | null>(null)
  const accessToken = oidcUser?.access_token ?? null

  const activeTenant = useMemo(
    () => tenants.find((tenant) => tenant.id === activeTenantId) ?? tenants[0],
    [activeTenantId, tenants],
  )
  const selectedCompany = useMemo(
    () => companies.find((company) => company.id === selectedCompanyId) ?? companies[0],
    [companies, selectedCompanyId],
  )
  const ownerCompany = useMemo(
    () => companies.find((company) => company.relationshipType === 'OWNER') ?? companies[0],
    [companies],
  )
  const companyDocuments = useMemo(
    () => documents.filter((document) => document.company.id === selectedCompany?.id),
    [documents, selectedCompany],
  )
  const companyInvoices = useMemo(() => {
    const text = invoiceSearch.trim().toLowerCase()
    return invoices
      .filter((invoice) => (
        invoice.issuerCompany.id === selectedCompany?.id || invoice.customerCompany.id === selectedCompany?.id
      ))
      .filter((invoice) => !invoiceStatusFilter || invoice.status === invoiceStatusFilter)
      .filter((invoice) => (
        !text ||
        invoice.invoiceNumber.toLowerCase().includes(text) ||
        invoice.issuerCompany.legalName.toLowerCase().includes(text) ||
        invoice.customerCompany.legalName.toLowerCase().includes(text)
      ))
  }, [invoices, invoiceSearch, invoiceStatusFilter, selectedCompany])
  const selectedInvoice = useMemo(
    () => companyInvoices.find((invoice) => invoice.id === selectedInvoiceId) ?? companyInvoices[0],
    [companyInvoices, selectedInvoiceId],
  )
  const selectedInvoiceEinvoice = useMemo(
    () => einvoices.find((message) => message.invoiceId === selectedInvoice?.id),
    [einvoices, selectedInvoice],
  )
  const selectedInvoiceSif = useMemo(
    () => sifRecords.find((record) => record.invoiceId === selectedInvoice?.id && record.recordType === 'REGISTRATION'),
    [selectedInvoice, sifRecords],
  )
  const invoiceTotals = useMemo(() => {
    return invoiceForm.lines.reduce(
      (totals, line) => {
        const quantity = toNumber(line.quantity)
        const unitPrice = toNumber(line.unitPrice)
        const taxRate = toNumber(line.taxRate)
        const discountPercent = toNumber(line.discountPercent)
        const withholdingPercent = toNumber(line.withholdingPercent)
        const gross = roundMoney(quantity * unitPrice)
        const base = roundMoney(gross - gross * (discountPercent / 100))
        const tax = roundMoney(base * (taxRate / 100))
        const withholding = roundMoney(base * (withholdingPercent / 100))
        return {
          base: roundMoney(totals.base + base),
          tax: roundMoney(totals.tax + tax),
          withholding: roundMoney(totals.withholding + withholding),
          total: roundMoney(totals.total + base + tax - withholding),
        }
      },
      { base: 0, tax: 0, withholding: 0, total: 0 },
    )
  }, [invoiceForm.lines])

  useEffect(() => {
    const controller = new AbortController()
    fetch(apiUrl('/health'), { signal: controller.signal })
      .then((response) => {
        if (!response.ok) {
          throw new Error('Backend health check failed')
        }
        return response.json() as Promise<ApiHealth>
      })
      .then((payload) => setHealth({ label: 'Operativo', tone: 'success', payload }))
      .catch((error: unknown) => {
        if (!(error instanceof DOMException && error.name === 'AbortError')) {
          setHealth({ label: 'Sin conexion', tone: 'danger' })
        }
      })

    return () => controller.abort()
  }, [])

  useEffect(() => {
    if (!oidcEnabled) {
      return
    }

    let mounted = true
    async function initializeOidc() {
      try {
        if (!oidcManager) {
          return
        }
        const params = new URLSearchParams(window.location.search)
        const isCallback = params.has('code') && params.has('state')
        const user = isCallback ? await oidcManager.signinRedirectCallback() : await oidcManager.getUser()
        if (isCallback) {
          window.history.replaceState({}, document.title, window.location.pathname)
        }
        if (mounted) {
          setOidcUser(user && !user.expired ? user : null)
        }
      } catch {
        if (mounted) {
          setOidcUser(null)
          setIdentityError('Sesion OIDC no disponible')
        }
      }
    }

    void initializeOidc()
    return () => {
      mounted = false
    }
  }, [])

  useEffect(() => {
    if (oidcEnabled && !accessToken) {
      return
    }

    const controller = new AbortController()
    Promise.all([
      fetch(apiUrl('/me'), { headers: authHeaders(undefined, accessToken), signal: controller.signal }),
      fetch(apiUrl('/tenants'), { headers: authHeaders(undefined, accessToken), signal: controller.signal }),
      fetch(apiUrl('/platform/plans'), { headers: authHeaders(undefined, accessToken), signal: controller.signal }),
    ])
      .then(async ([meResponse, tenantsResponse, plansResponse]) => {
        if (!meResponse.ok || !tenantsResponse.ok || !plansResponse.ok) {
          throw new Error('Identity bootstrap failed')
        }
        const currentUser = (await meResponse.json()) as CurrentUser
        const tenantList = (await tenantsResponse.json()) as TenantSummary[]
        const plans = (await plansResponse.json()) as SubscriptionPlanSummary[]
        setMe(currentUser)
        setTenants(tenantList)
        setSubscriptionPlans(plans)
        setActiveTenantId((current) => current || tenantList[0]?.id || '')
        if (currentUser.user.roles.includes('platform_admin')) {
          const platformTenantResponse = await fetch(apiUrl('/platform/tenants'), {
            headers: authHeaders(undefined, accessToken),
            signal: controller.signal,
          })
          if (platformTenantResponse.ok) {
            setPlatformTenants((await platformTenantResponse.json()) as TenantAdminSummary[])
          }
        }
      })
      .catch((error: unknown) => {
        if (!(error instanceof DOMException && error.name === 'AbortError')) {
          setIdentityError('Identidad no disponible')
        }
      })

    return () => controller.abort()
  }, [accessToken])

  async function loadTenantData(signal?: AbortSignal) {
    if (!activeTenantId) {
      return
    }

    setIsTenantDataLoading(true)
    const companyParams = new URLSearchParams()
    if (companySearch.trim()) {
      companyParams.set('search', companySearch.trim())
    }
    const companyPath = `/tenants/${activeTenantId}/companies${companyParams.size ? `?${companyParams.toString()}` : ''}`

    try {
      const [
        companiesResponse,
        relationshipsResponse,
        documentsResponse,
        invoicesResponse,
        einvoicesResponse,
        sifRecordsResponse,
        declarationsResponse,
      ] = await Promise.all([
        fetch(apiUrl(companyPath), { headers: authHeaders(activeTenantId, accessToken), signal }),
        fetch(apiUrl(`/tenants/${activeTenantId}/business-relationships`), {
          headers: authHeaders(activeTenantId, accessToken),
          signal,
        }),
        fetch(apiUrl(`/tenants/${activeTenantId}/documents`), { headers: authHeaders(activeTenantId, accessToken), signal }),
        fetch(apiUrl(`/tenants/${activeTenantId}/invoices`), { headers: authHeaders(activeTenantId, accessToken), signal }),
        fetch(apiUrl(`/tenants/${activeTenantId}/einvoices`), { headers: authHeaders(activeTenantId, accessToken), signal }),
        fetch(apiUrl(`/tenants/${activeTenantId}/verifactu/records`), {
          headers: authHeaders(activeTenantId, accessToken),
          signal,
        }),
        fetch(apiUrl(`/tenants/${activeTenantId}/verifactu/system-declarations/drafts`), {
          headers: authHeaders(activeTenantId, accessToken),
          signal,
        }),
      ])

      if (
        !companiesResponse.ok ||
        !relationshipsResponse.ok ||
        !documentsResponse.ok ||
        !invoicesResponse.ok ||
        !einvoicesResponse.ok ||
        !sifRecordsResponse.ok ||
        !declarationsResponse.ok
      ) {
        throw new Error('Tenant scope failed')
      }

      const tenantCompanies = (await companiesResponse.json()) as CompanySummary[]
      setCompanies(tenantCompanies)
      setRelationships((await relationshipsResponse.json()) as BusinessRelationship[])
      setDocuments((await documentsResponse.json()) as DocumentSummary[])
      setInvoices((await invoicesResponse.json()) as InvoiceSummary[])
      setEinvoices((await einvoicesResponse.json()) as EInvoiceSummary[])
      setSifRecords((await sifRecordsResponse.json()) as SifRecordSummary[])
      setSystemDeclarations((await declarationsResponse.json()) as SifSystemDeclarationSummary[])
      const nextCompany = tenantCompanies.find((company) => company.id === selectedCompanyId) ?? tenantCompanies[0]
      if (nextCompany) {
        applySelectedCompany(nextCompany, false)
      } else {
        setSelectedCompanyId('')
      }
      setIdentityError(null)
    } catch (error: unknown) {
      if (!(error instanceof DOMException && error.name === 'AbortError')) {
        setIdentityError('Tenant no accesible')
      }
    } finally {
      setIsTenantDataLoading(false)
    }
  }

  function applySelectedCompany(company: CompanySummary, resetInvoice = true) {
    setSelectedCompanyId(company.id)
    setCompanyEditForm({
      legalName: company.legalName,
      taxId: company.taxId,
      countryCode: company.countryCode,
      relationshipType: company.relationshipType,
      status: company.status,
    })
    if (resetInvoice) {
      setSelectedInvoiceId('')
      setEditingInvoiceId(null)
      setInvoiceForm(blankInvoiceForm(ownerCompany?.id || company.id, company.id))
    }
  }

  async function loadCompanyScope(companyId: string, signal?: AbortSignal) {
    if (!activeTenantId || !companyId) {
      return
    }

    const headers = authHeaders(activeTenantId, accessToken)
    const safeJson = async <T,>(response: Response, fallback: T): Promise<T> => (
      response.ok ? await response.json() as T : fallback
    )

    const [settingsResponse, seriesResponse, customersResponse, auditResponse, exportsResponse] = await Promise.all([
      fetch(apiUrl(`/tenants/${activeTenantId}/companies/${companyId}/fiscal-settings`), { headers, signal }),
      fetch(apiUrl(`/tenants/${activeTenantId}/companies/${companyId}/invoice-series`), { headers, signal }),
      fetch(apiUrl(`/tenants/${activeTenantId}/companies/${companyId}/customers`), { headers, signal }),
      fetch(apiUrl(`/tenants/${activeTenantId}/companies/${companyId}/audit-events`), { headers, signal }),
      fetch(apiUrl(`/tenants/${activeTenantId}/companies/${companyId}/exports`), { headers, signal }),
    ])

    const settings = await safeJson<FiscalSettings | null>(settingsResponse, null)
    const series = await safeJson<InvoiceSeries[]>(seriesResponse, [])
    const customerList = await safeJson<CustomerSummary[]>(customersResponse, [])
    setFiscalSettings(settings)
    setInvoiceSeries(series)
    setCustomers(customerList)
    setAuditEvents(await safeJson<AuditEventSummary[]>(auditResponse, []))
    setEvidenceExports(await safeJson<EvidenceExportSummary[]>(exportsResponse, []))
    setFiscalSettingsForm(settings ? fiscalSettingsToForm(settings) : {
      ...initialFiscalSettingsForm,
      legalName: selectedCompany?.legalName ?? '',
      tradeName: selectedCompany?.legalName ?? '',
      nif: selectedCompany?.taxId ?? '',
    })
  }

  useEffect(() => {
    if (!activeTenantId || !selectedCompany?.id || (oidcEnabled && !accessToken)) {
      return
    }
    const controller = new AbortController()
    // eslint-disable-next-line react-hooks/set-state-in-effect
    void loadCompanyScope(selectedCompany.id, controller.signal).catch(() => {
      setFiscalSettings(null)
      setInvoiceSeries([])
      setCustomers([])
      setAuditEvents([])
      setEvidenceExports([])
    })
    return () => controller.abort()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeTenantId, selectedCompany?.id, accessToken])

  useEffect(() => {
    if (!activeTenantId || !selectedInvoice?.id || (oidcEnabled && !accessToken)) {
      return
    }
    const controller = new AbortController()
    fetch(apiUrl(`/tenants/${activeTenantId}/invoices/${selectedInvoice.id}/payments`), {
      headers: authHeaders(activeTenantId, accessToken),
      signal: controller.signal,
    })
      .then(async (response) => {
        setInvoicePayments(response.ok ? await response.json() as InvoicePaymentSummary[] : [])
      })
      .catch(() => setInvoicePayments([]))
    return () => controller.abort()
  }, [activeTenantId, selectedInvoice?.id, accessToken])

  useEffect(() => {
    if (!activeTenantId || (oidcEnabled && !accessToken)) {
      return
    }

    const controller = new AbortController()
    // eslint-disable-next-line react-hooks/set-state-in-effect
    void loadTenantData(controller.signal)
    return () => controller.abort()
    // loadTenantData is a component action; dependencies above are the fetch inputs.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [accessToken, activeTenantId, companySearch])

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
          ...authHeaders(activeTenantId, accessToken),
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          legalName: companyForm.legalName,
          taxId: companyForm.taxId,
          countryCode: companyForm.countryCode,
          relationshipType: companyForm.relationshipType,
        }),
      })
      if (!response.ok) {
        throw new Error('Company creation failed')
      }
      const createdCompany = (await response.json()) as CompanySummary
      setCompanies((current) => [...current, createdCompany].sort((a, b) => a.legalName.localeCompare(b.legalName)))
      applySelectedCompany(createdCompany)
      setCompanyForm(initialCompanyForm)
      setCompanyMutationMessage('Empresa registrada y seleccionada')
    } catch {
      setCompanyMutationMessage('No se pudo registrar la empresa')
    } finally {
      setIsSubmittingCompany(false)
    }
  }

  async function updateCompany(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!activeTenantId || !selectedCompany) {
      return
    }

    setCompanyMutationMessage(null)
    try {
      const response = await fetch(apiUrl(`/tenants/${activeTenantId}/companies/${selectedCompany.id}`), {
        method: 'PATCH',
        headers: {
          ...authHeaders(activeTenantId, accessToken),
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(companyEditForm),
      })
      if (!response.ok) {
        throw new Error('Company update failed')
      }
      const updatedCompany = (await response.json()) as CompanySummary
      setCompanies((current) => current.map((company) => (company.id === updatedCompany.id ? updatedCompany : company)))
      setCompanyMutationMessage('Empresa actualizada')
    } catch {
      setCompanyMutationMessage('No se pudo actualizar la empresa')
    }
  }

  async function deactivateCompany() {
    if (!activeTenantId || !selectedCompany) {
      return
    }

    setCompanyMutationMessage(null)
    try {
      const response = await fetch(apiUrl(`/tenants/${activeTenantId}/companies/${selectedCompany.id}`), {
        method: 'DELETE',
        headers: authHeaders(activeTenantId, accessToken),
      })
      if (!response.ok) {
        throw new Error('Company deactivate failed')
      }
      setCompanies((current) => current.map((company) => (
        company.id === selectedCompany.id ? { ...company, status: 'INACTIVE' } : company
      )))
      setCompanyMutationMessage('Empresa desactivada')
    } catch {
      setCompanyMutationMessage('No se pudo desactivar la empresa')
    }
  }

  async function saveFiscalSettings(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!activeTenantId || !selectedCompany) {
      return
    }

    setSettingsMutationMessage(null)
    try {
      const response = await fetch(apiUrl(`/tenants/${activeTenantId}/companies/${selectedCompany.id}/fiscal-settings`), {
        method: 'PUT',
        headers: {
          ...authHeaders(activeTenantId, accessToken),
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          ...fiscalSettingsForm,
          defaultPaymentTermsDays: toNumber(fiscalSettingsForm.defaultPaymentTermsDays),
          defaultVatRate: toNumber(fiscalSettingsForm.defaultVatRate),
          defaultLanguage: 'es',
          verifactuLabelEnabled: false,
        }),
      })
      if (!response.ok) {
        throw new Error('Fiscal settings save failed')
      }
      const saved = await response.json() as FiscalSettings
      setFiscalSettings(saved)
      setFiscalSettingsForm(fiscalSettingsToForm(saved))
      setSettingsMutationMessage('Configuracion fiscal guardada')
      await loadCompanyScope(selectedCompany.id)
    } catch {
      setSettingsMutationMessage('No se pudo guardar configuracion fiscal')
    }
  }

  async function createInvoiceSeries(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!activeTenantId || !selectedCompany) {
      return
    }

    setSettingsMutationMessage(null)
    try {
      const response = await fetch(apiUrl(`/tenants/${activeTenantId}/companies/${selectedCompany.id}/invoice-series`), {
        method: 'POST',
        headers: {
          ...authHeaders(activeTenantId, accessToken),
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          code: seriesForm.code,
          prefix: seriesForm.prefix,
          nextNumber: toNumber(seriesForm.nextNumber),
          padding: toNumber(seriesForm.padding),
          active: seriesForm.active,
        }),
      })
      if (!response.ok) {
        throw new Error('Series creation failed')
      }
      const created = await response.json() as InvoiceSeries
      setInvoiceSeries((current) => [...current, created].sort((a, b) => a.code.localeCompare(b.code)))
      setSeriesForm(initialSeriesForm)
      setSettingsMutationMessage('Serie creada')
    } catch {
      setSettingsMutationMessage('No se pudo crear la serie')
    }
  }

  async function createCustomer(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!activeTenantId || !selectedCompany) {
      return
    }

    setCustomerMutationMessage(null)
    try {
      const response = await fetch(apiUrl(`/tenants/${activeTenantId}/companies/${selectedCompany.id}/customers`), {
        method: 'POST',
        headers: {
          ...authHeaders(activeTenantId, accessToken),
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(customerForm),
      })
      if (!response.ok) {
        throw new Error('Customer creation failed')
      }
      const created = await response.json() as CustomerSummary
      setCustomers((current) => [...current, created].sort((a, b) => a.name.localeCompare(b.name)))
      setInvoiceForm((current) => ({ ...current, customerId: created.id }))
      setCustomerForm(initialCustomerForm)
      setCustomerMutationMessage('Cliente creado y seleccionado')
    } catch {
      setCustomerMutationMessage('No se pudo crear el cliente')
    }
  }

  async function uploadDocument(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!activeTenantId || !selectedCompany || !documentFile || isUploadingDocument) {
      setDocumentMutationMessage('Selecciona una empresa y un archivo')
      return
    }

    setIsUploadingDocument(true)
    setDocumentMutationMessage(null)
    const formData = new FormData()
    formData.set('companyId', selectedCompany.id)
    formData.set('documentType', documentForm.documentType)
    formData.set('title', documentForm.title || documentFile.name)
    formData.set('file', documentFile)

    try {
      const response = await fetch(apiUrl(`/tenants/${activeTenantId}/documents`), {
        method: 'POST',
        headers: authHeaders(activeTenantId, accessToken),
        body: formData,
      })
      if (!response.ok) {
        throw new Error('Document upload failed')
      }
      const createdDocument = (await response.json()) as DocumentSummary
      setDocuments((current) => [createdDocument, ...current])
      setDocumentForm(initialDocumentForm)
      setDocumentFile(null)
      setDocumentMutationMessage('Documento subido a la empresa seleccionada')
    } catch {
      setDocumentMutationMessage('No se pudo subir el documento')
    } finally {
      setIsUploadingDocument(false)
    }
  }

  async function downloadDocument(document: DocumentSummary) {
    if (!activeTenantId) {
      return
    }

    const response = await fetch(apiUrl(`/tenants/${activeTenantId}/documents/${document.id}/download`), {
      headers: authHeaders(activeTenantId, accessToken),
    })
    if (!response.ok) {
      setDocumentMutationMessage('No se pudo descargar el documento')
      return
    }
    const blob = await response.blob()
    const href = URL.createObjectURL(blob)
    const link = window.document.createElement('a')
    link.href = href
    link.download = document.latestFilename || document.title
    link.click()
    URL.revokeObjectURL(href)
  }

  async function loadDocumentEvents(document: DocumentSummary) {
    if (!activeTenantId) {
      return
    }

    const response = await fetch(apiUrl(`/tenants/${activeTenantId}/documents/${document.id}/events`), {
      headers: authHeaders(activeTenantId, accessToken),
    })
    if (!response.ok) {
      setDocumentMutationMessage('No se pudieron cargar eventos')
      return
    }
    const events = (await response.json()) as DocumentEvent[]
    setDocumentEvents((current) => ({
      ...current,
      [document.id]: events,
    }))
  }

  async function saveInvoice(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!activeTenantId || !selectedCompany || isSavingInvoice) {
      return
    }

    setIsSavingInvoice(true)
    setInvoiceMutationMessage(null)
    const payload = {
      ...invoiceForm,
      lines: invoiceForm.lines.map((line) => ({
        description: line.description,
        quantity: toNumber(line.quantity),
        unitPrice: toNumber(line.unitPrice),
        taxRate: toNumber(line.taxRate),
        discountPercent: toNumber(line.discountPercent),
        withholdingPercent: toNumber(line.withholdingPercent),
        taxCategory: 'STANDARD',
      })),
    }

    try {
      const response = await fetch(apiUrl(`/tenants/${activeTenantId}/invoices${editingInvoiceId ? `/${editingInvoiceId}` : ''}`), {
        method: editingInvoiceId ? 'PATCH' : 'POST',
        headers: {
          ...authHeaders(activeTenantId, accessToken),
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      })
      if (!response.ok) {
        throw new Error('Invoice save failed')
      }
      const savedInvoice = (await response.json()) as InvoiceSummary
      setInvoices((current) => {
        const exists = current.some((invoice) => invoice.id === savedInvoice.id)
        return exists
          ? current.map((invoice) => (invoice.id === savedInvoice.id ? savedInvoice : invoice))
          : [savedInvoice, ...current]
      })
      setSelectedInvoiceId(savedInvoice.id)
      setEditingInvoiceId(null)
      setInvoiceForm(blankInvoiceForm(ownerCompany?.id || selectedCompany.id, selectedCompany.id))
      setInvoiceMutationMessage(editingInvoiceId ? 'Borrador actualizado' : 'Factura borrador creada')
    } catch {
      setInvoiceMutationMessage(editingInvoiceId ? 'No se pudo editar la factura' : 'No se pudo crear la factura')
    } finally {
      setIsSavingInvoice(false)
    }
  }

  async function issueInvoice(invoice: InvoiceSummary) {
    if (!activeTenantId) {
      return
    }

    setInvoiceMutationMessage(null)
    try {
      const response = await fetch(apiUrl(`/tenants/${activeTenantId}/invoices/${invoice.id}/issue`), {
        method: 'POST',
        headers: {
          ...authHeaders(activeTenantId, accessToken),
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          seriesId: invoiceSeries.find((series) => series.active)?.id,
          issueRequestId: `ui-${invoice.id}-${Date.now()}`,
        }),
      })
      if (!response.ok) {
        throw new Error('Invoice issue failed')
      }
      const updatedInvoice = (await response.json()) as InvoiceSummary
      setInvoices((current) => current.map((item) => (item.id === updatedInvoice.id ? updatedInvoice : item)))
      setSelectedInvoiceId(updatedInvoice.id)
      setInvoiceMutationMessage('Factura emitida')
    } catch {
      setInvoiceMutationMessage('No se pudo emitir la factura')
    }
  }

  async function generateEInvoice(invoice: InvoiceSummary) {
    if (!activeTenantId) {
      return
    }

    setFiscalMutationMessage(null)
    try {
      const response = await fetch(apiUrl(`/tenants/${activeTenantId}/einvoices`), {
        method: 'POST',
        headers: {
          ...authHeaders(activeTenantId, accessToken),
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ invoiceId: invoice.id, syntax: 'UBL' }),
      })
      if (!response.ok) {
        throw new Error('E-invoice generation failed')
      }
      const message = (await response.json()) as EInvoiceSummary
      setEinvoices((current) => [message, ...current])
      setFiscalMutationMessage('E-invoice local generada')
    } catch {
      setFiscalMutationMessage('No se pudo generar e-invoice local')
    }
  }

  async function registerSif(invoice: InvoiceSummary) {
    if (!activeTenantId) {
      return
    }

    setFiscalMutationMessage(null)
    try {
      const response = await fetch(apiUrl(`/tenants/${activeTenantId}/verifactu/records`), {
        method: 'POST',
        headers: {
          ...authHeaders(activeTenantId, accessToken),
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ invoiceId: invoice.id }),
      })
      if (!response.ok) {
        throw new Error('SIF register failed')
      }
      const record = (await response.json()) as SifRecordSummary
      setSifRecords((current) => [record, ...current])
      setFiscalMutationMessage('Registro SIF local creado')
    } catch {
      setFiscalMutationMessage('No se pudo crear registro SIF local')
    }
  }

  async function downloadInvoicePdf(invoice: InvoiceSummary) {
    if (!activeTenantId) {
      return
    }

    const response = await fetch(apiUrl(`/tenants/${activeTenantId}/invoices/${invoice.id}/pdf`), {
      headers: authHeaders(activeTenantId, accessToken),
    })
    if (!response.ok) {
      setInvoiceMutationMessage('PDF disponible solo tras emitir')
      return
    }
    const blob = await response.blob()
    const href = URL.createObjectURL(blob)
    const link = window.document.createElement('a')
    link.href = href
    link.download = `${invoice.fiscalNumber || invoice.invoiceNumber}.pdf`
    link.click()
    URL.revokeObjectURL(href)
    setInvoiceMutationMessage('PDF descargado')
    if (selectedCompany) {
      await loadCompanyScope(selectedCompany.id)
    }
  }

  async function registerPayment(invoice: InvoiceSummary) {
    if (!activeTenantId || !paymentAmount.trim()) {
      return
    }

    setInvoiceMutationMessage(null)
    try {
      const response = await fetch(apiUrl(`/tenants/${activeTenantId}/invoices/${invoice.id}/payments`), {
        method: 'POST',
        headers: {
          ...authHeaders(activeTenantId, accessToken),
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          amount: toNumber(paymentAmount),
          method: 'BANK_TRANSFER',
          paymentDate: new Date().toISOString().slice(0, 10),
          reference: 'Pago manual local/preprod',
        }),
      })
      if (!response.ok) {
        throw new Error('Payment failed')
      }
      const payment = await response.json() as InvoicePaymentSummary
      setInvoicePayments((current) => [payment, ...current])
      const refreshed = await fetch(apiUrl(`/tenants/${activeTenantId}/invoices/${invoice.id}`), {
        headers: authHeaders(activeTenantId, accessToken),
      })
      if (refreshed.ok) {
        const updatedInvoice = await refreshed.json() as InvoiceSummary
        setInvoices((current) => current.map((item) => (item.id === updatedInvoice.id ? updatedInvoice : item)))
      }
      setPaymentAmount('')
      setInvoiceMutationMessage('Pago registrado')
    } catch {
      setInvoiceMutationMessage('No se pudo registrar el pago')
    }
  }

  async function cancelInvoiceLocal(invoice: InvoiceSummary) {
    if (!activeTenantId) {
      return
    }

    setInvoiceMutationMessage(null)
    try {
      const response = await fetch(apiUrl(`/tenants/${activeTenantId}/invoices/${invoice.id}/cancel-local`), {
        method: 'POST',
        headers: {
          ...authHeaders(activeTenantId, accessToken),
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ reason: 'Anulacion local/preprod desde UI' }),
      })
      if (!response.ok) {
        throw new Error('Cancel failed')
      }
      const updatedInvoice = await response.json() as InvoiceSummary
      setInvoices((current) => current.map((item) => (item.id === updatedInvoice.id ? updatedInvoice : item)))
      setInvoiceMutationMessage('Factura anulada localmente')
    } catch {
      setInvoiceMutationMessage('No se pudo anular la factura')
    }
  }

  async function createCorrectiveInvoice(invoice: InvoiceSummary) {
    if (!activeTenantId) {
      return
    }

    setInvoiceMutationMessage(null)
    try {
      const response = await fetch(apiUrl(`/tenants/${activeTenantId}/invoices/${invoice.id}/create-corrective`), {
        method: 'POST',
        headers: authHeaders(activeTenantId, accessToken),
      })
      if (!response.ok) {
        throw new Error('Corrective failed')
      }
      const corrective = await response.json() as InvoiceSummary
      setInvoices((current) => [corrective, ...current.map((item) => (
        item.id === invoice.id ? { ...item, status: 'RECTIFIED' } : item
      ))])
      setSelectedInvoiceId(corrective.id)
      setInvoiceMutationMessage('Rectificativa creada como borrador')
    } catch {
      setInvoiceMutationMessage('No se pudo crear rectificativa')
    }
  }

  async function createEvidenceExport() {
    if (!activeTenantId || !selectedCompany) {
      return
    }

    setExportMutationMessage(null)
    try {
      const response = await fetch(apiUrl(`/tenants/${activeTenantId}/companies/${selectedCompany.id}/exports/evidence-pack`), {
        method: 'POST',
        headers: authHeaders(activeTenantId, accessToken),
      })
      if (!response.ok) {
        throw new Error('Export failed')
      }
      const created = await response.json() as EvidenceExportSummary
      setEvidenceExports((current) => [created, ...current])
      setExportMutationMessage('Paquete de evidencia generado')
    } catch {
      setExportMutationMessage('No se pudo generar paquete de evidencia')
    }
  }

  async function downloadEvidenceExport(exportJob: EvidenceExportSummary) {
    if (!activeTenantId || !selectedCompany) {
      return
    }

    const response = await fetch(apiUrl(`/tenants/${activeTenantId}/companies/${selectedCompany.id}/exports/${exportJob.id}/download`), {
      headers: authHeaders(activeTenantId, accessToken),
    })
    if (!response.ok) {
      setExportMutationMessage('No se pudo descargar evidencia')
      return
    }
    const blob = await response.blob()
    const href = URL.createObjectURL(blob)
    const link = window.document.createElement('a')
    link.href = href
    link.download = `evidence-pack-${exportJob.id}.zip`
    link.click()
    URL.revokeObjectURL(href)
  }

  function editInvoice(invoice: InvoiceSummary) {
    setEditingInvoiceId(invoice.id)
    setSelectedInvoiceId(invoice.id)
    setInvoiceForm({
      issuerCompanyId: invoice.issuerCompany.id,
      customerCompanyId: invoice.customerCompany.id,
      customerId: invoice.customerId ?? '',
      invoiceNumber: invoice.invoiceNumber,
      invoiceType: invoice.invoiceType,
      issueDate: invoice.issueDate,
      dueDate: invoice.dueDate ?? '',
      currency: invoice.currency,
      lines: (invoice.lines && invoice.lines.length > 0 ? invoice.lines : [initialInvoiceLine]).map((line) => ({
        description: line.description,
        quantity: String(line.quantity),
        unitPrice: String(line.unitPrice),
        taxRate: String(line.taxRate),
        discountPercent: String(line.discountPercent ?? 0),
        withholdingPercent: String(line.withholdingPercent ?? 0),
      })),
    })
  }

  function updateInvoiceLine(index: number, patch: Partial<InvoiceLineForm>) {
    setInvoiceForm((current) => ({
      ...current,
      lines: current.lines.map((line, lineIndex) => (lineIndex === index ? { ...line, ...patch } : line)),
    }))
  }

  async function loginWithOidc() {
    await oidcManager?.signinRedirect()
  }

  async function logoutFromOidc() {
    if (oidcManager) {
      await oidcManager.signoutRedirect()
    }
  }

  const displayUserEmail = me?.user.email ?? oidcUser?.profile.email ?? demoUserEmail
  const displayUserName = me?.user.displayName ?? displayUserEmail
  const latestSystemDeclaration = systemDeclarations[0]
  const tenantInvoiceTotal = invoices.reduce((total, invoice) => total + Number(invoice.total), 0)

  if (oidcEnabled && !accessToken) {
    return (
      <main className="auth-screen">
        <section className="auth-panel">
          <div className="brand-mark" aria-hidden="true">
            <ShieldCheck size={24} />
          </div>
          <p className="eyebrow">Fiscal SaaS</p>
          <h1>Acceso seguro</h1>
          <p>OIDC activo</p>
          {identityError ? (
            <div className="warning-note" role="status">
              <AlertTriangle size={18} />
              <span>{identityError}</span>
            </div>
          ) : null}
          <button className="primary-button" onClick={loginWithOidc} type="button">
            <LockKeyhole size={18} />
            Iniciar sesion
          </button>
        </section>
      </main>
    )
  }

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
            <span>Panel</span>
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
          <a href="#einvoice">
            <FileCheck2 size={18} />
            <span>E-invoice</span>
          </a>
          <a href="#sif">
            <Fingerprint size={18} />
            <span>SIF local</span>
          </a>
        </nav>
      </aside>

      <main className="workspace" id="dashboard">
        <header className="topbar">
          <div>
            <p className="eyebrow">Tenant &gt; Empresas &gt; Fiscalidad local</p>
            <h1>SaaS fiscal operativo</h1>
          </div>
          <div className="topbar-actions">
            <label className="search-box">
              <Search size={18} />
              <input
                aria-label="Buscar empresa"
                onChange={(event) => setCompanySearch(event.target.value)}
                placeholder="Buscar empresa o NIF"
                type="search"
                value={companySearch}
              />
            </label>
            <button className="ghost-button" onClick={() => void loadTenantData()} type="button">
              <RefreshCcw size={18} />
              Actualizar
            </button>
            {oidcEnabled ? (
              <button className="ghost-button" onClick={logoutFromOidc} type="button">
                <LockKeyhole size={18} />
                Salir
              </button>
            ) : null}
          </div>
        </header>

        <section className="status-strip" aria-label="Estado de servicios">
          <div className={`service-state ${health.tone}`}>
            <Server size={18} />
            <span>{health.label}</span>
            <small>{health.tone === 'success' ? health.payload.service : apiRoot}</small>
          </div>
          <div className="service-state success">
            <Network size={18} />
            <span>{activeTenant?.name ?? 'Sin tenant'}</span>
            <small>{activeTenant ? formatRole(activeTenant.role) : 'Seleccion pendiente'}</small>
          </div>
          <div className="service-state warning">
            <ShieldCheck size={18} />
            <span>Preproduccion local</span>
            <small>Sin envio legal externo</small>
          </div>
          <div className="service-state success">
            <Activity size={18} />
            <span>{displayUserName}</span>
            <small>{displayUserEmail}</small>
          </div>
        </section>

        <section className="identity-band" id="tenants" aria-label="Contexto de tenant">
          <div>
            <p className="eyebrow">Sesion</p>
            <h2>{displayUserName}</h2>
            <span>{displayUserEmail}</span>
            <small>{oidcEnabled ? 'OIDC' : 'Demo header auth'}</small>
          </div>
          <div className="tenant-switcher" role="tablist" aria-label="Tenant activo">
            {tenants.map((tenant) => (
              <button
                aria-selected={tenant.id === activeTenantId}
                className={tenant.id === activeTenantId ? 'selected' : ''}
                key={tenant.id}
                onClick={() => {
                  setSelectedCompanyId('')
                  setSelectedInvoiceId('')
                  setEditingInvoiceId(null)
                  setActiveTenantId(tenant.id)
                }}
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
        {isTenantDataLoading ? (
          <div className="loading-strip" role="status">
            <Activity size={18} />
            Sincronizando datos del tenant
          </div>
        ) : null}

        <section className="metrics-grid" aria-label="Resumen SaaS">
          <MetricCard icon={Building2} label="Empresas" value={companies.length.toString()} trend={selectedCompany?.legalName ?? 'Sin empresa'} />
          <MetricCard icon={Users} label="Relaciones" value={relationships.length.toString()} trend="B2B por tenant" />
          <MetricCard icon={Files} label="Documentos" value={documents.length.toString()} trend={`${companyDocuments.length} en empresa`} />
          <MetricCard icon={FileText} label="Facturas" value={invoices.length.toString()} trend={formatCurrency(tenantInvoiceTotal)} />
          <MetricCard icon={FileCheck2} label="E-invoices" value={einvoices.length.toString()} trend="Generacion local" />
          <MetricCard icon={Fingerprint} label="SIF" value={sifRecords.length.toString()} trend="Cadena local" />
        </section>

        <section className="saas-workbench">
          <article className="panel companies-panel" id="companies">
            <div className="panel-heading">
              <div>
                <p className="eyebrow">Empresas del tenant</p>
                <h2>Buscar, crear y abrir detalle</h2>
              </div>
              <span className="badge success">{companies.length} visibles</span>
            </div>

            <div className="company-list" role="list" aria-label="Empresas del tenant">
              {companies.length === 0 ? (
                <div className="empty-state">No hay empresas para este criterio</div>
              ) : companies.map((company) => (
                <button
                  className={`company-list-row ${company.id === selectedCompany?.id ? 'selected' : ''}`}
                  key={company.id}
                  onClick={() => {
                    applySelectedCompany(company)
                  }}
                  type="button"
                >
                  <span>
                    <strong>{company.legalName}</strong>
                    <small>{company.taxId}</small>
                  </span>
                  <span className={badgeClass(company.status)}>{company.status}</span>
                  <ChevronText label={formatRelationship(company.relationshipType)} />
                </button>
              ))}
            </div>

            <form className="company-form" onSubmit={createCompany}>
              <div className="form-heading">
                <h3>Nueva empresa</h3>
                <button className="primary-button compact" disabled={isSubmittingCompany} type="submit">
                  <Plus size={16} />
                  Crear
                </button>
              </div>
              <div className="compact-form-grid">
                <TextInput label="Nombre legal" onChange={(value) => setCompanyForm((current) => ({ ...current, legalName: value }))} value={companyForm.legalName} />
                <TextInput label="NIF/CIF" onChange={(value) => setCompanyForm((current) => ({ ...current, taxId: value }))} value={companyForm.taxId} />
                <TextInput label="Pais" maxLength={2} onChange={(value) => setCompanyForm((current) => ({ ...current, countryCode: value.toUpperCase() }))} value={companyForm.countryCode} />
                <SelectInput
                  label="Relacion"
                  onChange={(value) => setCompanyForm((current) => ({ ...current, relationshipType: value }))}
                  options={['CLIENT', 'SUPPLIER', 'OWNER']}
                  value={companyForm.relationshipType}
                />
              </div>
              {companyMutationMessage ? <p className="form-message">{companyMutationMessage}</p> : null}
            </form>
          </article>

          <article className="panel company-detail-panel" aria-label="Detalle de empresa">
            {selectedCompany ? (
              <>
                <div className="panel-heading">
                  <div>
                    <p className="eyebrow">Detalle de empresa</p>
                    <h2>{selectedCompany.legalName}</h2>
                  </div>
                  <span className={badgeClass(selectedCompany.status)}>{selectedCompany.status}</span>
                </div>

                <form className="company-form" onSubmit={updateCompany}>
                  <div className="section-heading">
                    <h3>Datos basicos</h3>
                    <div className="button-row">
                      <button className="ghost-button" onClick={deactivateCompany} type="button">
                        Desactivar
                      </button>
                      <button className="primary-button compact" type="submit">
                        <Save size={16} />
                        Guardar
                      </button>
                    </div>
                  </div>
                  <div className="compact-form-grid">
                    <TextInput label="Nombre legal" onChange={(value) => setCompanyEditForm((current) => ({ ...current, legalName: value }))} value={companyEditForm.legalName} />
                    <TextInput label="NIF/CIF" onChange={(value) => setCompanyEditForm((current) => ({ ...current, taxId: value }))} value={companyEditForm.taxId} />
                    <TextInput label="Pais" maxLength={2} onChange={(value) => setCompanyEditForm((current) => ({ ...current, countryCode: value.toUpperCase() }))} value={companyEditForm.countryCode} />
                    <SelectInput
                      label="Relacion"
                      onChange={(value) => setCompanyEditForm((current) => ({ ...current, relationshipType: value }))}
                      options={['CLIENT', 'SUPPLIER', 'OWNER']}
                      value={companyEditForm.relationshipType}
                    />
                    <SelectInput
                      label="Estado"
                      onChange={(value) => setCompanyEditForm((current) => ({ ...current, status: value }))}
                      options={['ACTIVE', 'INACTIVE']}
                      value={companyEditForm.status}
                    />
                  </div>
                </form>

                <section className="fiscal-config-panel" aria-label="Configuracion fiscal de empresa">
                  <div className="section-heading">
                    <div>
                      <p className="eyebrow">Fiscalidad</p>
                      <h3>Datos fiscales y numeracion</h3>
                    </div>
                    <span className={fiscalSettings ? 'badge success' : 'badge warning'}>
                      {fiscalSettings ? 'Completa' : 'Pendiente'}
                    </span>
                  </div>
                  <form className="company-form" onSubmit={saveFiscalSettings}>
                    <div className="compact-form-grid">
                      <TextInput label="Razon fiscal" onChange={(value) => setFiscalSettingsForm((current) => ({ ...current, legalName: value }))} value={fiscalSettingsForm.legalName} />
                      <TextInput label="NIF fiscal" onChange={(value) => setFiscalSettingsForm((current) => ({ ...current, nif: value }))} value={fiscalSettingsForm.nif} />
                      <TextInput label="Direccion fiscal" onChange={(value) => setFiscalSettingsForm((current) => ({ ...current, addressLine1: value }))} value={fiscalSettingsForm.addressLine1} />
                      <TextInput label="Ciudad" onChange={(value) => setFiscalSettingsForm((current) => ({ ...current, city: value }))} value={fiscalSettingsForm.city} />
                      <TextInput label="Provincia" onChange={(value) => setFiscalSettingsForm((current) => ({ ...current, province: value }))} value={fiscalSettingsForm.province} />
                      <TextInput label="Codigo postal" onChange={(value) => setFiscalSettingsForm((current) => ({ ...current, postalCode: value }))} value={fiscalSettingsForm.postalCode} />
                      <TextInput label="Moneda" maxLength={3} onChange={(value) => setFiscalSettingsForm((current) => ({ ...current, defaultCurrency: value.toUpperCase() }))} value={fiscalSettingsForm.defaultCurrency} />
                      <TextInput label="Dias pago" onChange={(value) => setFiscalSettingsForm((current) => ({ ...current, defaultPaymentTermsDays: value }))} type="number" value={fiscalSettingsForm.defaultPaymentTermsDays} />
                    </div>
                    <div className="button-row">
                      <button className="primary-button compact" type="submit">
                        <Save size={16} />
                        Guardar fiscalidad
                      </button>
                    </div>
                  </form>
                  <form className="series-form" onSubmit={createInvoiceSeries}>
                    <TextInput label="Serie" onChange={(value) => setSeriesForm((current) => ({ ...current, code: value }))} value={seriesForm.code} />
                    <TextInput label="Prefijo" onChange={(value) => setSeriesForm((current) => ({ ...current, prefix: value }))} value={seriesForm.prefix} />
                    <TextInput label="Siguiente" onChange={(value) => setSeriesForm((current) => ({ ...current, nextNumber: value }))} type="number" value={seriesForm.nextNumber} />
                    <button className="ghost-button compact" type="submit">Crear serie</button>
                  </form>
                  <div className="chip-row">
                    {invoiceSeries.map((series) => (
                      <span className={series.active ? 'badge success' : 'badge warning'} key={series.id}>
                        {series.code}: {series.prefix}{String(series.nextNumber).padStart(series.padding, '0')}
                      </span>
                    ))}
                  </div>
                  {settingsMutationMessage ? <p className="form-message">{settingsMutationMessage}</p> : null}
                </section>

                <section className="customers-panel" id="customers" aria-label="Clientes de facturacion">
                  <div className="section-heading">
                    <div>
                      <p className="eyebrow">Clientes</p>
                      <h3>Destinatarios con snapshot de factura</h3>
                    </div>
                    <span>{customers.length} clientes</span>
                  </div>
                  <form className="customer-form" onSubmit={createCustomer}>
                    <TextInput label="Cliente" onChange={(value) => setCustomerForm((current) => ({ ...current, name: value }))} value={customerForm.name} />
                    <TextInput label="NIF cliente" onChange={(value) => setCustomerForm((current) => ({ ...current, nif: value }))} value={customerForm.nif} />
                    <TextInput label="Email cliente" onChange={(value) => setCustomerForm((current) => ({ ...current, email: value }))} value={customerForm.email} />
                    <TextInput label="Direccion cliente" onChange={(value) => setCustomerForm((current) => ({ ...current, addressLine1: value }))} value={customerForm.addressLine1} />
                    <TextInput label="Ciudad cliente" onChange={(value) => setCustomerForm((current) => ({ ...current, city: value }))} value={customerForm.city} />
                    <TextInput label="CP cliente" onChange={(value) => setCustomerForm((current) => ({ ...current, postalCode: value }))} value={customerForm.postalCode} />
                    <button className="primary-button compact" type="submit">
                      <Plus size={16} />
                      Crear cliente
                    </button>
                  </form>
                  <div className="chip-row">
                    {customers.map((customer) => (
                      <button
                        className={invoiceForm.customerId === customer.id ? 'chip selected' : 'chip'}
                        key={customer.id}
                        onClick={() => setInvoiceForm((current) => ({ ...current, customerId: customer.id }))}
                        type="button"
                      >
                        {customer.name} - {customer.nif}
                      </button>
                    ))}
                  </div>
                  {customerMutationMessage ? <p className="form-message">{customerMutationMessage}</p> : null}
                </section>

                <section className="detail-tabs" aria-label="Datos operativos por empresa">
                  <a href="#customers">Clientes</a>
                  <a href="#documents">Documentos</a>
                  <a href="#invoices">Facturas</a>
                  <a href="#einvoice">E-invoice</a>
                  <a href="#sif">SIF local</a>
                </section>

                <section className="document-panel" id="documents" aria-label="Documentos por empresa">
                  <div className="section-heading">
                    <div>
                      <p className="eyebrow">Documentos</p>
                      <h3>Archivos asociados a la empresa</h3>
                    </div>
                    <span>Subir documento no crea una factura fiscal</span>
                  </div>

                  <form className="document-upload" onSubmit={uploadDocument}>
                    <TextInput label="Titulo" onChange={(value) => setDocumentForm((current) => ({ ...current, title: value }))} value={documentForm.title} />
                    <SelectInput
                      label="Tipo"
                      onChange={(value) => setDocumentForm((current) => ({ ...current, documentType: value }))}
                      options={['INVOICE_RECEIVED', 'INVOICE_ISSUED', 'CONTRACT', 'EVIDENCE']}
                      value={documentForm.documentType}
                    />
                    <label>
                      Archivo
                      <input onChange={(event) => setDocumentFile(event.target.files?.[0] ?? null)} type="file" />
                    </label>
                    <button className="primary-button compact" disabled={isUploadingDocument} type="submit">
                      <UploadCloud size={16} />
                      Subir
                    </button>
                  </form>
                  {documentMutationMessage ? <p className="form-message">{documentMutationMessage}</p> : null}

                  <div className="document-list">
                    {companyDocuments.length === 0 ? (
                      <div className="empty-state">No hay documentos en esta empresa</div>
                    ) : companyDocuments.map((document) => (
                      <div className="document-row" key={document.id}>
                        <span>
                          <strong>{document.title}</strong>
                          <small>{document.latestFilename}</small>
                        </span>
                        <span>{formatDocumentType(document.documentType)}</span>
                        <code>{document.latestSha256.slice(0, 12)}</code>
                        <button className="ghost-button compact" onClick={() => void downloadDocument(document)} type="button">
                          <Download size={16} />
                          Descargar
                        </button>
                        <button className="ghost-button compact" onClick={() => void loadDocumentEvents(document)} type="button">
                          Eventos
                        </button>
                        {documentEvents[document.id]?.map((event) => (
                          <small className="event-note" key={event.id}>{event.eventType}: {event.details}</small>
                        ))}
                      </div>
                    ))}
                  </div>
                </section>

                <section className="invoice-panel" id="invoices" aria-label="Facturas por empresa">
                  <div className="section-heading">
                    <div>
                      <p className="eyebrow">Facturas</p>
                      <h3>Crear borrador, editar y emitir</h3>
                    </div>
                    <div className="button-row">
                      <SelectInput label="Estado" onChange={setInvoiceStatusFilter} options={['', 'DRAFT', 'ISSUED', 'RECTIFIED', 'CANCELLED_LOCAL']} value={invoiceStatusFilter} />
                      <label className="search-box inline-search">
                        <Search size={16} />
                        <input aria-label="Buscar factura" onChange={(event) => setInvoiceSearch(event.target.value)} placeholder="Numero o empresa" type="search" value={invoiceSearch} />
                      </label>
                    </div>
                  </div>

                  <div className="invoice-list">
                    {companyInvoices.length === 0 ? (
                      <div className="empty-state">No hay facturas en esta empresa</div>
                    ) : companyInvoices.map((invoice) => (
                      <div className="invoice-row" key={invoice.id}>
                        <span>
                          <strong>{invoice.fiscalNumber || invoice.invoiceNumber}</strong>
                          <small>{invoice.issuerCompany.legalName} &gt; {invoice.customerCompany.legalName}</small>
                          <small>{invoice.paymentStatus ?? 'UNPAID'} - Pendiente {formatCurrency(Number(invoice.outstandingAmount ?? invoice.total), invoice.currency)}</small>
                        </span>
                        <span className={badgeClass(invoice.status)}>{invoice.status}</span>
                        <span>{formatCurrency(Number(invoice.payableTotal ?? invoice.total), invoice.currency)}</span>
                        <button className="ghost-button compact" onClick={() => setSelectedInvoiceId(invoice.id)} type="button">
                          Ver
                        </button>
                        <button className="ghost-button compact" disabled={invoice.status !== 'DRAFT'} onClick={() => editInvoice(invoice)} type="button">
                          Editar
                        </button>
                        <button className="primary-button compact" disabled={invoice.status !== 'DRAFT'} onClick={() => void issueInvoice(invoice)} type="button">
                          Emitir
                        </button>
                        <button className="ghost-button compact" disabled={invoice.status === 'DRAFT'} onClick={() => void downloadInvoicePdf(invoice)} type="button">
                          PDF
                        </button>
                        <button className="ghost-button compact" disabled={invoice.status !== 'ISSUED'} onClick={() => void createCorrectiveInvoice(invoice)} type="button">
                          Rectificar
                        </button>
                        <button className="ghost-button compact" disabled={invoice.status !== 'ISSUED'} onClick={() => void cancelInvoiceLocal(invoice)} type="button">
                          Anular
                        </button>
                      </div>
                    ))}
                  </div>

                  {selectedInvoice ? (
                    <div className="invoice-operations" aria-label="Cobros y estado de factura">
                      <div>
                        <p className="eyebrow">Factura seleccionada</p>
                        <strong>{selectedInvoice.fiscalNumber || selectedInvoice.invoiceNumber}</strong>
                        <span>{selectedInvoice.status} - {selectedInvoice.paymentStatus ?? 'UNPAID'}</span>
                      </div>
                      <TextInput label="Pago" onChange={setPaymentAmount} type="number" value={paymentAmount} />
                      <button className="primary-button compact" disabled={selectedInvoice.status !== 'ISSUED'} onClick={() => void registerPayment(selectedInvoice)} type="button">
                        Registrar pago
                      </button>
                      <div className="payment-list">
                        {invoicePayments.map((payment) => (
                          <small key={payment.id}>{payment.paymentDate}: {formatCurrency(Number(payment.amount), selectedInvoice.currency)} {payment.method}</small>
                        ))}
                      </div>
                    </div>
                  ) : null}

                  <form className="invoice-editor" onSubmit={saveInvoice}>
                    <div className="section-heading">
                      <h3>{editingInvoiceId ? 'Editar borrador' : 'Nueva factura'}</h3>
                      <div className="button-row">
                        {editingInvoiceId ? (
                          <button
                            className="ghost-button compact"
                            onClick={() => {
                              setEditingInvoiceId(null)
                              setInvoiceForm(blankInvoiceForm(ownerCompany?.id || selectedCompany.id, selectedCompany.id))
                            }}
                            type="button"
                          >
                            Cancelar
                          </button>
                        ) : null}
                        <button className="primary-button compact" disabled={isSavingInvoice} type="submit">
                          <Save size={16} />
                          Guardar borrador
                        </button>
                      </div>
                    </div>
                    <div className="compact-form-grid">
                      <SelectInput
                        label="Emisor"
                        onChange={(value) => setInvoiceForm((current) => ({ ...current, issuerCompanyId: value }))}
                        options={companies.map((company) => company.id)}
                        renderOption={(value) => companies.find((company) => company.id === value)?.legalName ?? value}
                        value={invoiceForm.issuerCompanyId}
                      />
                      <SelectInput
                        label="Cliente"
                        onChange={(value) => setInvoiceForm((current) => ({ ...current, customerCompanyId: value }))}
                        options={companies.map((company) => company.id)}
                        renderOption={(value) => companies.find((company) => company.id === value)?.legalName ?? value}
                        value={invoiceForm.customerCompanyId || selectedCompany.id}
                      />
                      <SelectInput
                        label="Destinatario"
                        onChange={(value) => setInvoiceForm((current) => ({ ...current, customerId: value }))}
                        options={['', ...customers.map((customer) => customer.id)]}
                        renderOption={(value) => value ? customers.find((customer) => customer.id === value)?.name ?? value : 'Sin cliente dedicado'}
                        value={invoiceForm.customerId}
                      />
                      <TextInput label="Numero" onChange={(value) => setInvoiceForm((current) => ({ ...current, invoiceNumber: value }))} value={invoiceForm.invoiceNumber} />
                      <TextInput label="Fecha" onChange={(value) => setInvoiceForm((current) => ({ ...current, issueDate: value }))} type="date" value={invoiceForm.issueDate} />
                      <TextInput label="Vencimiento" onChange={(value) => setInvoiceForm((current) => ({ ...current, dueDate: value }))} type="date" value={invoiceForm.dueDate} />
                      <TextInput label="Moneda" maxLength={3} onChange={(value) => setInvoiceForm((current) => ({ ...current, currency: value.toUpperCase() }))} value={invoiceForm.currency} />
                    </div>

                    <div className="invoice-lines">
                      {invoiceForm.lines.map((line, index) => (
                        <div className="invoice-line-editor" key={`${index}-${line.description}`}>
                          <TextInput label="Descripcion" onChange={(value) => updateInvoiceLine(index, { description: value })} value={line.description} />
                          <TextInput label="Cantidad" onChange={(value) => updateInvoiceLine(index, { quantity: value })} type="number" value={line.quantity} />
                          <TextInput label="Precio" onChange={(value) => updateInvoiceLine(index, { unitPrice: value })} type="number" value={line.unitPrice} />
                          <TextInput label="IVA %" onChange={(value) => updateInvoiceLine(index, { taxRate: value })} type="number" value={line.taxRate} />
                          <TextInput label="Dto %" onChange={(value) => updateInvoiceLine(index, { discountPercent: value })} type="number" value={line.discountPercent} />
                          <TextInput label="Ret %" onChange={(value) => updateInvoiceLine(index, { withholdingPercent: value })} type="number" value={line.withholdingPercent} />
                          <button
                            className="ghost-button compact"
                            disabled={invoiceForm.lines.length === 1}
                            onClick={() => setInvoiceForm((current) => ({ ...current, lines: current.lines.filter((_, lineIndex) => lineIndex !== index) }))}
                            type="button"
                          >
                            Quitar
                          </button>
                        </div>
                      ))}
                      <button
                        className="ghost-button compact"
                        onClick={() => setInvoiceForm((current) => ({ ...current, lines: [...current.lines, { ...initialInvoiceLine }] }))}
                        type="button"
                      >
                        <Plus size={16} />
                        Linea
                      </button>
                    </div>

                    <div className="invoice-summary" aria-label="Totales calculados">
                      <div>
                        <span>Base</span>
                        <strong>{formatCurrency(invoiceTotals.base, invoiceForm.currency)}</strong>
                      </div>
                      <div>
                        <span>IVA</span>
                        <strong>{formatCurrency(invoiceTotals.tax, invoiceForm.currency)}</strong>
                      </div>
                      <div>
                        <span>Retencion</span>
                        <strong>{formatCurrency(invoiceTotals.withholding, invoiceForm.currency)}</strong>
                      </div>
                      <div>
                        <span>Total</span>
                        <strong>{formatCurrency(invoiceTotals.total, invoiceForm.currency)}</strong>
                      </div>
                    </div>
                    {invoiceMutationMessage ? <p className="form-message">{invoiceMutationMessage}</p> : null}
                  </form>
                </section>

                <section className="fiscal-output-grid" aria-label="E-invoice y SIF local">
                  <section className="einvoice-panel" id="einvoice" aria-label="Factura electronica local">
                    <div className="section-heading">
                      <div>
                        <p className="eyebrow">E-invoice</p>
                        <h3>Generacion local UBL</h3>
                      </div>
                      <button
                        className="primary-button compact"
                        disabled={!selectedInvoice || selectedInvoice.status !== 'ISSUED' || Boolean(selectedInvoiceEinvoice)}
                        onClick={() => selectedInvoice && void generateEInvoice(selectedInvoice)}
                        type="button"
                      >
                        <FileCheck2 size={16} />
                        Generar
                      </button>
                    </div>
                    <p className="form-message">Solo facturas emitidas. No se envia a ningun servicio externo.</p>
                    {selectedInvoiceEinvoice ? (
                      <div className="evidence-card">
                        <strong>{selectedInvoiceEinvoice.exchangeStatus}</strong>
                        <span>{selectedInvoiceEinvoice.syntax} - {selectedInvoiceEinvoice.commercialStatus}</span>
                        <code>{selectedInvoiceEinvoice.payloadSha256.slice(0, 24)}</code>
                      </div>
                    ) : <div className="empty-state">Sin e-invoice para la factura seleccionada</div>}
                  </section>

                  <section className="sif-panel" id="sif" aria-label="Registro SIF local">
                    <div className="section-heading">
                      <div>
                        <p className="eyebrow">SIF / Verifactu local</p>
                        <h3>Registro encadenado local</h3>
                      </div>
                      <button
                        className="primary-button compact"
                        disabled={!selectedInvoice || selectedInvoice.status !== 'ISSUED' || Boolean(selectedInvoiceSif)}
                        onClick={() => selectedInvoice && void registerSif(selectedInvoice)}
                        type="button"
                      >
                        <Fingerprint size={16} />
                        Registrar
                      </button>
                    </div>
                    <p className="form-message">Preproduccion local: no es presentacion legal ni comunicacion AEAT real.</p>
                    {selectedInvoiceSif ? (
                      <div className="evidence-card">
                        <strong>Secuencia {selectedInvoiceSif.sequenceNumber}</strong>
                        <span>{selectedInvoiceSif.recordType}</span>
                        <code>{selectedInvoiceSif.recordHash.slice(0, 24)}</code>
                      </div>
                    ) : <div className="empty-state">Sin registro SIF para la factura seleccionada</div>}
                  </section>
                </section>
                {fiscalMutationMessage ? <p className="form-message">{fiscalMutationMessage}</p> : null}

                <section className="evidence-ops-panel" aria-label="Auditoria y exportacion de evidencia">
                  <div className="section-heading">
                    <div>
                      <p className="eyebrow">Evidencia</p>
                      <h3>Auditoria y portabilidad local/preprod</h3>
                    </div>
                    <button className="primary-button compact" onClick={() => void createEvidenceExport()} type="button">
                      <Download size={16} />
                      Generar ZIP
                    </button>
                  </div>
                  <div className="evidence-grid">
                    <div className="evidence-card">
                      <strong>Eventos auditados</strong>
                      {auditEvents.length === 0 ? (
                        <span>Sin eventos todavia</span>
                      ) : auditEvents.slice(0, 5).map((event) => (
                        <small key={event.id}>{event.eventType} - {event.actorEmail} - {(event.eventHash ?? '').slice(0, 12)}</small>
                      ))}
                    </div>
                    <div className="evidence-card">
                      <strong>Exports</strong>
                      {evidenceExports.length === 0 ? (
                        <span>Sin paquetes generados</span>
                      ) : evidenceExports.slice(0, 4).map((exportJob) => (
                        <button className="ghost-button compact" key={exportJob.id} onClick={() => void downloadEvidenceExport(exportJob)} type="button">
                          {exportJob.status} - {(exportJob.sha256 ?? '').slice(0, 12)}
                        </button>
                      ))}
                    </div>
                  </div>
                  {exportMutationMessage ? <p className="form-message">{exportMutationMessage}</p> : null}
                </section>
              </>
            ) : (
              <div className="empty-state">Selecciona o crea una empresa para operar</div>
            )}
          </article>
        </section>

        <section className="saas-grid" aria-label="Plataforma SaaS">
          <article className="panel saas-panel">
            <div className="panel-heading">
              <div>
                <p className="eyebrow">Planes</p>
                <h2>Limites de producto</h2>
              </div>
              <span className="badge success">{subscriptionPlans.length} planes</span>
            </div>
            <div className="plan-list">
              {subscriptionPlans.map((plan) => (
                <div className="plan-card" key={plan.code}>
                  <div>
                    <strong>{plan.displayName}</strong>
                    <small>{plan.maxUsers} usuarios - {plan.maxDocuments} docs - {plan.maxInvoices} facturas</small>
                  </div>
                  <span>{formatCurrency(plan.monthlyPriceCents / 100, plan.currency)}</span>
                </div>
              ))}
            </div>
          </article>

          <article className="panel saas-panel">
            <div className="panel-heading">
              <div>
                <p className="eyebrow">Gobierno</p>
                <h2>Aislamiento y evidencias</h2>
              </div>
              <span className="badge warning">Local</span>
            </div>
            <ul className="audit-list">
              <li><CheckCircle2 size={18} /> Tenant activo enviado por path y X-Tenant-Id.</li>
              <li><CheckCircle2 size={18} /> Busqueda de empresas ejecutada contra backend.</li>
              <li><CheckCircle2 size={18} /> Facturas emitidas bloquean edicion libre.</li>
              <li><CheckCircle2 size={18} /> SIF/e-invoice solo nacen de factura emitida.</li>
            </ul>
            <div className="tenant-admin-list">
              {platformTenants.slice(0, 3).map((tenant) => (
                <div className="tenant-admin-row" key={tenant.id}>
                  <div>
                    <strong>{tenant.displayName}</strong>
                    <small>{tenant.slug}</small>
                  </div>
                  <span className={badgeClass(tenant.status)}>{tenant.status}</span>
                  <span>{tenant.planCode}</span>
                </div>
              ))}
              {latestSystemDeclaration ? <code>{latestSystemDeclaration.payloadSha256.slice(0, 16)}</code> : null}
            </div>
          </article>
        </section>
      </main>
    </div>
  )
}

function MetricCard({ icon: Icon, label, value, trend }: {
  icon: typeof Building2
  label: string
  value: string
  trend: string
}) {
  return (
    <article className="metric-card">
      <div className="metric-icon">
        <Icon size={20} />
      </div>
      <span>{label}</span>
      <strong>{value}</strong>
      <small>{trend}</small>
    </article>
  )
}

function TextInput({ label, maxLength, onChange, type = 'text', value }: {
  label: string
  maxLength?: number
  onChange: (value: string) => void
  type?: string
  value: string
}) {
  return (
    <label>
      {label}
      <input maxLength={maxLength} onChange={(event: ChangeEvent<HTMLInputElement>) => onChange(event.target.value)} type={type} value={value} />
    </label>
  )
}

function SelectInput({ label, onChange, options, renderOption, value }: {
  label: string
  onChange: (value: string) => void
  options: string[]
  renderOption?: (value: string) => string
  value: string
}) {
  return (
    <label>
      {label}
      <select onChange={(event) => onChange(event.target.value)} value={value}>
        {options.map((option) => (
          <option key={option || 'all'} value={option}>
            {option ? (renderOption?.(option) ?? option) : 'Todos'}
          </option>
        ))}
      </select>
    </label>
  )
}

function ChevronText({ label }: { label: string }) {
  return <span className="chevron-text">{label}</span>
}

function fiscalSettingsToForm(settings: FiscalSettings): FiscalSettingsFormState {
  return {
    legalName: settings.legalName,
    tradeName: settings.tradeName ?? '',
    nif: settings.nif,
    vatNumber: settings.vatNumber ?? '',
    addressLine1: settings.addressLine1,
    city: settings.city,
    province: settings.province ?? '',
    postalCode: settings.postalCode,
    country: settings.country,
    defaultCurrency: settings.defaultCurrency,
    defaultPaymentTermsDays: String(settings.defaultPaymentTermsDays),
    defaultVatRate: String(settings.defaultVatRate),
    pdfTemplate: settings.pdfTemplate,
    sifMode: settings.sifMode,
  }
}

function toNumber(value: string | number) {
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : 0
}

function roundMoney(value: number) {
  return Math.round((value + Number.EPSILON) * 100) / 100
}

function formatCurrency(value: number, currency = 'EUR') {
  return new Intl.NumberFormat('es-ES', { style: 'currency', currency }).format(value)
}

function formatRole(role: string) {
  return role
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1).toLowerCase())
    .join(' ')
}

function formatRelationship(value: string) {
  const map: Record<string, string> = {
    CLIENT: 'Cliente',
    SUPPLIER: 'Proveedor',
    OWNER: 'Propia',
  }
  return map[value] ?? value
}

function formatDocumentType(value: string) {
  const map: Record<string, string> = {
    INVOICE_RECEIVED: 'Factura recibida',
    INVOICE_ISSUED: 'Factura emitida',
    CONTRACT: 'Contrato',
    EVIDENCE: 'Evidencia',
  }
  return map[value] ?? value
}

function badgeClass(value: string) {
  if (value === 'ACTIVE' || value === 'ISSUED' || value === 'GENERATED' || value === 'ACCEPTED') {
    return 'badge success'
  }
  if (value === 'DRAFT' || value === 'PENDING' || value === 'TRIALING') {
    return 'badge warning'
  }
  return 'badge danger'
}

export default App
