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
  ReceiptText,
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

type HealthState =
  | { label: 'Comprobando'; tone: 'warning' }
  | { label: 'Operativo'; tone: 'success'; payload: ApiHealth }
  | { label: 'Sin conexion'; tone: 'danger' }

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

const metricCards = [
  { label: 'Documentos pendientes', value: '128', trend: '+18 hoy', icon: FileClock },
  { label: 'Facturas validadas', value: '842', trend: '99,2% sin incidencias', icon: ReceiptText },
  { label: 'Empresas activas', value: '36', trend: '12 con intercambio B2B', icon: Building2 },
  { label: 'Eventos auditados', value: '7.418', trend: 'append-only', icon: ShieldCheck },
]

const documentQueue = [
  {
    id: 'DOC-2026-0418',
    company: 'Norte Asesores SL',
    kind: 'Factura recibida',
    status: 'Pendiente OCR',
    risk: 'Medio',
    updatedAt: '17:02',
  },
  {
    id: 'DOC-2026-0417',
    company: 'Cobalto Industrial SA',
    kind: 'Certificado fiscal',
    status: 'Validado',
    risk: 'Bajo',
    updatedAt: '16:47',
  },
  {
    id: 'DOC-2026-0416',
    company: 'Alba Retail Group',
    kind: 'Contrato B2B',
    status: 'Revision legal',
    risk: 'Alto',
    updatedAt: '16:31',
  },
  {
    id: 'DOC-2026-0415',
    company: 'Delta Servicios Profesionales',
    kind: 'Justificante',
    status: 'Archivado',
    risk: 'Bajo',
    updatedAt: '15:58',
  },
]

const fiscalStatus = [
  { label: 'SIF hash chain', value: 'Preparado', icon: Network, tone: 'success' },
  { label: 'AEAT adapter', value: 'Stub seguro', icon: Landmark, tone: 'warning' },
  { label: 'B2B e-invoice', value: 'Modelo pendiente', icon: FileCheck2, tone: 'neutral' },
]

const auditTrail = [
  'Validacion de migracion V1 completada',
  'Health endpoint publicado sin autenticacion',
  'Politica CORS restringida a origenes locales',
  'Credenciales reales excluidas del repositorio',
]

function App() {
  const [health, setHealth] = useState<HealthState>({ label: 'Comprobando', tone: 'warning' })

  useEffect(() => {
    const controller = new AbortController()

    fetch(`${apiBaseUrl}/api/health`, { signal: controller.signal })
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

    return () => controller.abort()
  }, [])

  const healthDetail = useMemo(() => {
    if (health.tone !== 'success') {
      return apiBaseUrl
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
          <a href="#documents">
            <Files size={18} />
            <span>Documentos</span>
          </a>
          <a href="#invoices">
            <ReceiptText size={18} />
            <span>Facturas</span>
          </a>
          <a href="#companies">
            <Users size={18} />
            <span>Empresas</span>
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
            <h1>Operaciones documentales</h1>
          </div>
          <div className="topbar-actions">
            <label className="search-box">
              <Search size={18} />
              <input aria-label="Buscar" placeholder="Buscar empresa, factura o documento" />
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
          <article className="panel document-panel" id="documents">
            <div className="panel-heading">
              <div>
                <p className="eyebrow">Cola documental</p>
                <h2>Entradas recientes</h2>
              </div>
              <button className="ghost-button" type="button">
                Ver todas
                <ChevronRight size={16} />
              </button>
            </div>

            <div className="data-table" role="table" aria-label="Documentos recientes">
              <div className="table-row table-head" role="row">
                <span role="columnheader">Documento</span>
                <span role="columnheader">Empresa</span>
                <span role="columnheader">Estado</span>
                <span role="columnheader">Riesgo</span>
                <span role="columnheader">Hora</span>
              </div>
              {documentQueue.map((document) => (
                <div className="table-row" role="row" key={document.id}>
                  <span role="cell">
                    <strong>{document.id}</strong>
                    <small>{document.kind}</small>
                  </span>
                  <span role="cell">{document.company}</span>
                  <span role="cell">
                    <StatusBadge label={document.status} />
                  </span>
                  <span role="cell">
                    <RiskBadge label={document.risk} />
                  </span>
                  <span role="cell">{document.updatedAt}</span>
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
  const tone = normalized.includes('validado') || normalized.includes('archivado') ? 'success' : 'warning'

  return <span className={`badge ${tone}`}>{label}</span>
}

function RiskBadge({ label }: { label: string }) {
  const tone = label === 'Alto' ? 'danger' : label === 'Medio' ? 'warning' : 'success'

  return <span className={`badge ${tone}`}>{label}</span>
}

export default App
