import { useEffect, useState, type FormEvent } from 'react';
import { Link, useParams } from 'react-router';
import { getEnv } from '../utils/Env';
import { useAuth } from '../useAuth';
import type { Video } from './VideoCard';
import VideoGrid from './VideoGrid';
import userIcon from '../assets/user/default-user.svg';

interface Props {
  videos: Video[];
}

interface VideoComment {
  author: string;
  text: string;
}

const isRecord = (value: unknown): value is Record<string, unknown> => typeof value === 'object' && value !== null;

const isVideoComment = (value: unknown): value is VideoComment =>
  isRecord(value) && typeof value.author === 'string' && typeof value.text === 'string';

const WatchPage = ({ videos }: Props) => {
  const { videoId } = useParams();
  const { user, status } = useAuth();
  const video = videos.find((item) => item.id === videoId);
  const [comments, setComments] = useState<VideoComment[]>([]);
  const [commentsLoading, setCommentsLoading] = useState(true);
  const [commentsError, setCommentsError] = useState('');
  const [newComment, setNewComment] = useState('');

  useEffect(() => {
    if (!video || status === 'loading') {
      if (!video) {
        setCommentsLoading(false);
      }
      return;
    }

    const controller = new AbortController();
    setComments([]);
    setCommentsLoading(true);
    setCommentsError('');

    const loadComments = async () => {
      try {
        const response = await fetch(`${getEnv().MEDIA_BASE_URL}/${encodeURIComponent(video.id)}.json`, {
          signal: controller.signal,
        });
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
  }, [video, status]);

  if (!video) {
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
          src={`${getEnv().MEDIA_BASE_URL}/${encodeURIComponent(video.id)}.mp4`}
          poster={`${getEnv().MEDIA_BASE_URL}/${encodeURIComponent(video.id)}.webp`}
          controls
          autoPlay
          playsInline
          preload="metadata"
          aria-label={video.title}
        >
          Your browser does not support HTML video.
        </video>

        <h1 className="pt-watch-title">{video.title}</h1>
        <p className="pt-watch-channel">{video.channel}</p>

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
        <VideoGrid videos={videos.filter((item) => item.id !== video.id)} compact />
      </aside>
    </div>
  );
};

export default WatchPage;
