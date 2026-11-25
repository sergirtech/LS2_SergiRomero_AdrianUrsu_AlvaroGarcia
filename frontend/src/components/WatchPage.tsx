import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router';
import { getEnv } from '../utils/Env';
import { useAuth } from '../useAuth';
import { useVideos } from '../useVideos';
import { fetchVideo, VideoNotFoundError, type VideoDetailDto } from '../videosApi';
import CommentsSection from './CommentsSection';
import VideoGrid from './VideoGrid';

type VideoState = 'loading' | 'ready' | 'not-found' | 'error';

const WatchPage = () => {
  const { videoId = '' } = useParams();
  const { user } = useAuth();
  const { videos: recommendations } = useVideos();
  const [video, setVideo] = useState<VideoDetailDto | null>(null);
  const [videoState, setVideoState] = useState<VideoState>('loading');
  const [videoError, setVideoError] = useState('');

  useEffect(() => {
    const controller = new AbortController();
    setVideo(null);
    setVideoState('loading');
    fetchVideo(videoId, controller.signal)
      .then((result) => {
        setVideo(result);
        setVideoState('ready');
      })
      .catch((error: unknown) => {
        if (controller.signal.aborted) return;
        if (error instanceof VideoNotFoundError) {
          setVideoState('not-found');
        } else {
          setVideoError(error instanceof Error ? error.message : 'Could not load the video.');
          setVideoState('error');
        }
      });
    return () => controller.abort();
  }, [videoId]);

  if (videoState === 'loading') {
    return (
      <p className="pt-empty" role="status">
        Loading video...
      </p>
    );
  }

  if (videoState === 'error') {
    return (
      <div className="pt-error" role="alert">
        <h2>Could not load the video</h2>
        <p>{videoError}</p>
        <Link to="/">Back to videos</Link>
      </div>
    );
  }

  if (videoState === 'not-found' || !video) {
    return (
      <div className="pt-error">
        <h2>Video not found</h2>
        <Link to="/">Back to videos</Link>
      </div>
    );
  }

  return (
    <div className="pt-watch-layout">
      <main className="pt-watch-main">
        <video
          className="pt-player"
          src={`${getEnv().MEDIA_BASE_URL}/${encodeURIComponent(video.fileKey)}.mp4`}
          poster={`${getEnv().MEDIA_BASE_URL}/${encodeURIComponent(video.fileKey)}.webp`}
          controls
          autoPlay
          playsInline
          preload="metadata"
          aria-label={video.title}
        >
          Your browser does not support HTML video.
        </video>

        <h1 className="pt-watch-title">{video.title}</h1>
        <div className="pt-watch-channel">
          <span>{video.author}</span>
          {user && video.ownerId === user.id && (
            <Link to={`/my-videos?edit=${video.id}`} className="btn btn-outline-secondary btn-sm">
              Edit video
            </Link>
          )}
        </div>

        {(video.category || video.description || video.tags.length > 0) && (
          <section className="pt-watch-details" aria-label="Video details">
            {video.category && <p className="pt-watch-category">{video.category}</p>}
            {video.description && <p className="pt-watch-description">{video.description}</p>}
            {video.tags.length > 0 && (
              <ul className="pt-watch-tags" aria-label="Tags">
                {video.tags.map((tag) => (
                  <li key={tag}>#{tag}</li>
                ))}
              </ul>
            )}
          </section>
        )}

        <CommentsSection key={video.id} videoId={video.id} signedIn={user !== null} />
      </main>

      <aside className="pt-watch-recommendations" aria-labelledby="recommendations-heading">
        <h2 id="recommendations-heading">Up next</h2>
        <VideoGrid videos={recommendations.filter((item) => item.id !== videoId)} compact />
      </aside>
    </div>
  );
};

export default WatchPage;
