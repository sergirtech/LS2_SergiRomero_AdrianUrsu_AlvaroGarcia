import { useEffect, useRef } from 'react';
import VideoCard from './VideoCard';
import type { Video } from './VideoCard';

interface Props {
  videos: Video[];
  compact?: boolean;
  hasMore?: boolean;
  loadingMore?: boolean;
  onLoadMore?: () => void;
}

const VideoGrid = ({ videos, compact = false, hasMore = false, loadingMore = false, onLoadMore }: Props) => {
  const loadMoreRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!onLoadMore || !hasMore || loadingMore || !loadMoreRef.current || typeof IntersectionObserver === 'undefined') {
      return;
    }

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) onLoadMore();
      },
      { rootMargin: '0px 0px 300px 0px' }
    );
    observer.observe(loadMoreRef.current);
    return () => observer.disconnect();
  }, [hasMore, loadingMore, onLoadMore]);

  return (
    <>
      {videos.length === 0 ? (
        <p className="pt-empty">No videos found.</p>
      ) : (
        <div className={`pt-grid${compact ? ' pt-grid-compact' : ''}`}>
          {videos.map((video) => (
            <VideoCard key={video.id} video={video} />
          ))}
        </div>
      )}
      {!compact && onLoadMore && hasMore && (
        <div className="pt-load-more">
          <div ref={loadMoreRef} aria-hidden="true" />
          <button type="button" onClick={onLoadMore} disabled={loadingMore}>
            {loadingMore ? 'Loading more videos...' : 'Load more videos'}
          </button>
        </div>
      )}
      {loadingMore && (
        <p className="visually-hidden" role="status">
          Loading more videos...
        </p>
      )}
    </>
  );
};

export const VideoGridSkeleton = ({ count = 8 }: { count?: number }) => {
  return (
    <div className="pt-grid" aria-busy="true" aria-label="Loading videos">
      {Array.from({ length: count }, (_, index) => (
        <div key={index} className="pt-card">
          <div className="pt-thumb pt-skeleton" />
          <div className="pt-card-body">
            <div className="pt-avatar pt-skeleton" />
            <div className="flex-grow-1">
              <div className="pt-skeleton pt-skeleton-line" />
              <div className="pt-skeleton pt-skeleton-line pt-skeleton-short" />
            </div>
          </div>
        </div>
      ))}
    </div>
  );
};

export default VideoGrid;
