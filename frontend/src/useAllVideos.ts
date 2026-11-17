import { useCallback, useEffect, useRef, useState } from 'react';
import { getVideosUrl, parseVideoPage } from './videoApi';
import type { Video } from './components/VideoCard';

export function useVideoPages(endpoint: string, enabled = true) {
  const [videos, setVideos] = useState<Video[]>([]);
  const [nextCursor, setNextCursor] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const requestInFlight = useRef<Promise<void> | null>(null);
  const requested = useRef(false);

  const requestPage = useCallback(
    (cursor?: number): Promise<void> => {
      if (requestInFlight.current) return requestInFlight.current;

      const isInitialPage = cursor === undefined;
      setError(null);
      if (isInitialPage) {
        setLoading(true);
      } else {
        setLoadingMore(true);
      }

      const request = (async () => {
        try {
          const url = cursor === undefined ? endpoint : `${endpoint}?cursor=${encodeURIComponent(String(cursor))}`;
          const response = await fetch(url, { credentials: 'include' });
          if (!response.ok) {
            throw new Error(`Could not load videos (HTTP ${response.status}).`);
          }

          const page = parseVideoPage(await response.json());
          setVideos((current) =>
            isInitialPage
              ? page.items
              : [...current, ...page.items.filter((video) => !current.some((item) => item.id === video.id))]
          );
          setNextCursor(page.nextCursor);
        } catch (requestError) {
          setError(requestError instanceof Error ? requestError.message : 'Could not load videos.');
        } finally {
          requestInFlight.current = null;
          setLoading(false);
          setLoadingMore(false);
        }
      })();
      requestInFlight.current = request;
      return request;
    },
    [endpoint]
  );

  useEffect(() => {
    if (!enabled || requested.current) return;
    requested.current = true;
    void requestPage();
  }, [enabled, requestPage]);

  const loadMore = useCallback(() => {
    if (nextCursor !== null) {
      void requestPage(nextCursor);
    }
  }, [nextCursor, requestPage]);

  const retry = useCallback(() => {
    if (videos.length === 0) {
      void requestPage();
    } else if (nextCursor !== null) {
      void requestPage(nextCursor);
    }
  }, [nextCursor, requestPage, videos.length]);

  const refresh = useCallback(async () => {
    if (requestInFlight.current) await requestInFlight.current;
    await requestPage();
  }, [requestPage]);

  return {
    videos,
    loading,
    loadingMore,
    error,
    hasMore: nextCursor !== null,
    loadMore,
    refresh,
    retry,
  };
}

export function useAllVideos(enabled = true) {
  return useVideoPages(getVideosUrl(), enabled);
}
