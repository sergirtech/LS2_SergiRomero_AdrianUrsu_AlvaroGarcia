import { useState } from 'react';
import { getEnv } from '../utils/Env';

interface Props {
  title: string;
}

const VideoCard = ({ title }: Props) => {
  const [thumbnailFailed, setThumbnailFailed] = useState(false);

  return (
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
            src={`${getEnv().MEDIA_BASE_URL}/${encodeURIComponent(title)}.webp`}
            alt=""
            loading="lazy"
            onError={() => setThumbnailFailed(true)}
          />
        )}
      </div>
      <div className="pt-card-body">
        <div className="pt-avatar" aria-hidden="true">
          {title.charAt(0).toUpperCase()}
        </div>
        <div>
          <h3 className="pt-card-title" title={title}>
            {title}
          </h3>
          <p className="pt-card-meta">ProTube</p>
        </div>
      </div>
    </article>
  );
};

export default VideoCard;
