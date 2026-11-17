import { useEffect, useState, type FormEvent } from 'react';
import { Link, useParams } from 'react-router';
import { useAuth } from '../useAuth';
import { getEnv } from '../utils/Env';
import { getVideoFileUrl, getVideoUrl, parseVideo } from '../videoApi';
import type { Video } from './VideoCard';
import VideoGrid from './VideoGrid';
import userIcon from '../assets/user/default-user.svg';

interface Props {
  videos: Video[];
}

interface VideoComment {
  id: number;
  videoId: number;
  authorId: number;
  author: string;
  text: string;
  parentId: number | null;
  likeCount: number;
  likedByCurrentUser: boolean;
  replies: VideoComment[];
}

const isRecord = (value: unknown): value is Record<string, unknown> => typeof value === 'object' && value !== null;

const parseComments = (value: unknown): VideoComment[] => {
  if (!Array.isArray(value)) throw new Error('The comments response has an invalid format.');
  const parsed = value.map((item): VideoComment => {
    if (
      !isRecord(item) ||
      typeof item.id !== 'number' ||
      typeof item.videoId !== 'number' ||
      typeof item.authorId !== 'number' ||
      typeof item.authorUsername !== 'string' ||
      typeof item.text !== 'string' ||
      !(typeof item.parentId === 'number' || item.parentId === null) ||
      typeof item.likeCount !== 'number' ||
      typeof item.likedByCurrentUser !== 'boolean'
    ) {
      throw new Error('A comment in the response has an invalid format.');
    }
    return {
      id: item.id,
      videoId: item.videoId,
      authorId: item.authorId,
      author: item.authorUsername,
      text: item.text,
      parentId: item.parentId,
      likeCount: item.likeCount,
      likedByCurrentUser: item.likedByCurrentUser,
      replies: [],
    };
  });
  const byId = new Map(parsed.map((comment) => [comment.id, comment]));
  const roots: VideoComment[] = [];
  parsed.forEach((comment) => {
    const parent = comment.parentId === null ? undefined : byId.get(comment.parentId);
    if (parent) parent.replies.push(comment);
    else roots.push(comment);
  });
  return roots;
};

