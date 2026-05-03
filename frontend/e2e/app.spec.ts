import { expect, test } from '@playwright/test'

test('loads the fiscal operations dashboard', async ({ page }) => {
  await page.goto('/')

  await expect(page).toHaveTitle(/Fiscal SaaS/)
  await expect(page.getByRole('heading', { name: 'Operaciones documentales' })).toBeVisible()
  await expect(page.getByRole('table', { name: 'Documentos recientes' })).toBeVisible()
})
