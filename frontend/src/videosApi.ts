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
}

export interface VideoPageDto {
  items: VideoSummaryDto[];
  page: number;
  size: number;
  totalItems: number;
  hasNext: boolean;
}

export const PAGE_SIZE = 12;

export class VideoNotFoundError extends Error {}

export const toVideo = (dto: VideoSummaryDto): Video => ({
  id: String(dto.id),
  fileKey: dto.fileKey,
  title: dto.title,
  channel: dto.author,
  viewCount: dto.viewCount,
});

export async function fetchVideoPage(page: number, signal?: AbortSignal): Promise<VideoPageDto> {
  const response = await fetch(`${getEnv().API_BASE_URL}/videos?page=${page}&size=${PAGE_SIZE}`, { signal });
  if (!response.ok) {
    throw new Error(`Could not load videos (HTTP ${response.status}).`);
  }
  return (await response.json()) as VideoPageDto;
}

export async function fetchVideo(id: string, signal?: AbortSignal): Promise<VideoDetailDto> {
  const response = await fetch(`${getEnv().API_BASE_URL}/videos/${encodeURIComponent(id)}`, { signal });
  // 400: the id is not a number (e.g. /watch/abc), so it can't exist either.
  if (response.status === 404 || response.status === 400) {
    throw new VideoNotFoundError(`Video ${id} not found.`);
  }
  if (!response.ok) {
    throw new Error(`Could not load the video (HTTP ${response.status}).`);
  }
  return (await response.json()) as VideoDetailDto;
}
