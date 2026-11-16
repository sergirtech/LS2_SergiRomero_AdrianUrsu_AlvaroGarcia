import { useEffect, useState } from 'react';
import type { Video } from './components/VideoCard';
import { fetchVideoPage, toVideo } from './videosApi';

type Status = 'loading' | 'ready' | 'error';

/** Paginated catalog from GET /api/videos (US-04). loadMore() appends the next page. */
export function useVideos() {
  const [videos, setVideos] = useState<Video[]>([]);
  const [page, setPage] = useState(0);
  const [attempt, setAttempt] = useState(0);
  const [hasNext, setHasNext] = useState(false);
  const [status, setStatus] = useState<Status>('loading');
  const [error, setError] = useState('');

  useEffect(() => {
    const controller = new AbortController();
    setStatus('loading');
    setError('');
    fetchVideoPage(page, controller.signal)
      .then((result) => {
        const loaded = result.items.map(toVideo);
        setVideos((current) => (page === 0 ? loaded : [...current, ...loaded]));
        setHasNext(result.hasNext);
        setStatus('ready');
      })
      .catch((err: unknown) => {
        if (controller.signal.aborted) return;
        setError(err instanceof Error ? err.message : 'Could not load videos.');
        setStatus('error');
      });
    return () => controller.abort();
  }, [page, attempt]);

  const loadMore = () => {
    if (status === 'ready' && hasNext) setPage((current) => current + 1);
  };
  const retry = () => setAttempt((current) => current + 1);

  return { videos, status, error, hasNext, loadMore, retry };
}
