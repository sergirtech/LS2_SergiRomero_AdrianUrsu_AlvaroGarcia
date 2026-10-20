import { useState } from 'react';
import { Route, Routes, useNavigate } from 'react-router';
import './App.css';
import { useAllVideos } from './useAllVideos';
import { useAuth } from './useAuth';
import Navbar from './components/Navbar';
import Sidebar from './components/Sidebar';
import VideoGrid, { VideoGridSkeleton } from './components/VideoGrid';
import LoginPage from './components/LoginPage';
import RegisterPage from './components/RegisterPage';

function App() {
  const [query, setQuery] = useState('');
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const { user, status, logout } = useAuth();
  const navigate = useNavigate();

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
      />
      <div className="pt-layout">
        <Sidebar open={sidebarOpen} />
        <main className="pt-main">
          <Routes>
            <Route path="/" element={<ContentApp query={query} />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
          </Routes>
        </main>
      </div>
    </div>
  );
}

function ContentApp({ query }: { query: string }) {
  const { loading, message, value } = useAllVideos();
  switch (loading) {
    case 'loading':
    case 'idle':
      return <VideoGridSkeleton />;
    case 'error':
      return (
        <div className="pt-error">
          <h3>Something went wrong</h3>
          <p>{message}</p>
        </div>
      );
    case 'success': {
      const filtered = value.filter((video) => video.toLowerCase().includes(query.trim().toLowerCase()));
      return (
        <>
          <h2 className="pt-section-title">{query ? `Results for "${query}"` : 'Recommended'}</h2>
          <VideoGrid videos={filtered} />
        </>
      );
    }
  }
}

export default App;
