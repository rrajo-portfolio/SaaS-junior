# E2E Integration

Phase 10 extends preproduction validation from page-load checks to critical fiscal workflows.

## Coverage

- Playwright local smoke keeps the UI shell testable without a backend.
- Playwright preproduction smoke verifies the deployed frontend reads tenant data from the backend.
- Playwright preproduction critical flow creates a fiscal invoice through the API, issues it, creates a UBL e-invoice message, creates a Verifactu SIF record, creates a system declaration draft and verifies the frontend renders the generated records.
- Backend integration tests keep Flyway, JPA and MySQL compatibility covered through Testcontainers.

## Commands

- Local UI shell: `npm run e2e`
- Preproduction UI and critical flows: `npm run e2e:preprod`
- Kubernetes HTTP smoke: `./scripts/k8s-smoke-preprod.ps1`

No screenshots, traces or Playwright result artifacts are committed.
