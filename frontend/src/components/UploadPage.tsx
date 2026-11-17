import { useState, type FormEvent } from 'react';
import { Link, Navigate, useNavigate } from 'react-router';
import { useAuth } from '../useAuth';
import { MAX_UPLOAD_BYTES, VideoApiError, uploadVideo } from '../videosApi';
import VideoMetadataFields from './VideoMetadataFields';
import {
  EMPTY_METADATA,
  toMetadataInput,
  validateMetadata,
  type MetadataErrors,
  type MetadataForm,
} from '../videoMetadataForm';

const validateFile = (file: File | null): string | undefined => {
  if (!file) return 'Choose a video file';
  if (!file.name.toLowerCase().endsWith('.mp4')) return 'Only .mp4 files are supported';
  if (file.size === 0) return 'The video file is empty';
  if (file.size > MAX_UPLOAD_BYTES) return 'The video file must be 100 MB or smaller';
  return undefined;
};

/** US-06: an authenticated user uploads an MP4 with its metadata; visitors are sent to log in. */
const UploadPage = () => {
  const { user, status } = useAuth();
  const navigate = useNavigate();
  const [file, setFile] = useState<File | null>(null);
  const [fileError, setFileError] = useState<string | undefined>();
  const [form, setForm] = useState<MetadataForm>(EMPTY_METADATA);
  const [errors, setErrors] = useState<MetadataErrors>({});
  const [formError, setFormError] = useState('');
  const [uploading, setUploading] = useState(false);

  if (status === 'loading') {
    return (
      <p className="pt-empty" role="status">
        Loading...
      </p>
    );
  }
  if (!user) {
    return <Navigate to="/login" replace />;
  }

  const handleChange = (field: keyof MetadataForm, value: string) => {
    setForm((current) => ({ ...current, [field]: value }));
    setErrors((current) => ({ ...current, [field]: undefined }));
  };

  const handleFile = (chosen: File | null) => {
    setFile(chosen);
    setFileError(undefined);
    // Most people name the video after the file: prefill the title if it's still empty.
    if (chosen && !form.title.trim()) {
      handleChange('title', chosen.name.replace(/\.mp4$/i, ''));
    }
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const metadataErrors = validateMetadata(form);
    const chosenFileError = validateFile(file);
    setErrors(metadataErrors);
    setFileError(chosenFileError);
    setFormError('');
    if (chosenFileError || Object.keys(metadataErrors).length > 0 || !file) {
      return;
    }

    setUploading(true);
    try {
      const video = await uploadVideo(file, toMetadataInput(form));
      navigate(`/watch/${video.id}`);
    } catch (error) {
      setFormError(error instanceof VideoApiError ? error.message : 'Could not upload the video. Try again.');
      setUploading(false);
    }
  };

  return (
    <section className="pt-video-form">
      <h2 className="pt-section-title">Upload a video</h2>
      <form onSubmit={handleSubmit} noValidate aria-busy={uploading}>
        {formError && (
          <div className="alert alert-danger" role="alert">
            {formError}
          </div>
        )}
        <div className="mb-3">
          <label htmlFor="upload-file" className="form-label">
            Video file
          </label>
          <input
            id="upload-file"
            type="file"
            accept="video/mp4,.mp4"
            className={`form-control${fileError ? ' is-invalid' : ''}`}
            disabled={uploading}
            onChange={(event) => handleFile(event.target.files?.[0] ?? null)}
            aria-invalid={Boolean(fileError)}
            aria-describedby={fileError ? 'upload-file-error upload-file-hint' : 'upload-file-hint'}
          />
          <div id="upload-file-hint" className="form-text">
            MP4 only, up to 100 MB.
          </div>
          {fileError && (
            <div id="upload-file-error" className="invalid-feedback">
              {fileError}
            </div>
          )}
        </div>
        <VideoMetadataFields
          idPrefix="upload"
          value={form}
          errors={errors}
          disabled={uploading}
          onChange={handleChange}
        />
        <div className="pt-video-form-actions">
          <Link to="/my-videos" className="btn btn-outline-secondary">
            My videos
          </Link>
          <button type="submit" className="btn btn-primary" disabled={uploading}>
            {uploading ? 'Uploading...' : 'Upload'}
          </button>
        </div>
      </form>
    </section>
  );
};

export default UploadPage;
