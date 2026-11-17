import { useCallback, useEffect, useState } from 'react';
import { getEnv } from './utils/Env';

export interface UserList {
  id: string;
  name: string;
  videoIds: string[];
}

const parsePlaylists = (value: unknown): UserList[] => {
  if (!Array.isArray(value)) throw new Error('Lists response has an invalid format.');
  return value.map((playlist) => {
    if (
      typeof playlist !== 'object' ||
      playlist === null ||
      typeof playlist.id !== 'number' ||
      typeof playlist.name !== 'string' ||
      !Array.isArray(playlist.videoIds) ||
      !playlist.videoIds.every((id: unknown) => typeof id === 'number')
    ) {
      throw new Error('A list response has an invalid format.');
    }
    return {
      id: String(playlist.id),
      name: playlist.name,
      videoIds: playlist.videoIds.map(String),
    };
  });
};

const apiUrl = () => `${getEnv().API_BASE_URL}/playlists`;

export const useProfileLists = (userId: number | null) => {
  const [lists, setLists] = useState<UserList[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    if (userId === null) {
      setLists([]);
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(apiUrl(), { credentials: 'include' });
      if (!response.ok) throw new Error(`Could not load your lists (HTTP ${response.status}).`);
      setLists(parsePlaylists(await response.json()));
    } catch (loadError: unknown) {
      setError(loadError instanceof Error ? loadError.message : 'Could not load your lists.');
    } finally {
      setLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    setLists([]);
    setError(null);
    void refresh();
  }, [refresh]);

  const mutate = useCallback(
    async (url: string, method: string, body?: unknown): Promise<boolean> => {
      if (userId === null) {
        setError('Sign in to manage your lists.');
        return false;
      }
      setError(null);
      try {
        const response = await fetch(url, {
          method,
          credentials: 'include',
          ...(body === undefined
            ? {}
            : { headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) }),
        });
        if (!response.ok) throw new Error(`Could not save your lists (HTTP ${response.status}).`);
        if (method === 'DELETE' && !url.includes('/videos/')) {
          setLists((current) => current.filter((list) => url !== `${apiUrl()}/${list.id}`));
        } else {
          const updated = parsePlaylists([await response.json()])[0];
          setLists((current) => {
            const existing = current.some((list) => list.id === updated.id);
            return existing ? current.map((list) => (list.id === updated.id ? updated : list)) : [updated, ...current];
          });
        }
        return true;
      } catch (saveError: unknown) {
        setError(saveError instanceof Error ? saveError.message : 'Could not save your lists.');
        return false;
      }
    },
    [userId]
  );

  const createList = useCallback((name: string) => mutate(apiUrl(), 'POST', { name }), [mutate]);
  const renameList = useCallback(
    (listId: string, name: string) => mutate(`${apiUrl()}/${listId}`, 'PUT', { name }),
    [mutate]
  );
  const addVideo = useCallback(
    (listId: string, videoId: string) => mutate(`${apiUrl()}/${listId}/videos/${videoId}`, 'POST'),
    [mutate]
  );
  const removeVideo = useCallback(
    (listId: string, videoId: string) => mutate(`${apiUrl()}/${listId}/videos/${videoId}`, 'DELETE'),
    [mutate]
  );
  const deleteList = useCallback((listId: string) => mutate(`${apiUrl()}/${listId}`, 'DELETE'), [mutate]);

  return { lists, loading, error, refresh, createList, renameList, addVideo, removeVideo, deleteList };
};
