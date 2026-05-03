import { expect, test } from '@playwright/test'

test('loads the fiscal operations dashboard', async ({ page }) => {
  await page.goto('/')

  await expect(page).toHaveTitle(/Fiscal SaaS/)
  await expect(page.getByRole('heading', { name: 'Identidad y tenants' })).toBeVisible()
  await expect(page.getByRole('table', { name: 'Empresas del tenant' })).toBeVisible()
  await expect(page.getByRole('heading', { name: 'Registrar cliente o proveedor' })).toBeVisible()
  await expect(page.getByRole('region', { name: 'Relaciones B2B' })).toBeVisible()
  await expect(page.getByRole('region', { name: 'Facturacion fiscal' })).toBeVisible()
  await expect(page.getByRole('region', { name: 'Factura electronica B2B' })).toBeVisible()
  await expect(page.getByRole('region', { name: 'Verifactu SIF' })).toBeVisible()
  await expect(page.getByRole('region', { name: 'Centro documental' })).toBeVisible()
})
