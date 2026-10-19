# Design decisions

Short records of decisions the team must be able to justify. Newest last. Status: **Accepted** (implemented or
agreed), **Proposed** (planned, not implemented yet), **Open** (needs a team decision).

## D1 — Email + password login instead of Google OAuth2 (Accepted)
US-01 literally asks for "an email and password". The Google OAuth2 properties in `application.properties` come from
the course template and are unused (no OAuth2 client dependency). They can be removed or kept for a later extension.

## D2 — Server session (`JSESSIONID`) instead of JWT (Proposed, step B5)
Native to Spring Security, real logout (the server invalidates the session) and expiry for free through
`server.servlet.session.timeout` (30 min default), which covers US-02 AC3. JWT would make logout and expiry harder
to do right for no MVP benefit.

## D3 — BCrypt password hashes (Accepted)
Slow by design and salted per password; the salt is stored inside the hash, so one `password_hash` column is enough.
The `PasswordEncoder` interface is injected so tests can mock it and the algorithm can change in one place.
`spring-security-crypto` alone provides it without enabling Spring Security's web filters.

## D4 — Normalize emails (trim + lowercase, `Locale.ROOT`) (Accepted)
Otherwise `Ana@x.com` and `ana@x.com` would be two accounts, breaking US-01 AC2. The same helper
(`AuthService.normalizeEmail`) must be used at login. `Locale.ROOT` avoids locale-dependent lowercasing.

## D5 — Duplicate protection in two layers (Accepted)
`AuthService` checks `existsByEmail` / `existsByUsername` to return a clear message; the `UNIQUE` constraints on
`app_user` stop the race where two simultaneous registrations both pass the check.

## D6 — Table name `app_user` (Accepted)
`user` is a reserved word in H2 and PostgreSQL.

## D7 — `username` as the required profile field (Accepted, confirm with the team)
US-01 mentions "required profile fields" without listing them. Username is unique, 3–30 characters.

## D8 — Password policy 8–72 characters (Proposed, step B4)
8 is the usual minimum length; 72 because BCrypt ignores bytes beyond 72.

## D9 — DTO records instead of exposing entities (Proposed, step B4)
Input DTOs prevent mass assignment (`id`, `passwordHash`); `UserResponse` has no hash field, so it can't leak.

## D10 — Validation on both sides (Proposed, B4 + F3)
Client-side for immediate feedback; Bean Validation on the server is the source of truth (US-01 AC3).

## D11 — HTTP errors as JSON from one `@RestControllerAdvice` (Proposed, B4/B5)
400 invalid fields `{message, errors}`, 409 duplicate `{message}`, 401 bad credentials with a generic "Incorrect email
or password" so login doesn't reveal which emails exist (registration does, because US-01 AC2 requires it).

## D12 — 401 instead of redirect for unauthenticated API calls (Proposed, step B5)
Spring Security's default redirects to an HTML login form the SPA doesn't have.

## D13 — CSRF (Open, step B5)
With cookie sessions CSRF is a real risk. Options: disable it for the MVP and document the risk, or keep it with
`CookieCsrfTokenRepository` and send `X-XSRF-TOKEN` from the frontend.

## D14 — CORS with credentials for dev (Proposed, step B7)
The session cookie only travels from `:5173` to `:8080` with `allowCredentials(true)` and an explicit origin pattern
(`http://localhost:[*]`), not `*`. In prod the SPA is served by the backend (same origin).

## D15 — Add `spring-boot-starter-security` together with the filter chain (Accepted)
Adding it alone protects every route with a generated password and breaks `GET /api/videos`; each commit must leave
the app working.

## D16 — `react-router` for `/login` and `/register` (Proposed, step F1)
Real URLs and back-button support; also needed later for the video detail page (US-05).

## D17 — `fetch` in `useAuth` (Proposed, step F2)
Frontend rules require mocking HTTP with `jest-fetch-mock`, and `credentials: 'include'` is explicit.
