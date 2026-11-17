# Profile video upload and management UI

**Approved on:** 2026-11-12
**Approved by:** Requester (session approval)
**Used for:** Adding authenticated MP4 upload and owned-video management to the profile

## Prompt

Follow `AGENTS.md`, `frontend/AGENTS.md`, and the frontend instruction file. Implement the approved frontend profile
video-management slice using the existing backend API without changing its contract:

* Add an authenticated profile section for uploading `.mp4` files, listing the current user's uploaded videos, and
  editing or deleting their video metadata/content through the existing API.
* Use the paginated `GET /api/videos/mine` endpoint. Keep ownership enforcement on the backend, send session
  credentials, preserve the browser-generated multipart boundary, and refresh both owned videos and the public
  catalog after successful mutations.
* Reset user-specific state on logout/account changes. Validate file extension/size and metadata client-side while
  treating server validation as authoritative. Show actionable loading, empty, retry, success, and error states.
* Leave comments, playlists, profile photos, and theme persistence for separate approved slices.
* Add component, hook, and route integration tests. Update project status and the approved prompt log. Format only
  changed files.
