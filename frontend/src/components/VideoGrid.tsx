import VideoCard from './VideoCard';
import type { Video } from './VideoCard';

interface Props {
  videos: Video[];
  compact?: boolean;
}

const VideoGrid = ({ videos, compact = false }: Props) => {
  if (videos.length === 0) {
    return <p className="pt-empty">No videos found.</p>;
  }

  return (
    <div className={`pt-grid${compact ? ' pt-grid-compact' : ''}`}>
      {videos.map((video) => (
        <VideoCard key={video.id} video={video} />
      ))}
    </div>
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
