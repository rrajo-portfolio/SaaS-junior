import { expect, test } from '@playwright/test'

test('loads tenant data from the preproduction backend', async ({ page }) => {
  await page.goto('/')

  await expect(page.getByRole('heading', { name: 'Identidad y tenants' })).toBeVisible()
  await expect(page.getByRole('table', { name: 'Empresas del tenant' }).getByText('Cobalto Industrial SA')).toBeVisible()
  await expect(page.getByText('Portal proveedor')).toBeVisible()
  await page.getByRole('tab', { name: /Norte Asesores/ }).click()
  await expect(page.getByRole('table', { name: 'Empresas del tenant' }).getByText('Norte Asesores SL')).toBeVisible()
  await expect(page.getByText('Gestion cliente')).toBeVisible()
  await expect(page.getByRole('region', { name: 'Facturacion fiscal' })).toBeVisible()
  await expect(page.getByRole('region', { name: 'Verifactu SIF' })).toBeVisible()
  await expect(page.getByRole('region', { name: 'Centro documental' })).toBeVisible()
  await expect(page.getByText('Platform Admin').first()).toBeVisible()
})
