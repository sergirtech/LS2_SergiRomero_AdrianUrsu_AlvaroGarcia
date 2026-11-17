# Frontend video catalog and playback integration

**Approved on:** 2026-11-12
**Approved by:** Requester (session approval)
**Used for:** Connecting the frontend catalog, infinite scrolling and playback to the video API

## Prompt

Follow `AGENTS.md`, `frontend/AGENTS.md`, and the frontend instruction file. Integrate the existing React video
catalog and watch page with the backend video API without adding dependencies or changing backend contracts:

* Fetch the first page from `GET /api/videos` (the backend defaults to 20 items); use the returned `nextCursor` to
  request each continuation page from `GET /api/videos?cursor=...` (the backend defaults to 10 items).
* Load later pages automatically as the user scrolls, prevent duplicate/in-flight requests, and provide clear loading,
  retry, error, and empty states.
* Load video details from `GET /api/videos/{id}` and play the MP4 from `GET /api/videos/{id}/file`. Handle unknown
  IDs and API failures without rendering a broken player.
* Build API/media URLs through `src/utils/Env.ts`, preserve current navigation and temporary comment behavior, and do
  not implement comments persistence in this slice.
* Add tests for API response handling, pagination, scrolling, playback and failure states. Update project status and
  directly related documentation. Format only files changed by this task.

## Notes

Backend API contract and database persistence are defined by
[`04-video-persistence-crud.md`](04-video-persistence-crud.md). Profile, comments, playlists, photo and theme
management remain separate implementation phases.
