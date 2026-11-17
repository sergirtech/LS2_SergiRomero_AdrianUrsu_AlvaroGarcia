import { useCallback } from 'react';
import { getMyVideosUrl, getVideoUrl, getVideosUrl, parseVideo } from './videoApi';
import { useVideoPages } from './useAllVideos';

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === 'object' && value !== null && !Array.isArray(value);

const throwForFailedResponse = async (response: Response) => {
  if (response.ok) return;

  let message = `Request failed (HTTP ${response.status}).`;
  try {
    const body: unknown = await response.json();
    if (isRecord(body) && typeof body.message === 'string' && body.message.trim()) {
      message = body.message;
    }
  } catch {
    // Keep the HTTP status message when the server does not return JSON.
  }
  throw new Error(message);
};

interface VideoUpload {
  title: string;
  description: string;
  file: File;
}

export function useMyVideos(userId: number | null, enabled: boolean, refreshCatalog: () => Promise<void>) {
  const pages = useVideoPages(getMyVideosUrl(), userId !== null && enabled);
  const { refresh } = pages;

  const refreshAfterMutation = useCallback(async () => {
    await Promise.all([refresh(), refreshCatalog()]);
  }, [refresh, refreshCatalog]);

  const uploadVideo = useCallback(
    async ({ title, description, file }: VideoUpload) => {
      const body = new FormData();
      body.append('title', title);
      body.append('description', description);
      body.append('file', file);

      const response = await fetch(getVideosUrl(), {
        method: 'POST',
        credentials: 'include',
        body,
      });
      await throwForFailedResponse(response);
      const uploaded = parseVideo(await response.json());
      await refreshAfterMutation();
      return uploaded;
    },
    [refreshAfterMutation]
  );

  const editVideo = useCallback(
    async (videoId: string, title: string, description: string) => {
      const response = await fetch(getVideoUrl(videoId), {
        method: 'PUT',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title, description }),
      });
      await throwForFailedResponse(response);
      const updated = parseVideo(await response.json());
      await refreshAfterMutation();
      return updated;
    },
    [refreshAfterMutation]
  );

  const deleteVideo = useCallback(
    async (videoId: string) => {
      const response = await fetch(getVideoUrl(videoId), {
        method: 'DELETE',
        credentials: 'include',
      });
      await throwForFailedResponse(response);
      await refreshAfterMutation();
    },
    [refreshAfterMutation]
  );

  return { ...pages, uploadVideo, editVideo, deleteVideo };
}
