import { useCallback, useEffect, useState } from 'react';
import { getEnv } from './utils/Env';

export interface MyComment {
  id: number;
  videoId: number;
  videoTitle: string;
  text: string;
  createdAt: string;
}

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === 'object' && value !== null && !Array.isArray(value);

const parseComments = (value: unknown): MyComment[] => {
  if (!Array.isArray(value)) throw new Error('Comments response has an invalid format.');
  return value.map((comment) => {
    if (
      !isRecord(comment) ||
      typeof comment.id !== 'number' ||
      typeof comment.videoId !== 'number' ||
      typeof comment.text !== 'string' ||
      typeof comment.createdAt !== 'string'
    ) {
      throw new Error('A comment response has an invalid format.');
    }
    return {
      id: comment.id,
      videoId: comment.videoId,
      videoTitle: typeof comment.videoTitle === 'string' ? comment.videoTitle : `Video ${comment.videoId}`,
      text: comment.text,
      createdAt: comment.createdAt,
    };
  });
};

const commentsUrl = () => `${getEnv().API_BASE_URL}/comments/mine`;

export function useMyComments(userId: number | null, enabled: boolean) {
  const [comments, setComments] = useState<MyComment[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    if (userId === null || !enabled) return;
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(commentsUrl(), { credentials: 'include' });
      if (!response.ok) throw new Error(`Could not load your comments (HTTP ${response.status}).`);
      setComments(parseComments(await response.json()));
    } catch (loadError: unknown) {
      setError(loadError instanceof Error ? loadError.message : 'Could not load your comments.');
    } finally {
      setLoading(false);
    }
  }, [enabled, userId]);

  useEffect(() => {
    setComments([]);
    setError(null);
    void refresh();
  }, [refresh]);

  const edit = useCallback(
    async (commentId: number, text: string) => {
      const response = await fetch(`${getEnv().API_BASE_URL}/comments/${commentId}`, {
        method: 'PUT',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ text }),
      });
      if (!response.ok) throw new Error(`Could not update comment (HTTP ${response.status}).`);
      await refresh();
    },
    [refresh]
  );

  const remove = useCallback(
    async (commentId: number) => {
      const response = await fetch(`${getEnv().API_BASE_URL}/comments/${commentId}`, {
        method: 'DELETE',
        credentials: 'include',
      });
      if (!response.ok) throw new Error(`Could not delete comment (HTTP ${response.status}).`);
      await refresh();
    },
    [refresh]
  );

  return { comments, loading, error, refresh, edit, remove };
}
