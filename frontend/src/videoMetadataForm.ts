import { MAX_DESCRIPTION_LENGTH, MAX_TAGS, MAX_TITLE_LENGTH, parseTags, type VideoMetadataInput } from './videosApi';

/** Form state: tags stay as the text the user types ("rock, live") until submit. */
export interface MetadataForm {
  title: string;
  description: string;
  tags: string;
  category: string;
}

export type MetadataErrors = Partial<Record<keyof MetadataForm, string>>;

export const EMPTY_METADATA: MetadataForm = { title: '', description: '', tags: '', category: '' };

export const validateMetadata = (form: MetadataForm): MetadataErrors => {
  const errors: MetadataErrors = {};
  const title = form.title.trim();
  if (!title) {
    errors.title = 'Title is required';
  } else if (title.length > MAX_TITLE_LENGTH) {
    errors.title = `Title must have at most ${MAX_TITLE_LENGTH} characters`;
  }
  if (form.description.length > MAX_DESCRIPTION_LENGTH) {
    errors.description = `Description must have at most ${MAX_DESCRIPTION_LENGTH} characters`;
  }
  if (new Set(parseTags(form.tags).map((tag) => tag.toLowerCase())).size > MAX_TAGS) {
    errors.tags = `Use at most ${MAX_TAGS} tags`;
  }
  return errors;
};

export const toMetadataInput = (form: MetadataForm): VideoMetadataInput => ({
  title: form.title.trim(),
  description: form.description.trim(),
  tags: parseTags(form.tags),
  category: form.category,
});
