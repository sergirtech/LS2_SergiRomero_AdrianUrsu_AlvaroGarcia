import { useEffect, useState, type ComponentProps } from 'react';
import { Route, Routes, useLocation, useNavigate } from 'react-router';
import './App.css';
import { useAuth } from './useAuth';
import { useAllVideos } from './useAllVideos';
import Navbar from './components/Navbar';
import Sidebar from './components/Sidebar';
import VideoGrid, { VideoGridSkeleton } from './components/VideoGrid';
import LoginPage from './components/LoginPage';
import RegisterPage from './components/RegisterPage';
import WatchPage from './components/WatchPage';
import ProfilePage from './components/ProfilePage';
import { useMyVideos } from './useMyVideos';
import { useMyComments } from './useMyComments';
import { PROFILE_STATS_CHANGE_EVENT, type ProfileStats, type ProfileStatsChange } from './profileStats';
import { useProfileLists } from './useProfileLists';
import { useProfilePhoto } from './useProfilePhoto';

const applyStatChange = (current: number, change: number | undefined): number => {
  if (typeof change !== 'number' || !Number.isSafeInteger(change)) return current;
  const updated = current + change;
  return Number.isSafeInteger(updated) && updated >= 0 ? updated : current;
};

function App() {
  const [query, setQuery] = useState('');
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [darkMode, setDarkMode] = useState(() => {
    try {
      return window.localStorage.getItem('protube:theme') === 'dark';
    } catch {
      return false;
    }
  });
  const [profileStats, setProfileStats] = useState<ProfileStats>({ lists: 0, followers: 0 });
  const { user, status, logout } = useAuth();
  const location = useLocation();
  const catalogEnabled =
    location.pathname === '/' || location.pathname.startsWith('/watch/') || location.pathname === '/profile/lists';
  const videoCatalog = useAllVideos(catalogEnabled);
  const profileLists = useProfileLists(user?.id ?? null);
  const navigate = useNavigate();

  useEffect(() => {
    document.documentElement.dataset.bsTheme = darkMode ? 'dark' : 'light';
    try {
      window.localStorage.setItem('protube:theme', darkMode ? 'dark' : 'light');
    } catch {
      // Theme still applies for this session when browser storage is unavailable.
    }
  }, [darkMode]);

  useEffect(() => {
    const handleProfileStatsChange = (event: Event) => {
      const { detail } = event as CustomEvent<ProfileStatsChange>;
      if (!detail) return;

      setProfileStats((current) => ({ ...current, followers: applyStatChange(current.followers, detail.followers) }));
    };

    window.addEventListener(PROFILE_STATS_CHANGE_EVENT, handleProfileStatsChange);
    return () => window.removeEventListener(PROFILE_STATS_CHANGE_EVENT, handleProfileStatsChange);
  }, []);

  const handleSignOut = async () => {
    try {
      await logout();
      navigate('/');
    } catch {
      // Server unreachable: the session is still open, so the user stays signed in.
    }
  };

  return (
    <div className="pt-app">
      <Navbar
        query={query}
        onQueryChange={setQuery}
        onToggleSidebar={() => setSidebarOpen((open) => !open)}
        user={user}
        authReady={status === 'ready'}
        onSignOut={handleSignOut}
        darkMode={darkMode}
        onDarkModeChange={setDarkMode}
      />
      <div className="pt-layout">
        <Sidebar open={sidebarOpen} />
        <main className="pt-main">
          <Routes>
            <Route path="/" element={<ContentApp query={query} {...videoCatalog} />} />
            <Route path="/watch/:videoId" element={<WatchPage videos={videoCatalog.videos} />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route
              path="/profile/*"
              element={
                <ProfileRoute
                  key={user?.id ?? 'signed-out'}
                  refreshCatalog={videoCatalog.refresh}
                  user={user}
                  stats={{ ...profileStats, lists: profileLists.lists.length }}
                  videos={videoCatalog.videos}
                  lists={profileLists.lists}
                  listsLoading={profileLists.loading}
                  listsError={profileLists.error}
                  onCreateList={profileLists.createList}
                  onRenameList={profileLists.renameList}
                  onAddVideo={profileLists.addVideo}
                  onRemoveVideo={profileLists.removeVideo}
                  onDeleteList={profileLists.deleteList}
                />
              }
            />
          </Routes>
        </main>
      </div>
      <footer className="pt-footer">Tube Enjoyers</footer>
    </div>
  );
}

type ProfileRouteProps = Omit<ComponentProps<typeof ProfilePage>, 'videoManager'> & {
  refreshCatalog: () => Promise<void>;
};

function ProfileRoute({ refreshCatalog, ...profileProps }: ProfileRouteProps) {
  const { pathname } = useLocation();
  const isProfileHome = pathname === '/profile' || pathname === '/profile/';
  const videos = useMyVideos(profileProps.user?.id ?? null, isProfileHome, refreshCatalog);
  const comments = useMyComments(profileProps.user?.id ?? null, isProfileHome);
  const photo = useProfilePhoto(profileProps.user?.id ?? null, pathname.startsWith('/profile'));

  return (
    <ProfilePage
      {...profileProps}
      photoUrl={photo.photoUrl}
      photoLoading={photo.loading}
      photoSaving={photo.saving}
      photoError={photo.error}
      onUploadPhoto={photo.upload}
      onDeletePhoto={photo.remove}
      videoManager={{
        videos: videos.videos,
        loading: videos.loading,
        loadingMore: videos.loadingMore,
        hasMore: videos.hasMore,
        error: videos.error,
        onRetry: videos.retry,
        onLoadMore: videos.loadMore,
        onUpload: (title, description, file) => videos.uploadVideo({ title, description, file }),
        onEdit: videos.editVideo,
        onDelete: videos.deleteVideo,
      }}
      commentsManager={{
        comments: comments.comments,
        loading: comments.loading,
        error: comments.error,
        onRetry: () => void comments.refresh(),
        onEdit: comments.edit,
        onDelete: comments.remove,
      }}
    />
  );
}

interface ContentAppProps {
  query: string;
  videos: ReturnType<typeof useAllVideos>['videos'];
  loading: boolean;
  loadingMore: boolean;
  error: string | null;
  hasMore: boolean;
  loadMore: () => void;
  retry: () => void;
}

function ContentApp({ query, videos, loading, loadingMore, error, hasMore, loadMore, retry }: ContentAppProps) {
  const normalizedQuery = query.trim().toLowerCase();
  const filtered = videos.filter((video) =>
    `${video.title} ${video.channel} ${video.description ?? ''}`.toLowerCase().includes(normalizedQuery)
  );

  return (
    <>
      <h2 className="pt-section-title">{query ? `Results for "${query}"` : 'Recommended'}</h2>
      {loading && videos.length === 0 ? (
        <VideoGridSkeleton count={20} />
      ) : (
        <>
          {error && (
            <div className="pt-video-error" role="alert">
              <p>{error}</p>
              <button type="button" className="btn btn-outline-secondary" onClick={retry}>
                Retry
              </button>
            </div>
          )}
          {!(error && videos.length === 0) && (
            <VideoGrid
              videos={filtered}
              hasMore={hasMore && !error}
              loadingMore={loadingMore}
              onLoadMore={error ? undefined : loadMore}
            />
          )}
        </>
      )}
    </>
  );
}

export default App;
