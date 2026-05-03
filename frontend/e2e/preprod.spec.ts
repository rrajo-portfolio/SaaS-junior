import { expect, test } from '@playwright/test'

test('loads tenant data from the preproduction backend', async ({ page }) => {
  await page.goto('/')

  await expect(page.getByRole('heading', { name: 'Identidad y tenants' })).toBeVisible()
  await expect(page.getByText('Cobalto Industrial SA')).toBeVisible()
  await page.getByRole('tab', { name: /Norte Asesores/ }).click()
  await expect(page.getByText('Norte Asesores SL')).toBeVisible()
  await expect(page.getByText('Platform Admin').first()).toBeVisible()
})
