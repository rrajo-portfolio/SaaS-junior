import { expect, test } from '@playwright/test'

test('loads tenant data from the preproduction backend', async ({ page }) => {
  await page.goto('/')

  await expect(page.getByRole('heading', { name: 'SaaS fiscal operativo' })).toBeVisible()
  await expect(page.getByRole('region', { name: 'Estado de servicios' })).toBeVisible()
  await expect(page.getByRole('region', { name: 'Contexto de tenant' })).toBeVisible()
  await expect(page.getByRole('region', { name: 'Resumen SaaS' })).toBeVisible()
  await expect(page.getByRole('list', { name: 'Empresas del tenant' }).getByText('Cobalto Industrial SA')).toBeVisible()
  await expect(page.getByRole('region', { name: 'Configuracion fiscal de empresa' })).toBeVisible()
  await expect(page.getByRole('region', { name: 'Clientes de facturacion' })).toBeVisible()
  await page.getByRole('tab', { name: /Norte Asesores/ }).click()
  await expect(page.getByRole('list', { name: 'Empresas del tenant' }).getByText('Norte Asesores SL')).toBeVisible()
  await expect(page.getByRole('region', { name: 'Facturas por empresa' })).toBeVisible()
  await expect(page.getByRole('region', { name: 'Factura electronica local' })).toBeVisible()
  await expect(page.getByRole('region', { name: 'Registro SIF local' })).toBeVisible()
  await expect(page.getByRole('region', { name: 'Documentos por empresa' })).toBeVisible()
  await expect(page.getByRole('region', { name: 'Auditoria y exportacion de evidencia' })).toBeVisible()
  await expect(page.getByText('Platform Admin').first()).toBeVisible()
})
