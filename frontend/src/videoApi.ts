import type { Video } from './components/VideoCard';
import { getEnv } from './utils/Env';

interface VideoResponse {
  id: number;
  title: string;
  description: string;
  ownerUsername: string;
}

export interface VideoPageResponse {
  items: Video[];
  nextCursor: number | null;
}

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === 'object' && value !== null && !Array.isArray(value);

export const parseVideo = (value: unknown): Video => {
  if (
    !isRecord(value) ||
    typeof value.id !== 'number' ||
    !Number.isSafeInteger(value.id) ||
    value.id < 1 ||
    typeof value.title !== 'string' ||
    typeof value.description !== 'string' ||
    typeof value.ownerId !== 'number' ||
    !Number.isSafeInteger(value.ownerId) ||
    typeof value.ownerUsername !== 'string' ||
    typeof value.createdAt !== 'string'
  ) {
    throw new Error('The video response has an invalid format.');
  }

  const response: VideoResponse = {
    id: value.id,
    title: value.title,
    description: value.description,
    ownerUsername: value.ownerUsername,
  };

  return {
    id: String(response.id),
    title: response.title,
    description: response.description,
    channel: response.ownerUsername,
    thumbnailUrl: null,
  };
};

export const parseVideoPage = (value: unknown): VideoPageResponse => {
  if (!isRecord(value) || !Array.isArray(value.items)) {
    throw new Error('The video page response has an invalid format.');
  }

  let nextCursor: number | null;
  if (value.nextCursor === null) {
    nextCursor = null;
  } else if (typeof value.nextCursor === 'number' && Number.isSafeInteger(value.nextCursor)) {
    nextCursor = value.nextCursor;
  } else {
    throw new Error('The video page response has an invalid cursor.');
  }

  return {
    items: value.items.map(parseVideo),
    nextCursor,
  };
};

export const getVideosUrl = () => `${getEnv().API_BASE_URL}/videos`;

export const getMyVideosUrl = () => `${getVideosUrl()}/mine`;

export const getVideoUrl = (videoId: string) => `${getVideosUrl()}/${encodeURIComponent(videoId)}`;

export const getVideoFileUrl = (videoId: string) => `${getVideoUrl(videoId)}/file`;
