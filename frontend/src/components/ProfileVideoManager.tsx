import { useState, type FormEvent } from 'react';
import { Link } from 'react-router';
import type { User } from '../useAuth';
import type { Video } from './VideoCard';

const MAX_FILE_SIZE = 100 * 1024 * 1024;
const MAX_TITLE_LENGTH = 150;
const MAX_DESCRIPTION_LENGTH = 2_000;

export interface ProfileVideoManagerProps {
  user: User | null;
  videos: Video[];
  loading: boolean;
  loadingMore: boolean;
  hasMore: boolean;
  error: string | null;
  onRetry: () => void;
  onLoadMore: () => void;
  onUpload: (title: string, description: string, file: File) => Promise<void>;
  onEdit: (videoId: string, title: string, description: string) => Promise<void>;
  onDelete: (videoId: string) => Promise<void>;
}

const getErrorMessage = (error: unknown) =>
  error instanceof Error ? error.message : 'The video operation failed. Please try again.';

const ProfileVideoManager = ({
  user,
  videos,
  loading,
  loadingMore,
  hasMore,
  error,
  onRetry,
  onLoadMore,
  onUpload,
  onEdit,
  onDelete,
}: ProfileVideoManagerProps) => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [file, setFile] = useState<File | null>(null);
  const [editingVideoId, setEditingVideoId] = useState<string | null>(null);
  const [editTitle, setEditTitle] = useState('');
  const [editDescription, setEditDescription] = useState('');
  const [confirmDeleteVideoId, setConfirmDeleteVideoId] = useState<string | null>(null);
  const [actionError, setActionError] = useState('');
  const [statusMessage, setStatusMessage] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleUpload = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const form = event.currentTarget;
    setActionError('');
    setStatusMessage('');

    if (!title.trim() || title.trim().length > MAX_TITLE_LENGTH) {
      setActionError(`Title must contain 1 to ${MAX_TITLE_LENGTH} characters.`);
      return;
    }
    if (!description.trim() || description.trim().length > MAX_DESCRIPTION_LENGTH) {
      setActionError(`Description must contain 1 to ${MAX_DESCRIPTION_LENGTH} characters.`);
      return;
    }
    if (!file) {
      setActionError('Choose an MP4 file to upload.');
      return;
    }
    if (!file.name.toLowerCase().endsWith('.mp4')) {
      setActionError('Only .mp4 files are supported.');
      return;
    }
    if (file.size === 0 || file.size > MAX_FILE_SIZE) {
      setActionError('Video file must be greater than 0 bytes and 100 MB or smaller.');
      return;
    }

    setSubmitting(true);
    try {
      await onUpload(title.trim(), description.trim(), file);
      setTitle('');
      setDescription('');
      setFile(null);
      form.reset();
      setStatusMessage('Video uploaded successfully.');
    } catch (uploadError: unknown) {
      setActionError(getErrorMessage(uploadError));
    } finally {
      setSubmitting(false);
    }
  };

  const beginEdit = (video: Video) => {
    setEditingVideoId(video.id);
    setEditTitle(video.title);
    setEditDescription(video.description ?? '');
    setActionError('');
    setStatusMessage('');
    setConfirmDeleteVideoId(null);
  };

  const saveEdit = async (event: FormEvent<HTMLFormElement>, videoId: string) => {
    event.preventDefault();
    setActionError('');
    setStatusMessage('');
    if (!editTitle.trim() || editTitle.trim().length > MAX_TITLE_LENGTH) {
      setActionError(`Title must contain 1 to ${MAX_TITLE_LENGTH} characters.`);
      return;
    }
    if (editDescription.length > MAX_DESCRIPTION_LENGTH) {
      setActionError(`Description must be at most ${MAX_DESCRIPTION_LENGTH} characters.`);
      return;
    }

    setSubmitting(true);
    try {
      await onEdit(videoId, editTitle.trim(), editDescription.trim());
      setEditingVideoId(null);
      setStatusMessage('Video details updated.');
    } catch (editError: unknown) {
      setActionError(getErrorMessage(editError));
    } finally {
      setSubmitting(false);
    }
  };

  const confirmDelete = async (videoId: string) => {
    setActionError('');
    setStatusMessage('');
    setSubmitting(true);
    try {
      await onDelete(videoId);
      setConfirmDeleteVideoId(null);
      setStatusMessage('Video deleted.');
    } catch (deleteError: unknown) {
      setActionError(getErrorMessage(deleteError));
    } finally {
      setSubmitting(false);
    }
  };

  if (!user) {
    return (
      <section className="pt-profile-videos" aria-labelledby="pt-profile-videos-title">
        <h2 id="pt-profile-videos-title">My videos</h2>
        <p>
          <Link to="/login">Sign in</Link> to upload and manage your videos.
        </p>
      </section>
    );
  }

  return (
    <section className="pt-profile-videos" aria-labelledby="pt-profile-videos-title">
      <h2 id="pt-profile-videos-title">My videos</h2>
      <form className="pt-video-upload-form" onSubmit={handleUpload}>
        <h3>Upload a video</h3>
        <div>
          <label htmlFor="profile-video-title">Title</label>
          <input
            id="profile-video-title"
            type="text"
            value={title}
            onChange={(event) => setTitle(event.target.value)}
            maxLength={MAX_TITLE_LENGTH}
            required
          />
        </div>
        <div>
          <label htmlFor="profile-video-description">Description</label>
          <textarea
            id="profile-video-description"
            value={description}
            onChange={(event) => setDescription(event.target.value)}
            maxLength={MAX_DESCRIPTION_LENGTH}
            required
          />
        </div>
        <div>
          <label htmlFor="profile-video-file">MP4 file (100 MB maximum)</label>
          <input
            id="profile-video-file"
            type="file"
            accept=".mp4,video/mp4"
            required
            onChange={(event) => setFile(event.target.files?.[0] ?? null)}
          />
        </div>
        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Uploading...' : 'Upload video'}
        </button>
      </form>

      {error && (
        <div className="pt-video-management-error" role="alert">
          <p>{error}</p>
          <button type="button" className="btn btn-outline-secondary" onClick={onRetry}>
            Retry loading videos
          </button>
        </div>
      )}
      {actionError && (
        <p className="pt-video-management-error" role="alert">
          {actionError}
        </p>
      )}
      {statusMessage && (
        <p className="pt-video-management-status" role="status">
          {statusMessage}
        </p>
      )}

      <h3>Uploaded videos</h3>
      {loading && videos.length === 0 ? (
        <p role="status">Loading your videos...</p>
      ) : !loading && !error && videos.length === 0 ? (
        <p>You haven’t uploaded any videos yet.</p>
      ) : (
        <ul className="pt-owned-video-list">
          {videos.map((video) => (
            <li key={video.id}>
              <article>
                <Link to={`/watch/${encodeURIComponent(video.id)}`}>{video.title}</Link>
                <p>{video.description}</p>
                {editingVideoId === video.id ? (
                  <form onSubmit={(event) => void saveEdit(event, video.id)}>
                    <label htmlFor={`edit-video-title-${video.id}`}>Edit title</label>
                    <input
                      id={`edit-video-title-${video.id}`}
                      value={editTitle}
                      onChange={(event) => setEditTitle(event.target.value)}
                      maxLength={MAX_TITLE_LENGTH}
                      required
                    />
                    <label htmlFor={`edit-video-description-${video.id}`}>Edit description</label>
                    <textarea
                      id={`edit-video-description-${video.id}`}
                      value={editDescription}
                      onChange={(event) => setEditDescription(event.target.value)}
                      maxLength={MAX_DESCRIPTION_LENGTH}
                    />
                    <button type="submit" className="btn btn-primary" disabled={submitting}>
                      Save changes
                    </button>
                    <button type="button" className="btn btn-outline-secondary" onClick={() => setEditingVideoId(null)}>
                      Cancel
                    </button>
                  </form>
                ) : (
                  <div className="pt-owned-video-actions">
                    <button type="button" className="btn btn-outline-secondary" onClick={() => beginEdit(video)}>
                      Edit
                    </button>
                    {confirmDeleteVideoId === video.id ? (
                      <>
                        <span>Delete this video?</span>
                        <button
                          type="button"
                          className="btn btn-danger"
                          disabled={submitting}
                          onClick={() => void confirmDelete(video.id)}
                        >
                          Confirm delete
                        </button>
                        <button
                          type="button"
                          className="btn btn-outline-secondary"
                          onClick={() => setConfirmDeleteVideoId(null)}
                        >
                          Cancel
                        </button>
                      </>
                    ) : (
                      <button
                        type="button"
                        className="btn btn-outline-danger"
                        onClick={() => {
                          setConfirmDeleteVideoId(video.id);
                          setEditingVideoId(null);
                        }}
                      >
                        Delete
                      </button>
                    )}
                  </div>
                )}
              </article>
            </li>
          ))}
        </ul>
      )}
      {hasMore && (
        <button type="button" className="btn btn-outline-secondary" onClick={onLoadMore} disabled={loadingMore}>
          {loadingMore ? 'Loading more videos...' : 'Load more videos'}
        </button>
      )}
    </section>
  );
};

export default ProfileVideoManager;
