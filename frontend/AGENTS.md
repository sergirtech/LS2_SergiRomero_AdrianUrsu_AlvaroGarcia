# frontend/AGENTS.md

Rules for `frontend/` only. General rules are in the root [`AGENTS.md`](../AGENTS.md).

## Stack

React 19, TypeScript 5.9, Vite 7, Bootstrap 5, axios. Tests with Jest 30 + Testing Library (jsdom) and
`jest-fetch-mock`. ESLint 9 (flat config, `eslint.config.js`) and Prettier 3 (`.prettierrc`). CI uses Node 22.

## Commands (run from `frontend/`)

* Install: `npm ci` (use `npm install` only when changing dependencies, and commit `package-lock.json`).
* Dev server: `npm run dev` (port 5173).
* Build: `npm run build` (`tsc -b && vite build`; type errors fail the build).
* Tests (coverage on): `npm run test`; single file: `npx jest src/components/__tests__/VideoGrid.test.tsx`.
* Lint: `npm run lint` / `npm run lint-fix`. Format: `npm run format`.

**Before finishing any frontend change, run `npm run build`, `npm run test` and `npm run lint` and make sure they pass.**

## Structure

* `src/App.tsx` composes the UI; data hooks like `src/useAllVideos.ts` call the API.
* `src/components/` — components; tests in `src/components/__tests__/*.test.tsx`.
* `src/utils/Env.ts` — the only place that reads `VITE_API_DOMAIN` / `VITE_MEDIA_DOMAIN`; it has a manual mock in
  `src/utils/__mocks__/Env.ts` for tests.
* `.env.development` / `.env.production` hold non-secret Vite config only.

## Rules

* Build API and media URLs from `Env.ts`; never hardcode `localhost` URLs in components.
* Mock HTTP in tests with `jest-fetch-mock` (`fetchMock.resetMocks()` in `beforeEach`); tests must not hit the network.
* If a snapshot changes, check the diff is intended before updating it with `npx jest -u`.
