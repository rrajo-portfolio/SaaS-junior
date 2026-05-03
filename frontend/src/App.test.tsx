import { render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import App from './App'

describe('App', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('renders the fiscal identity shell', () => {
    vi.stubGlobal('fetch', vi.fn(() => Promise.reject(new Error('offline'))))

    render(<App />)

    expect(screen.getByRole('heading', { name: /identidad y tenants/i })).toBeInTheDocument()
    expect(screen.getByRole('table', { name: 'Empresas del tenant' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: /registrar cliente o proveedor/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /subir documento/i })).toBeInTheDocument()
  })

  it('shows backend health and tenant data when the API responds', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn((input: RequestInfo | URL) => {
        const url = input.toString()
        const payload = url.endsWith('/health')
          ? {
              status: 'ok',
              service: 'fiscal-saas-backend',
              checkedAt: '2026-05-03T15:00:00Z',
            }
          : url.endsWith('/me')
            ? {
                user: {
                  id: '20000000-0000-0000-0000-000000000001',
                  email: 'ana.admin@fiscalsaas.local',
                  displayName: 'Ana Admin',
                  roles: ['platform_admin'],
                },
                memberships: [],
              }
            : url.endsWith('/tenants')
              ? [
                  {
                    id: '10000000-0000-0000-0000-000000000001',
                    slug: 'norte-asesores',
                    name: 'Norte Asesores',
                    role: 'platform_admin',
                  },
                ]
              : url.endsWith('/business-relationships')
                ? [
                    {
                      id: '50000000-0000-0000-0000-000000000001',
                      tenantId: '10000000-0000-0000-0000-000000000001',
                      sourceCompany: {
                        id: '40000000-0000-0000-0000-000000000001',
                        tenantId: '10000000-0000-0000-0000-000000000001',
                        legalName: 'Norte Asesores SL',
                        taxId: 'B12345678',
                        countryCode: 'ES',
                        relationshipType: 'OWNER',
                        status: 'ACTIVE',
                      },
                      targetCompany: {
                        id: '40000000-0000-0000-0000-000000000002',
                        tenantId: '10000000-0000-0000-0000-000000000001',
                        legalName: 'Alba Retail Group SL',
                        taxId: 'B87654321',
                        countryCode: 'ES',
                        relationshipType: 'CLIENT',
                        status: 'ACTIVE',
                      },
                      relationshipKind: 'CLIENT_MANAGEMENT',
                      status: 'ACTIVE',
                      notes: 'Gestion fiscal y documental recurrente',
                      startsAt: '2026-01-01',
                    },
                  ]
                : [
                  {
                    id: '40000000-0000-0000-0000-000000000001',
                    tenantId: '10000000-0000-0000-0000-000000000001',
                    legalName: 'Norte Asesores SL',
                    taxId: 'B12345678',
                    countryCode: 'ES',
                    relationshipType: 'OWNER',
                    status: 'ACTIVE',
                  },
                ]

        return Promise.resolve({
          ok: true,
          json: () => Promise.resolve(payload),
        })
      }),
    )

    render(<App />)

    await waitFor(() => expect(screen.getByText('Operativo')).toBeInTheDocument())
    expect(screen.getByText('fiscal-saas-backend')).toBeInTheDocument()
    await waitFor(() => expect(screen.getAllByText('Norte Asesores SL').length).toBeGreaterThan(0))
    expect(screen.getByText('Gestion cliente')).toBeInTheDocument()
  })
})
