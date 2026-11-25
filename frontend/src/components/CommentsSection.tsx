import { useState, type FormEvent } from 'react';
import { Link } from 'react-router';
import { useComments } from '../useComments';
import { MAX_COMMENT_LENGTH } from '../videosApi';
import userIcon from '../assets/user/default-user.svg';

interface Props {
  videoId: number;
  signedIn: boolean;
}

const CommentsSection = ({ videoId, signedIn }: Props) => {
  const { comments, loading, error, add } = useComments(videoId);
  const [newComment, setNewComment] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState('');

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const text = newComment.trim();
    if (!text || submitting) return;

    setSubmitting(true);
    setSubmitError('');
    try {
      await add(text);
      setNewComment('');
    } catch (err: unknown) {
      // The text stays in the box, so nothing the user wrote is lost.
      setSubmitError(err instanceof Error ? err.message : 'Could not add the comment.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section className="pt-comments" aria-labelledby="comments-heading">
      <h2 id="comments-heading">Comments</h2>
      {signedIn ? (
        <form className="pt-comment-form" onSubmit={handleSubmit}>
          <label className="visually-hidden" htmlFor="new-comment">
            Add a comment
          </label>
          <img src={userIcon} alt="" />
          <input
            id="new-comment"
            value={newComment}
            onChange={(event) => setNewComment(event.target.value)}
            placeholder="Add a comment..."
            maxLength={MAX_COMMENT_LENGTH}
            readOnly={submitting}
          />
          <button type="submit" disabled={submitting || !newComment.trim()}>
            Comment
          </button>
        </form>
      ) : (
        <div className="pt-comment-signin">
          <img src={userIcon} alt="" />
          <Link to="/login">Sign in to add a comment</Link>
        </div>
      )}
      {submitError && (
        <p className="pt-comment-error" role="alert">
          {submitError}
        </p>
      )}

      {loading && <p role="status">Loading comments...</p>}
      {error && (
        <p className="pt-comment-error" role="alert">
          {error}
        </p>
      )}
      {!loading && !error && comments.length === 0 && <p>No comments yet.</p>}
      <ul className="pt-comment-list">
        {comments.map((comment) => (
          <li className="pt-comment" key={comment.id}>
            <img src={userIcon} alt="" />
            <div>
              <strong>@{comment.authorName}</strong>
              <p>{comment.text}</p>
            </div>
          </li>
        ))}
      </ul>
    </section>
  );
};

export default CommentsSection;
