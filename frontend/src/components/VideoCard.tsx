import { useState } from 'react';
import { Link } from 'react-router';
import { getEnv } from '../utils/Env';

export interface Video {
  id: string;
  title: string;
  channel: string;
  viewCount: number;
}

interface Props {
  video: Video;
}

const VideoCard = ({ video }: Props) => {
  const [thumbnailFailed, setThumbnailFailed] = useState(false);
  const formattedViews = new Intl.NumberFormat('en', {
    notation: 'compact',
    maximumFractionDigits: 1,
  }).format(video.viewCount);

  return (
    <Link to={`/watch/${encodeURIComponent(video.id)}`} className="pt-card-link">
      <article className="pt-card">
        <div className="pt-thumb">
          {thumbnailFailed ? (
            <div className="pt-thumb-placeholder" data-testid="thumb-placeholder">
              <svg viewBox="0 0 24 24" width="44" height="44" aria-hidden="true">
                <path d="M8 5v14l11-7z" fill="currentColor" />
              </svg>
            </div>
          ) : (
            <img
              src={`${getEnv().MEDIA_BASE_URL}/${encodeURIComponent(video.id)}.webp`}
              alt={`${video.title} thumbnail`}
              loading="lazy"
              onError={() => setThumbnailFailed(true)}
            />
          )}
        </div>
        <div className="pt-card-body">
          <div>
            <h3 className="pt-card-title" title={video.title}>
              {video.title}
            </h3>
            <p className="pt-card-meta">
              {video.channel}
              <span className="pt-meta-separator" aria-hidden="true">
                ·
              </span>
              {formattedViews} views
            </p>
          </div>
        </div>
      </article>
    </Link>
  );
};

export default VideoCard;
