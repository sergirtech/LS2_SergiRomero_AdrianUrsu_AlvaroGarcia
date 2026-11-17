import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { Link, Navigate, useSearchParams } from 'react-router';
import { useAuth } from '../useAuth';
import {
  VideoApiError,
  deleteVideo,
  fetchMyVideoPage,
  fetchVideo,
  updateVideo,
  type VideoSummaryDto,
} from '../videosApi';
import VideoMetadataFields from './VideoMetadataFields';
import { toMetadataInput, validateMetadata, type MetadataErrors, type MetadataForm } from '../videoMetadataForm';

type ListStatus = 'loading' | 'ready' | 'error';

interface Editor {
  id: number;
  form: MetadataForm | null;
  errors: MetadataErrors;
  error: string;
  saving: boolean;
}

const messageOf = (error: unknown, fallback: string) => (error instanceof Error ? error.message : fallback);

/** US-07: the signed-in user's uploads, with metadata editing and deletion. Visitors are sent to log in. */
const MyVideosPage = () => {
  const { user, status: authStatus } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  const [videos, setVideos] = useState<VideoSummaryDto[]>([]);
  const [page, setPage] = useState(0);
  const [attempt, setAttempt] = useState(0);
  const [hasNext, setHasNext] = useState(false);
  const [listStatus, setListStatus] = useState<ListStatus>('loading');
  const [listError, setListError] = useState('');
  const [editor, setEditor] = useState<Editor | null>(null);
  const [confirmingDelete, setConfirmingDelete] = useState<number | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [notice, setNotice] = useState('');
  const userId = user?.id;

  useEffect(() => {
    if (userId === undefined) return;
    const controller = new AbortController();
    setListStatus('loading');
    fetchMyVideoPage(page, controller.signal)
      .then((result) => {
        setVideos((current) => (page === 0 ? result.items : [...current, ...result.items]));
        setHasNext(result.hasNext);
        setListStatus('ready');
      })
      .catch((error: unknown) => {
        if (controller.signal.aborted) return;
        setListError(messageOf(error, 'Could not load your videos.'));
        setListStatus('error');
      });
    return () => controller.abort();
  }, [userId, page, attempt]);

  const openEditor = useCallback((id: number) => {
    setNotice('');
    setConfirmingDelete(null);
    setEditor({ id, form: null, errors: {}, error: '', saving: false });
    // The list only has summaries: description, tags and category come from the detail.
    fetchVideo(String(id))
      .then((video) =>
        setEditor((current) =>
          current?.id === id
            ? {
                ...current,
                form: {
                  title: video.title,
                  description: video.description ?? '',
                  tags: video.tags.join(', '),
                  category: video.category ?? '',
                },
              }
            : current
        )
      )
      .catch((error: unknown) =>
        setEditor((current) =>
          current?.id === id ? { ...current, error: messageOf(error, 'Could not load the video.') } : current
        )
      );
  }, []);

  // /my-videos?edit=5 (the "Edit video" link on the watch page) opens that video's editor.
  const editParam = searchParams.get('edit');
  useEffect(() => {
    if (userId === undefined || !editParam) return;
    const id = Number(editParam);
    if (Number.isInteger(id) && id > 0) openEditor(id);
    setSearchParams({}, { replace: true });
  }, [userId, editParam, openEditor, setSearchParams]);

  if (authStatus === 'loading') {
    return (
      <p className="pt-empty" role="status">
        Loading...
      </p>
    );
  }
  if (!user) {
    return <Navigate to="/login" replace />;
  }

  const changeField = (field: keyof MetadataForm, value: string) =>
    setEditor((current) =>
      current?.form
        ? { ...current, form: { ...current.form, [field]: value }, errors: { ...current.errors, [field]: undefined } }
        : current
    );

  const handleSave = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!editor?.form) return;
    const errors = validateMetadata(editor.form);
    if (Object.keys(errors).length > 0) {
      setEditor({ ...editor, errors });
      return;
    }

    setEditor({ ...editor, saving: true, error: '' });
    try {
      const saved = await updateVideo(editor.id, toMetadataInput(editor.form));
      setVideos((current) =>
        current.map((video) => (video.id === saved.id ? { ...video, title: saved.title } : video))
      );
      setEditor(null);
      setNotice(`Changes to "${saved.title}" saved.`);
    } catch (error) {
      setEditor({ ...editor, saving: false, error: messageOf(error, 'Could not save the changes.') });
    }
  };

  const handleDelete = async (video: VideoSummaryDto) => {
    setDeleting(true);
    setNotice('');
    try {
      await deleteVideo(video.id);
      setVideos((current) => current.filter((item) => item.id !== video.id));
      setNotice(`"${video.title}" was deleted.`);
    } catch (error) {
      setNotice(error instanceof VideoApiError ? error.message : 'Could not delete the video.');
    } finally {
      setDeleting(false);
      setConfirmingDelete(null);
    }
  };

  const renderEditor = (current: Editor) => {
    if (!current.form) {
      return current.error ? (
        <p className="pt-comment-error" role="alert">
          {current.error}
        </p>
      ) : (
        <p role="status">Loading video details...</p>
      );
    }
    return (
      <form className="pt-my-video-editor" onSubmit={handleSave} noValidate aria-label="Edit video">
        {current.error && (
          <div className="alert alert-danger" role="alert">
            {current.error}
          </div>
        )}
        <VideoMetadataFields
          idPrefix={`edit-${current.id}`}
          value={current.form}
          errors={current.errors}
          disabled={current.saving}
          onChange={changeField}
        />
        <div className="pt-video-form-actions">
          <button type="button" className="btn btn-outline-secondary" onClick={() => setEditor(null)}>
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={current.saving}>
            {current.saving ? 'Saving...' : 'Save changes'}
          </button>
        </div>
      </form>
    );
  };

  return (
    <section className="pt-my-videos">
      <div className="pt-my-videos-header">
        <h2 className="pt-section-title">My videos</h2>
        <Link to="/upload" className="btn btn-primary">
          Upload a video
        </Link>
      </div>
      {notice && (
        <p className="alert alert-info" role="status">
          {notice}
        </p>
      )}
      {editor && !videos.some((video) => video.id === editor.id) && renderEditor(editor)}

      {listStatus === 'ready' && videos.length === 0 && (
        <p className="pt-empty">
          You haven't uploaded any videos yet. <Link to="/upload">Upload your first one</Link>.
        </p>
      )}
      <ul className="pt-my-video-list">
        {videos.map((video) => (
          <li key={video.id} className="pt-my-video">
            <div className="pt-my-video-row">
              <Link to={`/watch/${video.id}`} className="pt-my-video-title">
                {video.title}
              </Link>
              {confirmingDelete === video.id ? (
                <div className="pt-my-video-actions" role="group" aria-label={`Confirm deleting ${video.title}`}>
                  <span>Delete it for good?</span>
                  <button
                    type="button"
                    className="btn btn-danger btn-sm"
                    disabled={deleting}
                    onClick={() => void handleDelete(video)}
                  >
                    {deleting ? 'Deleting...' : 'Yes, delete'}
                  </button>
                  <button
                    type="button"
                    className="btn btn-outline-secondary btn-sm"
                    disabled={deleting}
                    onClick={() => setConfirmingDelete(null)}
                  >
                    Keep it
                  </button>
                </div>
              ) : (
                <div className="pt-my-video-actions">
                  <button
                    type="button"
                    className="btn btn-outline-secondary btn-sm"
                    onClick={() => openEditor(video.id)}
                    aria-label={`Edit ${video.title}`}
                  >
                    Edit
                  </button>
                  <button
                    type="button"
                    className="btn btn-outline-danger btn-sm"
                    onClick={() => {
                      setEditor(null);
                      setConfirmingDelete(video.id);
                    }}
                    aria-label={`Delete ${video.title}`}
                  >
                    Delete
                  </button>
                </div>
              )}
            </div>
            {editor?.id === video.id && renderEditor(editor)}
          </li>
        ))}
      </ul>

      {listStatus === 'loading' && (
        <p className="pt-empty" role="status">
          Loading your videos...
        </p>
      )}
      {listStatus === 'error' && (
        <div className="pt-error" role="alert">
          <p>{listError}</p>
          <button type="button" className="btn btn-outline-secondary" onClick={() => setAttempt((n) => n + 1)}>
            Try again
          </button>
        </div>
      )}
      {hasNext && listStatus === 'ready' && (
        <div className="pt-load-more">
          <button type="button" className="btn btn-outline-secondary" onClick={() => setPage((n) => n + 1)}>
            Load more
          </button>
        </div>
      )}
    </section>
  );
};

export default MyVideosPage;
