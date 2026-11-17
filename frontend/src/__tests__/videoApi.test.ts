import { getVideoFileUrl, getVideoUrl, getVideosUrl, parseVideo, parseVideoPage } from '../videoApi';

const videoResponse = {
  id: 42,
  title: 'A video',
  description: 'Video details',
  ownerId: 7,
  ownerUsername: 'creator',
  createdAt: '2026-11-12T10:00:00Z',
};

describe('video API mapping', () => {
  it('maps backend video metadata to the catalog model', () => {
    expect(parseVideo(videoResponse)).toEqual({
      id: '42',
      title: 'A video',
      description: 'Video details',
      channel: 'creator',
      thumbnailUrl: null,
    });
  });

  it('rejects malformed video metadata', () => {
    expect(() => parseVideo(null)).toThrow('The video response has an invalid format.');
    expect(() => parseVideo({ ...videoResponse, id: 0 })).toThrow('The video response has an invalid format.');
  });

  it('parses pages with and without a continuation cursor', () => {
    expect(parseVideoPage({ items: [videoResponse], nextCursor: 42 })).toEqual({
      items: [parseVideo(videoResponse)],
      nextCursor: 42,
    });
    expect(parseVideoPage({ items: [], nextCursor: null })).toEqual({ items: [], nextCursor: null });
  });

  it('rejects malformed pages and cursors', () => {
    expect(() => parseVideoPage(null)).toThrow('The video page response has an invalid format.');
    expect(() => parseVideoPage({ items: [], nextCursor: '42' })).toThrow(
      'The video page response has an invalid cursor.'
    );
    expect(() => parseVideoPage({ items: [null], nextCursor: null })).toThrow(
      'The video response has an invalid format.'
    );
  });

  it('builds catalog, detail and playback URLs from the configured API base', () => {
    expect(getVideosUrl()).toBe('other/videos');
    expect(getVideoUrl('video id')).toBe('other/videos/video%20id');
    expect(getVideoFileUrl('42')).toBe('other/videos/42/file');
  });
});
