import { getEnv } from './utils/Env';
import type { Video } from './components/VideoCard';

export interface VideoSummaryDto {
  id: number;
  fileKey: string;
  title: string;
  author: string;
  durationSeconds: number;
  viewCount: number;
  publishedAt: string | null;
}

export interface VideoDetailDto extends VideoSummaryDto {
  description: string | null;
  width: number;
  height: number;
  likeCount: number;
  /** Uploader's user id; null for videos imported from the videoGrabber. */
  ownerId: number | null;
  tags: string[];
  category: string | null;
}

export interface VideoPageDto {
  items: VideoSummaryDto[];
  page: number;
  size: number;
  totalItems: number;
  hasNext: boolean;
}

/** What the owner can set on a video (US-06, US-07). */
export interface VideoMetadataInput {
  title: string;
  description: string;
  tags: string[];
  category: string;
}

export const PAGE_SIZE = 12;

// Same limits as the backend (Video.java, UploadVideoUseCase): checked here only to answer faster.
export const MAX_UPLOAD_BYTES = 100 * 1024 * 1024;
export const MAX_TITLE_LENGTH = 150;
export const MAX_DESCRIPTION_LENGTH = 10_000;
export const MAX_TAGS = 10;
export const VIDEO_CATEGORIES = ['Music', 'Gaming', 'Education', 'Entertainment', 'Sports', 'News', 'Other'];

export class VideoNotFoundError extends Error {}

/** Error answered by the videos API, with the backend's message ({message}) when there is one. */
export class VideoApiError extends Error {
  readonly status: number;

  constructor(status: number, message: string) {
    super(message);
    this.name = 'VideoApiError';
    this.status = status;
  }
}

const videosUrl = () => `${getEnv().API_BASE_URL}/videos`;

export const toVideo = (dto: VideoSummaryDto): Video => ({
  id: String(dto.id),
  fileKey: dto.fileKey,
  title: dto.title,
  channel: dto.author,
  viewCount: dto.viewCount,
});

/** "Rock, live ,, Pop" -> ["Rock", "live", "Pop"]. The backend lowercases and removes duplicates. */
export const parseTags = (text: string): string[] =>
  text
    .split(',')
    .map((tag) => tag.trim())
    .filter((tag) => tag.length > 0);

const failure = async (response: Response, fallback: string): Promise<VideoApiError> => {
  const data = (await response.json().catch(() => ({}))) as { message?: string };
  if (response.status === 401) {
    return new VideoApiError(401, 'Your session has expired. Sign in again.');
  }
  return new VideoApiError(response.status, data.message ?? `${fallback} (HTTP ${response.status}).`);
};

// The session lives in the JSESSIONID cookie, so the calls that need a user send credentials (D17).
const send = async (url: string, init: RequestInit): Promise<Response> => {
  try {
    return await fetch(url, { ...init, credentials: 'include' });
  } catch {
    throw new VideoApiError(0, 'Could not reach the server. Try again later.');
  }
};

export async function fetchVideoPage(page: number, signal?: AbortSignal): Promise<VideoPageDto> {
  const response = await fetch(`${videosUrl()}?page=${page}&size=${PAGE_SIZE}`, { signal });
  if (!response.ok) {
    throw new Error(`Could not load videos (HTTP ${response.status}).`);
  }
  return (await response.json()) as VideoPageDto;
}

export async function fetchVideo(id: string, signal?: AbortSignal): Promise<VideoDetailDto> {
  const response = await fetch(`${videosUrl()}/${encodeURIComponent(id)}`, { signal });
  // 400: the id is not a number (e.g. /watch/abc), so it can't exist either.
  if (response.status === 404 || response.status === 400) {
    throw new VideoNotFoundError(`Video ${id} not found.`);
  }
  if (!response.ok) {
    throw new Error(`Could not load the video (HTTP ${response.status}).`);
  }
  return (await response.json()) as VideoDetailDto;
}

/** Videos the signed-in user uploaded, newest first (US-07). */
export async function fetchMyVideoPage(page: number, signal?: AbortSignal): Promise<VideoPageDto> {
  const response = await send(`${videosUrl()}/mine?page=${page}&size=${PAGE_SIZE}`, { signal });
  if (!response.ok) {
    throw await failure(response, 'Could not load your videos');
  }
  return (await response.json()) as VideoPageDto;
}

/** Multipart upload (US-06). The browser sets the Content-Type with the multipart boundary. */
export async function uploadVideo(file: File, metadata: VideoMetadataInput): Promise<VideoDetailDto> {
  const form = new FormData();
  form.append('file', file);
  form.append('title', metadata.title);
  form.append('description', metadata.description);
  metadata.tags.forEach((tag) => form.append('tags', tag));
  if (metadata.category) form.append('category', metadata.category);

  const response = await send(videosUrl(), { method: 'POST', body: form });
  if (!response.ok) {
    throw await failure(response, 'Could not upload the video');
  }
  return (await response.json()) as VideoDetailDto;
}

export async function updateVideo(id: number, metadata: VideoMetadataInput): Promise<VideoDetailDto> {
  const response = await send(`${videosUrl()}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ ...metadata, category: metadata.category || null }),
  });
  if (!response.ok) {
    throw await failure(response, 'Could not save the changes');
  }
  return (await response.json()) as VideoDetailDto;
}

export async function deleteVideo(id: number): Promise<void> {
  const response = await send(`${videosUrl()}/${id}`, { method: 'DELETE' });
  if (!response.ok) {
    throw await failure(response, 'Could not delete the video');
  }
}
