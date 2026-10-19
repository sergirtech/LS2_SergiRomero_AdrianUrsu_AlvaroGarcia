---
applyTo: "frontend/src/**/*.{ts,tsx,css}"
---

# Frontend rules

* Build API and media URLs with `getEnv()` from `src/utils/Env.ts`; never hardcode `localhost`.
* Components are presentational: they receive data as props. Fetching lives in hooks (`src/use*.ts`) used by `App.tsx`.
* Styling: Bootstrap 5 dark mode + `src/App.css`. Use the `--pt-*` CSS variables for colours and the `pt-` prefix for
  custom classes. Import CSS only from `App.tsx` / `main.tsx` (Jest can't load `.css` files).
* Every new component gets a test in `src/components/__tests__/<Component>.test.tsx` using Testing Library
  (`render`, `screen`, `fireEvent`). Query by role or label, not by CSS class, when possible.
* No network in tests: mock HTTP with `jest-fetch-mock`. `Env.ts` is already mocked globally in `jest.setup.ts`.
* Jest requires ≥75% global coverage; `npm run build`, `npm run test` and `npm run lint` must pass.