const WatchPage = ({ videos }: Props) => {
  const { videoId } = useParams();
  const { user } = useAuth();
  const [video, setVideo] = useState<Video | null>(null);
  const [videoLoading, setVideoLoading] = useState(true);
  const [videoError, setVideoError] = useState('');
  const [videoMissing, setVideoMissing] = useState(false);
  const [comments, setComments] = useState<VideoComment[]>([]);
  const [commentsLoading, setCommentsLoading] = useState(true);
  const [commentsError, setCommentsError] = useState('');
  const [newComment, setNewComment] = useState('');
  const [replyTo, setReplyTo] = useState<number | null>(null);
  const [commentActionError, setCommentActionError] = useState('');

  useEffect(() => {
    if (!videoId) {
      setVideoMissing(true);
      setVideoLoading(false);
      return;
    }

    const controller = new AbortController();
    setVideo(null);
    setVideoLoading(true);
    setVideoError('');
    setVideoMissing(false);

    const loadVideo = async () => {
      try {
        const response = await fetch(getVideoUrl(videoId), {
          signal: controller.signal,
          credentials: 'include',
        });
        if (response.status === 404) {
          setVideoMissing(true);
          return;
        }
        if (!response.ok) {
          throw new Error(`Could not load this video (HTTP ${response.status}).`);
        }

        const loadedVideo = parseVideo(await response.json());
        if (loadedVideo.id !== videoId) {
          throw new Error('The video response did not match the requested ID.');
        }
        setVideo(loadedVideo);
      } catch (error: unknown) {
        if (!controller.signal.aborted) {
          setVideoError(error instanceof Error ? error.message : 'Could not load this video.');
        }
      } finally {
        if (!controller.signal.aborted) {
          setVideoLoading(false);
        }
      }
    };

    void loadVideo();
    return () => controller.abort();
  }, [videoId]);

  useEffect(() => {
    if (!video) {
      setComments([]);
      setCommentsLoading(false);
      return;
    }

    const controller = new AbortController();
    setComments([]);
    setCommentsLoading(true);
    setCommentsError('');

    const loadComments = async () => {
      try {
        const response = await fetch(`${getVideoUrl(video.id)}/comments`, {
          signal: controller.signal,
          credentials: 'include',
        });
        if (!response.ok) {
          throw new Error(`Could not load comments (HTTP ${response.status}).`);
        }
        setComments(parseComments(await response.json()));
      } catch (error: unknown) {
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
  }, [video]);

  if (videoLoading || (video !== null && video.id !== videoId)) {
    return (
      <p className="pt-error" role="status">
        Loading video...
      </p>
    );
  }

  if (videoError) {
    return (
      <div className="pt-error">
        <h2>Could not load video</h2>
        <p role="alert">{videoError}</p>
        <Link to="/">Back to videos</Link>
      </div>
    );
  }

  if (videoMissing || !video) {
    return (
      <div className="pt-error">
        <h2>Video not found</h2>
        <Link to="/">Back to videos</Link>
      </div>
    );
  }

  const handleAddComment = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const text = newComment.trim();
    if (!text || !user) {
      return;
    }

    setCommentActionError('');
    try {
      const response = await fetch(`${getVideoUrl(video.id)}/comments`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ text, parentId: replyTo }),
      });
      if (!response.ok) throw new Error(`Could not add comment (HTTP ${response.status}).`);
      const created: unknown = await response.json();
      if (!isRecord(created) || typeof created.id !== 'number') {
        throw new Error('Comment was saved, but the response was invalid.');
      }
      const fresh = await fetch(`${getVideoUrl(video.id)}/comments`, { credentials: 'include' });
      if (!fresh.ok) throw new Error(`Comment saved, but comments could not be refreshed (HTTP ${fresh.status}).`);
      setComments(parseComments(await fresh.json()));
      setNewComment('');
      setReplyTo(null);
    } catch (error: unknown) {
      setCommentActionError(error instanceof Error ? error.message : 'Could not add comment.');
    }
  };

  const handleToggleLike = async (commentId: number) => {
    setCommentActionError('');
    try {
      const response = await fetch(`${getEnv().API_BASE_URL}/comments/${commentId}/likes`, {
        method: 'POST',
        credentials: 'include',
      });
      if (!response.ok) throw new Error(`Could not update comment like (HTTP ${response.status}).`);
      const refreshed = await fetch(`${getVideoUrl(video.id)}/comments`, { credentials: 'include' });
      if (!refreshed.ok) throw new Error(`Could not refresh comments (HTTP ${refreshed.status}).`);
      setComments(parseComments(await refreshed.json()));
    } catch (error: unknown) {
      setCommentActionError(error instanceof Error ? error.message : 'Could not update comment like.');
    }
  };

  const renderComment = (comment: VideoComment) => (
    <li className="pt-comment" key={comment.id}>
      <img src={userIcon} alt="" />
      <div>
        <strong>@{comment.author}</strong>
        <p>{comment.text}</p>
        {user ? (
          <>
            <button
              type="button"
              className="pt-comment-action"
              onClick={() => void handleToggleLike(comment.id)}
              aria-label={`Like comment by ${comment.author} (${comment.likeCount})`}
            >
              {comment.likeCount} {comment.likedByCurrentUser ? 'Liked' : 'Like'}
            </button>
            <button type="button" className="pt-comment-action" onClick={() => setReplyTo(comment.id)}>
              Reply
            </button>
          </>
        ) : (
          <Link to="/login">Sign in to like or reply</Link>
        )}
        {comment.replies.length > 0 && <ul>{comment.replies.map(renderComment)}</ul>}
      </div>
    </li>
  );

  return (
    <div className="pt-watch-layout">
      <main className="pt-watch-main">
        <video
          className="pt-player"
          src={getVideoFileUrl(video.id)}
          poster={video.thumbnailUrl ?? undefined}
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
        {video.description && <p className="pt-watch-description">{video.description}</p>}

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
                {replyTo ? 'Reply' : 'Comment'}
              </button>
              {replyTo && (
                <button type="button" onClick={() => setReplyTo(null)}>
                  Cancel reply
                </button>
              )}
            </form>
          ) : (
            <div className="pt-comment-signin">
              <img src={userIcon} alt="" />
              <Link to="/login">Sign in to add a comment</Link>
            </div>
          )}
          {commentsLoading && <p role="status">Loading comments...</p>}
          {commentsError && (
            <p className="pt-comment-error" role="alert">
              {commentsError}
            </p>
          )}
          {!commentsLoading && !commentsError && comments.length === 0 && <p>No comments yet.</p>}
          {commentActionError && (
            <p className="pt-comment-error" role="alert">
              {commentActionError}
            </p>
          )}
          <ul className="pt-comment-list">{comments.map(renderComment)}</ul>
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
