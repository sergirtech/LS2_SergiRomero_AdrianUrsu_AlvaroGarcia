import { useState } from 'react';
import './App.css';
import { useAllVideos } from './useAllVideos';
import Navbar from './components/Navbar';
import Sidebar from './components/Sidebar';
import VideoGrid, { VideoGridSkeleton } from './components/VideoGrid';

function App() {
  const [query, setQuery] = useState('');
  const [sidebarOpen, setSidebarOpen] = useState(true);

  return (
    <div className="pt-app">
      <Navbar query={query} onQueryChange={setQuery} onToggleSidebar={() => setSidebarOpen((open) => !open)} />
      <div className="pt-layout">
        <Sidebar open={sidebarOpen} />
        <main className="pt-main">
          <ContentApp query={query} />
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
