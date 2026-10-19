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

* `src/App.tsx` composes the layout (`Navbar` + `Sidebar` + main content) and owns UI state (search query, sidebar
  open). Data hooks like `src/useAllVideos.ts` call the API; components receive data as props and don't fetch.
* `src/components/` — presentational components:
  * `Navbar` — logo, search box (filters the loaded videos client-side), "Sign in" button.
  * `Sidebar` — navigation links; collapses to an icon rail.
  * `VideoGrid` / `VideoGridSkeleton` — responsive card grid and its loading placeholder.
  * `VideoCard` — thumbnail from `${MEDIA_BASE_URL}/<name>.webp`, falling back to a gradient placeholder on error.
* Tests in `src/components/__tests__/*.test.tsx`.
* `src/utils/Env.ts` — the only place that reads `VITE_API_DOMAIN` / `VITE_MEDIA_DOMAIN`. In tests it is mocked
  globally in `jest.setup.ts` (`API_BASE_URL: 'other'`, `MEDIA_BASE_URL: 'development'`); that mock wins over
  `src/utils/__mocks__/Env.ts`.
* `.env.development` / `.env.production` hold non-secret Vite config only.

## Styling

* Bootstrap 5 in dark mode (`data-bs-theme="dark"` on `<html>` in `index.html`) plus custom styles in `src/App.css`.
* Colours and sizes are CSS variables on `:root` at the top of `App.css` (`--pt-accent`, `--pt-bg`, ...). Change the
  theme there; don't hardcode colours in components.
* Custom classes use the `pt-` prefix (e.g. `pt-card`, `pt-sidebar`) so they never clash with Bootstrap.
* Icons are inline SVGs with `stroke="currentColor"`; don't add an icon library for a few icons.
* Import CSS only from `App.tsx` / `main.tsx`. Jest has no CSS module mapper, so a component that imports a `.css`
  file can't be tested.

## Rules

* Build API and media URLs from `Env.ts`; never hardcode `localhost` URLs in components.
* Mock HTTP in tests with `jest-fetch-mock` (`fetchMock.resetMocks()` in `beforeEach`); tests must not hit the network.
* If a snapshot changes, check the diff is intended before updating it with `npx jest -u`.
* Jest enforces ≥75% global coverage (branches, functions, lines, statements) over the tested files: every new
  component needs its own test file.
* Run Prettier only on the files you change (`npx prettier --write <files>`), not on `src/` or the whole project.

## Known issues

* `npm run lint` currently fails on `Delete ␍` errors in config files (`vite.config.ts`, `jest.setup.ts`,
  `eslint.config.js`, ...) because `core.autocrlf=true` checks them out with CRLF. This is not caused by your change;
  fix it in a dedicated PR (e.g. `"endOfLine": "auto"` in `.prettierrc` or a `.gitattributes` with `eol=lf`), not
  as part of a feature.
