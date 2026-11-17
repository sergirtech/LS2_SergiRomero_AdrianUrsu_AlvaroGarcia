import { useState } from 'react';
import { Link } from 'react-router';
import type { MyComment } from '../useMyComments';

interface Props {
  comments: MyComment[];
  loading: boolean;
  error: string | null;
  onRetry: () => void;
  onEdit: (id: number, text: string) => Promise<void>;
  onDelete: (id: number) => Promise<void>;
}

const ProfileCommentsManager = ({ comments, loading, error, onRetry, onEdit, onDelete }: Props) => {
  const [editingId, setEditingId] = useState<number | null>(null);
  const [text, setText] = useState('');
  const [actionError, setActionError] = useState('');
  const [busy, setBusy] = useState(false);

  const save = async (id: number) => {
    setBusy(true);
    setActionError('');
    try {
      await onEdit(id, text.trim());
      setEditingId(null);
    } catch (error: unknown) {
      setActionError(error instanceof Error ? error.message : 'Could not update comment.');
    } finally {
      setBusy(false);
    }
  };

  const remove = async (id: number) => {
    if (!window.confirm('Delete this comment?')) return;
    setBusy(true);
    setActionError('');
    try {
      await onDelete(id);
    } catch (error: unknown) {
      setActionError(error instanceof Error ? error.message : 'Could not delete comment.');
    } finally {
      setBusy(false);
    }
  };

  return (
    <section className="pt-profile-comments" aria-labelledby="pt-profile-comments-title">
      <h2 id="pt-profile-comments-title">My comments</h2>
      {error && (
        <div role="alert">
          <p>{error}</p>
          <button type="button" onClick={onRetry}>
            Retry
          </button>
        </div>
      )}
      {actionError && <p role="alert">{actionError}</p>}
      {loading && <p role="status">Loading your comments...</p>}
      {!loading && !error && comments.length === 0 && <p>You haven’t commented on any videos yet.</p>}
      <ul>
        {comments.map((comment) => (
          <li key={comment.id}>
            <Link to={`/watch/${comment.videoId}`}>{comment.videoTitle}</Link>
            {editingId === comment.id ? (
              <>
                <label htmlFor={`my-comment-${comment.id}`}>Edit comment</label>
                <textarea
                  id={`my-comment-${comment.id}`}
                  maxLength={1000}
                  value={text}
                  onChange={(event) => setText(event.target.value)}
                />
                <button type="button" disabled={busy || !text.trim()} onClick={() => void save(comment.id)}>
                  Save
                </button>
                <button type="button" onClick={() => setEditingId(null)}>
                  Cancel
                </button>
              </>
            ) : (
              <>
                <p>{comment.text}</p>
                <button
                  type="button"
                  onClick={() => {
                    setEditingId(comment.id);
                    setText(comment.text);
                  }}
                >
                  Edit
                </button>
                <button type="button" disabled={busy} onClick={() => void remove(comment.id)}>
                  Delete
                </button>
              </>
            )}
          </li>
        ))}
      </ul>
    </section>
  );
};

export default ProfileCommentsManager;
