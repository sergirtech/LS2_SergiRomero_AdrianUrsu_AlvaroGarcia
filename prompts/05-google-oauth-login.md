# Google OAuth alongside password login

**Approved on:** 2026-11-12
**Approved by:** Requester (session approval)
**Used for:** Adding Google OAuth2 login without removing existing email/password auth

## Prompt

Add Google OAuth2 sign-in to ProTube alongside the existing email/password registration and login. Keep the existing
`JSESSIONID` session and ensure `Authentication.getName()` continues to be the local normalized email, so existing
authenticated API ownership checks work with either login method. Require Google's verified email, use the stable
Google subject to find/link the identity, link an existing account only by matching verified email, and create a
local account on first Google sign-in. Generate new usernames from the Google display name as a URL-safe slug and
append a numeric suffix on collisions. Google-only accounts have no local password; local password authentication
must reject them with the same generic unauthorized response as other bad credentials.

Read client ID and secret from the existing environment variables; never commit credentials. Redirect OAuth success
to the configured frontend URL. Add unit and integration coverage using mocks or fake OAuth config only, with no
network requests to Google, and update the auth documentation and project status.

## Notes

The project has an existing Google OAuth environment-property block but did not previously have OAuth2 client
support enabled.
