import { useEffect, useState, type FormEvent } from 'react';
import { Link, useParams } from 'react-router';
import { getEnv } from '../utils/Env';
import { useAuth } from '../useAuth';
import { useVideos } from '../useVideos';
import { fetchVideo, VideoNotFoundError, type VideoDetailDto } from '../videosApi';
import VideoGrid from './VideoGrid';
import userIcon from '../assets/user/default-user.svg';

interface VideoComment {
  author: string;
  text: string;
}

type VideoState = 'loading' | 'ready' | 'not-found' | 'error';

const isRecord = (value: unknown): value is Record<string, unknown> => typeof value === 'object' && value !== null;

const isVideoComment = (value: unknown): value is VideoComment =>
  isRecord(value) && typeof value.author === 'string' && typeof value.text === 'string';

const WatchPage = () => {
  const { videoId = '' } = useParams();
  const { user, status } = useAuth();
  const { videos: recommendations } = useVideos();
  const [video, setVideo] = useState<VideoDetailDto | null>(null);
  const [videoState, setVideoState] = useState<VideoState>('loading');
  const [videoError, setVideoError] = useState('');
  const [comments, setComments] = useState<VideoComment[]>([]);
  const [commentsLoading, setCommentsLoading] = useState(true);
  const [commentsError, setCommentsError] = useState('');
  const [newComment, setNewComment] = useState('');
  const fileKey = video?.fileKey;

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

  useEffect(() => {
    if (!fileKey || status === 'loading') return;

    const controller = new AbortController();
    setComments([]);
    setCommentsLoading(true);
    setCommentsError('');

    const loadComments = async () => {
      try {
        // US-09 will read comments from the API; until then they come from the grabber metadata.
        const response = await fetch(`${getEnv().MEDIA_BASE_URL}/${encodeURIComponent(fileKey)}.json`, {
          signal: controller.signal,
        });
        // Uploaded videos have no grabber .json: that just means no comments yet.
        if (response.status === 404) {
          setComments([]);
          return;
        }
        if (!response.ok) {
          throw new Error(`Could not load comments (HTTP ${response.status}).`);
        }

        const data: unknown = await response.json();
        if (!isRecord(data) || !isRecord(data.meta) || !Array.isArray(data.meta.comments)) {
          throw new Error('The video metadata does not contain a valid comments list.');
        }

        setComments(data.meta.comments.filter(isVideoComment));
      } catch (error) {
        if (!controller.signal.aborted) {
          setCommentsError(error instanceof Error ? error.message : 'Could not load comments.');
        }
      } finally {
        if (!controller.signal.aborted) {
          setCommentsLoading(false);
        }
      }
    };

    void loadComments();
    return () => controller.abort();
  }, [fileKey, status]);

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

  const handleAddComment = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const text = newComment.trim();
    if (!text || !user) {
      return;
    }

    setComments((current) => [{ author: user.username, text }, ...current]);
    setNewComment('');
  };

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

        <section className="pt-comments" aria-labelledby="comments-heading">
          <h2 id="comments-heading">Comments</h2>
          {user ? (
            <form className="pt-comment-form" onSubmit={handleAddComment}>
              <label className="visually-hidden" htmlFor="new-comment">
                Add a comment
              </label>
              <img src={userIcon} alt="" />
              <input
                id="new-comment"
                value={newComment}
                onChange={(event) => setNewComment(event.target.value)}
                placeholder="Add a comment..."
                maxLength={1000}
              />
              <button type="submit" disabled={!newComment.trim()}>
                Comment
              </button>
              <span className="visually-hidden">Comments added here are temporary and are not saved.</span>
            </form>
          ) : (
            <div className="pt-comment-signin">
              <img src={userIcon} alt="" />
              <Link to="/login">Sign in to add a comment</Link>
            </div>
          )}
          <p className="pt-comment-note">Comments you add are temporary and are not saved.</p>

          {commentsLoading && <p role="status">Loading comments...</p>}
          {commentsError && (
            <p className="pt-comment-error" role="alert">
              {commentsError}
            </p>
          )}
          {!commentsLoading && !commentsError && comments.length === 0 && <p>No comments yet.</p>}
          <ul className="pt-comment-list">
            {comments.map((comment, index) => (
              <li className="pt-comment" key={`${comment.author}-${index}`}>
                <img src={userIcon} alt="" />
                <div>
                  <strong>@{comment.author}</strong>
                  <p>{comment.text}</p>
                </div>
              </li>
            ))}
          </ul>
        </section>
      </main>

      <aside className="pt-watch-recommendations" aria-labelledby="recommendations-heading">
        <h2 id="recommendations-heading">Up next</h2>
        <VideoGrid videos={recommendations.filter((item) => item.id !== videoId)} compact />
      </aside>
    </div>
  );
};

export default WatchPage;
