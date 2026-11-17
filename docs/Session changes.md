# Session changes report

**Project:** ProTube
**Session date:** 16 November 2026
**Scope:** Video catalog and management, optional Google OAuth startup, comments, theme preference, playlists, profile
photos, project documentation, tests, and this report.

## Summary

This session completed the requested video/profile work and the remaining playlist and profile-photo CRUD slice. The
backend remains organized around hexagonal architecture: web controllers depend on input ports, application services
use output ports, and persistence adapters map to JPA entities. Persistent entities are compatible with PostgreSQL
storage.

## Changes by feature

### Video storage, catalog and profile management

- Added video metadata and binary-content persistence with separate JPA tables.
- Added authenticated upload, metadata edit, owner-only deletion, and current-user video listing.
- Added public catalog paging, video detail, and playback endpoints.
- The catalog returns 20 videos initially and 10 per continuation. The frontend fetches more while scrolling.
- Added frontend API mapping, loading/error/not-found states, video playback, and profile upload/edit/delete controls.
- Video deletion also removes dependent comments and playlist references.

### Authentication startup

- Made Google OAuth client registration conditional on both OAuth credentials being configured. Email/password
  authentication can run without Google credentials; credentials remain environment-only.

### Comments and theme preference

- Added persistent comments and per-user likes, including replies, comment author edit/delete ownership, and profile
  comment management.
- Added persistent light/dark theme selection through `localStorage` for signed-in and anonymous visitors.

### Playlists

- Added persistent, user-owned playlists with create, rename, delete, and video add/remove operations.
- Added backend owner checks and validation; deleting a video clears playlist membership.
- Replaced frontend local-storage playlist persistence with authenticated API calls and async UI callbacks.

### Profile photos

- Added authenticated photo retrieval, upload/replacement, and deletion backed by a dedicated database table.
- Added frontend profile photo selection/removal UI and default-avatar fallback.
- Enforced a 5 MB upload limit and supported PNG/JPG/JPEG/SVG formats. The backend checks raster signatures and
  extension/content-type agreement, parses SVG with external XML entities disabled, and removes active or unsafe SVG
  elements/attributes before persistence.

### Documentation and follow-up tracking

- Updated `docs/architecture.md` and `docs/project-status.md`.
- Added prompt records for the implemented feature slices.
- Recorded remaining MVP gaps in project status: full-catalog ranked search (US-08), analytics (US-11), E2E tests
  (NFR-02), and full-stack Compose deployment (NFR-03). The user asked to be reminded that full-catalog search is
  the first follow-up; no search implementation was started in this session.
- Added `docs/implementation-log.md`, which contains the complete categorized file-by-file inventory for the
  implementation work.

## Files created

### Backend application, domain and web

- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/in/DeleteVideoUseCase.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/in/EditVideoUseCase.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/in/GetVideoFileUseCase.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/in/GetVideoUseCase.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/in/ListMyVideosUseCase.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/in/ManageCommentsUseCase.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/in/ManagePlaylistsUseCase.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/in/ManageProfilePhotoUseCase.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/in/ProvisionGoogleUserUseCase.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/in/UploadVideoUseCase.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/out/CommentRepository.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/out/PlaylistRepository.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/out/ProfilePhotoRepository.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/out/VideoRepository.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/service/CommentService.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/service/GoogleAccountService.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/service/PlaylistService.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/service/ProfilePhotoService.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/exception/CommentNotFoundException.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/exception/InvalidCommentException.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/exception/InvalidPlaylistException.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/exception/InvalidProfilePhotoException.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/exception/InvalidVideoException.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/exception/PlaylistNotFoundException.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/exception/VideoNotFoundException.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/model/Playlist.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/model/ProfilePhoto.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/model/Video.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/model/VideoComment.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/model/VideoFile.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/model/VideoPage.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/model/VideoSummary.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/config/GoogleOAuth2ClientConfiguration.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/CommentJpaEntity.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/CommentJpaRepository.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/CommentLikeJpaEntity.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/CommentLikeJpaRepository.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/CommentPersistenceAdapter.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/PlaylistJpaEntity.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/PlaylistJpaRepository.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/PlaylistPersistenceAdapter.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/ProfilePhotoPersistenceAdapter.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/UserProfilePhotoJpaEntity.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/UserProfilePhotoJpaRepository.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/VideoContentJpaEntity.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/VideoContentJpaRepository.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/VideoJpaEntity.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/VideoJpaRepository.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/VideoPersistenceAdapter.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/security/GoogleOAuth2UserService.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/web/CommentsController.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/web/PlaylistsController.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/web/ProfilePhotoController.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/web/dto/CommentRequest.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/web/dto/CommentResponse.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/web/dto/PlaylistNameRequest.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/web/dto/PlaylistResponse.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/web/dto/VideoPageResponse.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/web/dto/VideoResponse.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/web/dto/VideoUpdateRequest.java`

### Backend tests and test configuration

- `backend/src/test/java/com/tecnocampus/LS2/protube_back/application/service/CommentServiceTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/application/service/GoogleAccountServiceTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/application/service/PlaylistServiceTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/application/service/ProfilePhotoServiceTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/CommentPersistenceAdapterTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/PlaylistPersistenceAdapterTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/ProfilePhotoPersistenceAdapterTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/VideoPersistenceAdapterTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/security/GoogleOAuth2UserServiceTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/web/CommentsControllerTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/web/PlaylistsControllerTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/web/ProfilePhotoControllerTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/web/VideosApiIntegrationTest.java`
- `backend/src/test/resources/application.properties`

### Frontend

- `frontend/src/__tests__/useAllVideos.test.tsx`
- `frontend/src/__tests__/useMyComments.test.tsx`
- `frontend/src/__tests__/useMyVideos.test.tsx`
- `frontend/src/__tests__/useProfilePhoto.test.tsx`
- `frontend/src/__tests__/videoApi.test.ts`
- `frontend/src/components/ProfileCommentsManager.tsx`
- `frontend/src/components/ProfilePhotoManager.tsx`
- `frontend/src/components/ProfileVideoManager.tsx`
- `frontend/src/components/__tests__/ProfileCommentsManager.test.tsx`
- `frontend/src/components/__tests__/ProfilePhotoManager.test.tsx`
- `frontend/src/components/__tests__/ProfileVideoManager.test.tsx`
- `frontend/src/useMyComments.ts`
- `frontend/src/useMyVideos.ts`
- `frontend/src/useProfilePhoto.ts`
- `frontend/src/videoApi.ts`

### Prompts and documentation

- `prompts/04-video-persistence-crud.md`
- `prompts/05-google-oauth-login.md`
- `prompts/06-frontend-video-catalog-playback.md`
- `prompts/07-profile-video-management.md`
- `prompts/08-persistent-comments-and-theme.md`
- `prompts/09-playlists-and-profile-photos.md`
- `docs/implementation-log.md`
- `docs/Session changes.md` (this report)

## Existing files modified

### Backend application and configuration

- `backend/pom.xml`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/in/ListVideosUseCase.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/port/out/UserRepository.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/application/service/VideoService.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/domain/model/User.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/config/SecurityConfig.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/UserJpaEntity.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/UserJpaRepository.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/UserPersistenceAdapter.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/security/AppUserDetailsService.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/web/ApiExceptionHandler.java`
- `backend/src/main/java/com/tecnocampus/LS2/protube_back/infrastructure/web/VideosController.java`
- `backend/src/main/resources/application.properties`

### Backend regression tests

- `backend/src/test/java/com/tecnocampus/LS2/protube_back/ProtubeBackApplicationTests.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/application/service/VideoServiceTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/config/SecurityConfigTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/persistence/UserPersistenceAdapterTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/security/AppUserDetailsServiceTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/web/ApiExceptionHandlerTest.java`
- `backend/src/test/java/com/tecnocampus/LS2/protube_back/infrastructure/web/VideosControllerTest.java`

### Frontend

- `frontend/jest.setup.ts`
- `frontend/src/App.css`
- `frontend/src/App.tsx`
- `frontend/src/__tests__/AppAuth.test.tsx`
- `frontend/src/components/LoginPage.tsx`
- `frontend/src/components/ProfileListsPage.tsx`
- `frontend/src/components/ProfilePage.tsx`
- `frontend/src/components/RegisterPage.tsx`
- `frontend/src/components/VideoCard.tsx`
- `frontend/src/components/VideoGrid.tsx`
- `frontend/src/components/WatchPage.tsx`
- `frontend/src/components/__tests__/LoginPage.test.tsx`
- `frontend/src/components/__tests__/ProfileListsPage.test.tsx`
- `frontend/src/components/__tests__/ProfilePage.test.tsx`
- `frontend/src/components/__tests__/RegisterPage.test.tsx`
- `frontend/src/components/__tests__/VideoGrid.test.tsx`
- `frontend/src/components/__tests__/WatchPage.test.tsx`
- `frontend/src/useAllVideos.ts`
- `frontend/src/useProfileLists.ts`
- `frontend/src/utils/Env.ts`

### Project documentation and deployment

- `DEVELOPMENT.md`
- `compose.yaml`
- `docs/architecture.md`
- `docs/decisions.md`
- `docs/project-status.md`

## Validation results

- Backend: `.\mvnw.cmd clean verify` — passed, 107 tests; JaCoCo thresholds met.
- Frontend: `npm run build` — passed.
- Frontend tests: `npm run test -- --runInBand` — passed, 109 tests.
- Changed frontend files: ESLint passed.
- Repository-wide `npm run lint`: still fails on pre-existing CRLF/Prettier errors in unrelated files.
- `git diff --check`: passed.

## Worktree notes

No commit was created. The already-staged `docs/Development_Guide.pdf` and edits to `AGENTS.md` and
`backend/AGENTS.md` were not part of this session's implementation and were left unchanged.
