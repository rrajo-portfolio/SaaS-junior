import { render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import App from './App'

describe('App', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('renders the fiscal dashboard shell', () => {
    vi.stubGlobal('fetch', vi.fn(() => Promise.reject(new Error('offline'))))

    render(<App />)

    expect(screen.getByRole('heading', { name: /operaciones documentales/i })).toBeInTheDocument()
    expect(screen.getByText('Cola documental')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /subir documento/i })).toBeInTheDocument()
  })

  it('shows backend health when the API responds', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(() =>
        Promise.resolve({
          ok: true,
          json: () =>
            Promise.resolve({
              status: 'ok',
              service: 'fiscal-saas-backend',
              checkedAt: '2026-05-03T15:00:00Z',
            }),
        }),
      ),
    )

    render(<App />)

    await waitFor(() => expect(screen.getByText('Operativo')).toBeInTheDocument())
    expect(screen.getByText('fiscal-saas-backend')).toBeInTheDocument()
  })
})
