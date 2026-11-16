import { useEffect, useState } from 'react';
import { Route, Routes, useNavigate } from 'react-router';
import './App.css';
import { useAuth } from './useAuth';
import Navbar from './components/Navbar';
import Sidebar from './components/Sidebar';
import VideoGrid from './components/VideoGrid';
import LoginPage from './components/LoginPage';
import RegisterPage from './components/RegisterPage';
import WatchPage from './components/WatchPage';
import ProfilePage from './components/ProfilePage';
import type { Video } from './components/VideoCard';
import { PROFILE_STATS_CHANGE_EVENT, type ProfileStats, type ProfileStatsChange } from './profileStats';
import { useProfileLists } from './useProfileLists';

const FEATURED_VIDEOS: Video[] = [
  {
    id: '0',
    title: 'Mark Ronson - Uptown Funk (Official Video) ft. Bruno Mars',
    channel: 'Mark Ronson',
    viewCount: 5909815318,
  },
  {
    id: '1',
    title: "Meat Loaf - I'd Do Anything For Love (But I Won't Do That) (Official Music Video)",
    channel: 'Meat Loaf',
    viewCount: 344248053,
  },
  {
    id: '2',
    title: 'Taylor Swift - Shake It Off',
    channel: 'Taylor Swift',
    viewCount: 3733580673,
  },
  {
    id: '3',
    title: 'Ed Sheeran - BLOW (with Chris Stapleton & Bruno Mars) [Official Music Video]',
    channel: 'Ed Sheeran',
    viewCount: 25260403,
  },
  {
    id: '4',
    title: 'Danny Ocean - Me Rehúso (Official Audio)',
    channel: 'Danny Ocean',
    viewCount: 2097733567,
  },
];

const applyStatChange = (current: number, change: number | undefined): number => {
  if (typeof change !== 'number' || !Number.isSafeInteger(change)) return current;
  const updated = current + change;
  return Number.isSafeInteger(updated) && updated >= 0 ? updated : current;
};

function App() {
  const [query, setQuery] = useState('');
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [darkMode, setDarkMode] = useState(false);
  const [profileStats, setProfileStats] = useState<ProfileStats>({ lists: 0, followers: 0 });
  const { user, status, logout } = useAuth();
  const profileLists = useProfileLists(user?.id ?? null);
  const navigate = useNavigate();

  useEffect(() => {
    document.documentElement.dataset.bsTheme = darkMode ? 'dark' : 'light';
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
            <Route path="/" element={<ContentApp query={query} />} />
            <Route path="/watch/:videoId" element={<WatchPage videos={FEATURED_VIDEOS} />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route
              path="/profile/*"
              element={
                <ProfilePage
                  user={user}
                  stats={{ ...profileStats, lists: profileLists.lists.length }}
                  videos={FEATURED_VIDEOS}
                  lists={profileLists.lists}
                  listsLoading={profileLists.loading}
                  listsError={profileLists.error}
                  onCreateList={profileLists.createList}
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

function ContentApp({ query }: { query: string }) {
  const normalizedQuery = query.trim().toLowerCase();
  const filtered = FEATURED_VIDEOS.filter((video) =>
    `${video.title} ${video.channel}`.toLowerCase().includes(normalizedQuery)
  );

  return (
    <>
      <h2 className="pt-section-title">{query ? `Results for "${query}"` : 'Recommended'}</h2>
      <VideoGrid videos={filtered} />
    </>
  );
}

export default App;
