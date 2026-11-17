import { MAX_DESCRIPTION_LENGTH, MAX_TAGS, MAX_TITLE_LENGTH, VIDEO_CATEGORIES } from '../videosApi';
import type { MetadataErrors, MetadataForm } from '../videoMetadataForm';

interface Props {
  idPrefix: string;
  value: MetadataForm;
  errors: MetadataErrors;
  disabled?: boolean;
  onChange: (field: keyof MetadataForm, value: string) => void;
}

/** Title, description, tags and category inputs shared by the upload and edit forms. */
const VideoMetadataFields = ({ idPrefix, value, errors, disabled = false, onChange }: Props) => {
  const describedBy = (field: keyof MetadataForm, hint?: string) =>
    [errors[field] ? `${idPrefix}-${field}-error` : '', hint ?? ''].filter(Boolean).join(' ') || undefined;
  const error = (field: keyof MetadataForm) =>
    errors[field] && (
      <div id={`${idPrefix}-${field}-error`} className="invalid-feedback">
        {errors[field]}
      </div>
    );
  const invalid = (field: keyof MetadataForm) => (errors[field] ? ' is-invalid' : '');

  return (
    <>
      <div className="mb-3">
        <label htmlFor={`${idPrefix}-title`} className="form-label">
          Title
        </label>
        <input
          id={`${idPrefix}-title`}
          className={`form-control${invalid('title')}`}
          value={value.title}
          maxLength={MAX_TITLE_LENGTH}
          disabled={disabled}
          onChange={(event) => onChange('title', event.target.value)}
          aria-invalid={Boolean(errors.title)}
          aria-describedby={describedBy('title')}
        />
        {error('title')}
      </div>
      <div className="mb-3">
        <label htmlFor={`${idPrefix}-description`} className="form-label">
          Description
        </label>
        <textarea
          id={`${idPrefix}-description`}
          className={`form-control${invalid('description')}`}
          rows={4}
          value={value.description}
          maxLength={MAX_DESCRIPTION_LENGTH}
          disabled={disabled}
          onChange={(event) => onChange('description', event.target.value)}
          aria-invalid={Boolean(errors.description)}
          aria-describedby={describedBy('description')}
        />
        {error('description')}
      </div>
      <div className="mb-3">
        <label htmlFor={`${idPrefix}-tags`} className="form-label">
          Tags
        </label>
        <input
          id={`${idPrefix}-tags`}
          className={`form-control${invalid('tags')}`}
          value={value.tags}
          placeholder="music, live, guitar"
          disabled={disabled}
          onChange={(event) => onChange('tags', event.target.value)}
          aria-invalid={Boolean(errors.tags)}
          aria-describedby={describedBy('tags', `${idPrefix}-tags-hint`)}
        />
        <div id={`${idPrefix}-tags-hint`} className="form-text">
          Separate tags with commas (up to {MAX_TAGS}).
        </div>
        {error('tags')}
      </div>
      <div className="mb-3">
        <label htmlFor={`${idPrefix}-category`} className="form-label">
          Category
        </label>
        <select
          id={`${idPrefix}-category`}
          className="form-select"
          value={value.category}
          disabled={disabled}
          onChange={(event) => onChange('category', event.target.value)}
        >
          <option value="">No category</option>
          {VIDEO_CATEGORIES.map((category) => (
            <option key={category} value={category}>
              {category}
            </option>
          ))}
          {/* Keeps a category the list doesn't have (e.g. set through the API). */}
          {value.category && !VIDEO_CATEGORIES.includes(value.category) && (
            <option value={value.category}>{value.category}</option>
          )}
        </select>
      </div>
    </>
  );
};

export default VideoMetadataFields;
