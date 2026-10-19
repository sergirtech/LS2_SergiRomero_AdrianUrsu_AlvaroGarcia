# Frontend UI template (layout and theme)

**Approved on:** _pending team approval_
**Approved by:** _pending — approve in the PR of `feature/ui-template`_
**Used for:** creating or restyling the layout of the React frontend (navbar, sidebar, video grid, theme).

## Prompt

```
Give the ProTube frontend (frontend/) a good-looking, YouTube-style template.

Follow AGENTS.md and frontend/AGENTS.md. Constraints:
- Use the existing stack only: React 19 + TypeScript, Bootstrap 5 (dark mode via data-bs-theme="dark"), plain CSS in
  src/App.css. No new dependencies; icons as inline SVG.
- Layout: sticky top navbar (menu toggle, logo from public/, search box, "Sign in" button), collapsible sidebar
  (icon rail on small screens) and a responsive video card grid with 16:9 thumbnails.
- Keep data loading in the existing useAllVideos hook; components receive data as props. The search box filters
  the loaded videos client-side.
- Thumbnails come from `${getEnv().MEDIA_BASE_URL}/<video>.webp` with a placeholder when the image fails to load.
- Show a skeleton grid while loading and clear messages for errors and empty results.
- Define colours as CSS variables on :root and prefix custom classes with "pt-".
- Add Testing Library tests for every new or changed component (Env.ts is mocked in jest.setup.ts; no network).
- Run npm run build, npm run test and npm run lint from frontend/ and report the results. Don't reformat files you
  didn't change.
```

## Notes

* Lint fails on CRLF line endings in config files untouched by this prompt (see "Known issues" in
  `frontend/AGENTS.md`).
* The backend `/api/videos` still returns placeholder names, so cards show the gradient placeholder until real video
  names are returned.
* Sidebar links and "Sign in" are visual only until navigation and auth (OAuth) are implemented.
