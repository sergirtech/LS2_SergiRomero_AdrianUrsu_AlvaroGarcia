import { useCallback, useEffect, useState } from 'react';

export interface UserList {
  id: string;
  name: string;
  videoIds: string[];
}

const isUserListArray = (value: unknown): value is UserList[] =>
  Array.isArray(value) &&
  value.every(
    (item) =>
      typeof item === 'object' &&
      item !== null &&
      'id' in item &&
      typeof item.id === 'string' &&
      'name' in item &&
      typeof item.name === 'string' &&
      'videoIds' in item &&
      Array.isArray(item.videoIds) &&
      item.videoIds.every((videoId: unknown) => typeof videoId === 'string')
  );

export const useProfileLists = (userId: number | null) => {
  const [lists, setLists] = useState<UserList[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const storageKey = userId === null ? null : `protube:user-lists:${userId}`;

  useEffect(() => {
    setLists([]);
    setError(null);
    if (storageKey === null) {
      setLoading(false);
      return;
    }

    setLoading(true);
    try {
      const saved = window.localStorage.getItem(storageKey);
      if (saved !== null) {
        const parsed: unknown = JSON.parse(saved);
        if (!isUserListArray(parsed)) {
          throw new Error('Saved lists have an invalid format.');
        }
        setLists(parsed);
      }
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : 'Could not load your lists.');
    } finally {
      setLoading(false);
    }
  }, [storageKey]);

  const save = useCallback(
    (updatedLists: UserList[]): boolean => {
      if (storageKey === null) {
        setError('Sign in to manage your lists.');
        return false;
      }

      try {
        window.localStorage.setItem(storageKey, JSON.stringify(updatedLists));
        setLists(updatedLists);
        setError(null);
        return true;
      } catch {
        setError('Could not save your lists. Check your browser storage and try again.');
        return false;
      }
    },
    [storageKey]
  );

  const createList = useCallback(
    (name: string): boolean => {
      const normalizedName = name.trim();
      if (!normalizedName) return false;

      return save([
        ...lists,
        {
          id: `${Date.now().toString(36)}-${Math.random().toString(36).slice(2)}`,
          name: normalizedName,
          videoIds: [],
        },
      ]);
    },
    [lists, save]
  );

  const addVideo = useCallback(
    (listId: string, videoId: string): boolean => {
      const updatedLists = lists.map((list) =>
        list.id === listId && !list.videoIds.includes(videoId)
          ? { ...list, videoIds: [...list.videoIds, videoId] }
          : list
      );
      return save(updatedLists);
    },
    [lists, save]
  );

  const removeVideo = useCallback(
    (listId: string, videoId: string): boolean =>
      save(
        lists.map((list) =>
          list.id === listId ? { ...list, videoIds: list.videoIds.filter((id) => id !== videoId) } : list
        )
      ),
    [lists, save]
  );

  const deleteList = useCallback(
    (listId: string): boolean => save(lists.filter((list) => list.id !== listId)),
    [lists, save]
  );

  return { lists, loading, error, createList, addVideo, removeVideo, deleteList };
};
