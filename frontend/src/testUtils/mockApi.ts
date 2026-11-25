import fetchMock from 'jest-fetch-mock';
import type { CommentDto, VideoDetailDto, VideoPageDto, VideoSummaryDto } from '../videosApi';

interface Reply {
  readonly __reply: true;
  status: number;
  body?: unknown;
}

/** A response with a specific status (and optional JSON body). */
export const reply = (status: number, body?: unknown): Reply => ({ __reply: true, status, body });

const isReply = (value: unknown): value is Reply => typeof value === 'object' && value !== null && '__reply' in value;

/**
 * Answers each fetch by the end of its URL, whatever the call order. Unknown URLs get a 404.
 * A key with a method ("POST /videos/1/comments") wins over the plain URL.
 */
export const mockApi = (routes: Record<string, unknown>) => {
  fetchMock.mockResponse(async (request) => {
    const keys = Object.keys(routes);
    const route =
      keys.find((key) => key.startsWith(`${request.method} `) && request.url.endsWith(key.split(' ')[1])) ??
      keys.find((key) => request.url.endsWith(key));
    if (route === undefined) return { status: 404, body: '' };
    const value = routes[route];
    if (isReply(value)) {
      return { status: value.status, body: value.body === undefined ? '' : JSON.stringify(value.body) };
    }
    return { status: 200, body: JSON.stringify(value) };
  });
};

export const videoSummary = (id: number, title: string): VideoSummaryDto => ({
  id,
  fileKey: `key-${id}`,
  title,
  author: `Channel ${id}`,
  durationSeconds: 60,
  viewCount: 1000,
  publishedAt: null,
});

export const videoDetail = (id: number, title: string): VideoDetailDto => ({
  ...videoSummary(id, title),
  description: 'A description',
  width: 1920,
  height: 1080,
  likeCount: 10,
  ownerId: null,
  tags: [],
  category: null,
});

export const videoComment = (id: number, text: string, authorName = 'viewer'): CommentDto => ({
  id,
  authorId: null,
  authorName,
  text,
  createdAt: '2026-01-01T00:00:00Z',
});

export const videoPage = (items: VideoSummaryDto[], hasNext = false, page = 0): VideoPageDto => ({
  items,
  page,
  size: 12,
  totalItems: items.length + (hasNext ? 1 : 0),
  hasNext,
});
