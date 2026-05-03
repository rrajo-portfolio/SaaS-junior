import { useEffect, useMemo, useState } from 'react'
import {
  Activity,
  AlertTriangle,
  Bell,
  Building2,
  CheckCircle2,
  ChevronRight,
  FileCheck2,
  FileClock,
  Files,
  Gauge,
  Landmark,
  LockKeyhole,
  Network,
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

type HealthState =
  | { label: 'Comprobando'; tone: 'warning' }
  | { label: 'Operativo'; tone: 'success'; payload: ApiHealth }
  | { label: 'Sin conexion'; tone: 'danger' }

const apiRoot = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api').replace(/\/$/, '')
const demoUserEmail = 'ana.admin@fiscalsaas.local'

const fiscalStatus = [
  { label: 'Tenant guard', value: 'Activo', icon: Network, tone: 'success' },
  { label: 'AEAT adapter', value: 'Stub seguro', icon: Landmark, tone: 'warning' },
  { label: 'B2B e-invoice', value: 'Modelo pendiente', icon: FileCheck2, tone: 'neutral' },
]

const auditTrail = [
  'Migracion V2 de identidad aplicada',
  'Memberships activos resueltos por usuario',
  'Cabecera X-Tenant-Id validada por endpoint',
  'Acceso cruzado entre tenants bloqueado',
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
  const [identityError, setIdentityError] = useState<string | null>(null)

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
    fetch(apiUrl(`/tenants/${activeTenantId}/companies`), {
      headers: authHeaders(activeTenantId),
      signal: controller.signal,
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error('Company scope failed')
        }
        return response.json() as Promise<CompanySummary[]>
      })
      .then(setCompanies)
      .catch((error: unknown) => {
        if (error instanceof DOMException && error.name === 'AbortError') {
          return
        }
        setIdentityError('Tenant no accesible')
      })

    return () => controller.abort()
  }, [activeTenantId])

  const activeTenant = useMemo(
    () => tenants.find((tenant) => tenant.id === activeTenantId) ?? tenants[0],
    [activeTenantId, tenants],
  )

  const metricCards = useMemo(
    () => [
      { label: 'Tenants asignados', value: tenants.length.toString(), trend: me?.user.displayName ?? demoUserEmail, icon: Users },
      { label: 'Empresas visibles', value: companies.length.toString(), trend: activeTenant?.name ?? 'Sin tenant activo', icon: Building2 },
      { label: 'Roles activos', value: new Set(me?.user.roles ?? []).size.toString(), trend: formatRole(activeTenant?.role), icon: ShieldCheck },
      { label: 'Eventos auditados', value: '7.418', trend: 'append-only', icon: FileClock },
    ],
    [activeTenant, companies.length, me, tenants.length],
  )

  const healthDetail = useMemo(() => {
    if (health.tone !== 'success') {
      return apiRoot
    }
    return health.payload.service
  }, [health])

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

function formatStatus(value: string) {
  return value === 'ACTIVE' ? 'Activo' : value
}

export default App
