import { useCallback, useEffect, useState } from 'react';
import { getEnv } from './utils/Env';

const MAX_FILE_SIZE = 5 * 1024 * 1024;
const PHOTO_URL = `${getEnv().API_BASE_URL}/users/me/photo`;

const validatePhoto = (file: File): string | null => {
  const extension = file.name.split('.').pop()?.toLowerCase();
  const validTypes: Record<string, string> = {
    png: 'image/png',
    jpg: 'image/jpeg',
    jpeg: 'image/jpeg',
    svg: 'image/svg+xml',
  };
  if (!extension || validTypes[extension] !== file.type) {
    return 'Choose a PNG, JPG, JPEG, or SVG image.';
  }
  if (file.size === 0 || file.size > MAX_FILE_SIZE) return 'The profile photo must be between 1 byte and 5 MB.';
  return null;
};

export function useProfilePhoto(userId: number | null, enabled: boolean) {
  const [photo, setPhoto] = useState<Blob | null>(null);
  const [photoUrl, setPhotoUrl] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    if (userId === null || !enabled) return;
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(PHOTO_URL, { credentials: 'include' });
      if (response.status === 404) {
        setPhoto(null);
        return;
      }
      if (!response.ok) throw new Error(`Could not load your profile photo (HTTP ${response.status}).`);
      setPhoto(await response.blob());
    } catch (loadError: unknown) {
      setError(loadError instanceof Error ? loadError.message : 'Could not load your profile photo.');
    } finally {
      setLoading(false);
    }
  }, [enabled, userId]);

  useEffect(() => {
    setPhoto(null);
    setPhotoUrl(null);
    setError(null);
    void refresh();
  }, [refresh]);

  useEffect(() => {
    if (!photo) {
      setPhotoUrl(null);
      return;
    }
    const url = URL.createObjectURL(photo);
    setPhotoUrl(url);
    return () => URL.revokeObjectURL(url);
  }, [photo]);

  const upload = useCallback(
    async (file: File): Promise<boolean> => {
      if (userId === null) {
        setError('Sign in to update your profile photo.');
        return false;
      }
      const validationError = validatePhoto(file);
      if (validationError) {
        setError(validationError);
        return false;
      }
      setSaving(true);
      setError(null);
      try {
        const formData = new FormData();
        formData.append('file', file);
        const response = await fetch(PHOTO_URL, {
          method: 'PUT',
          credentials: 'include',
          body: formData,
        });
        if (!response.ok) throw new Error(`Could not update your profile photo (HTTP ${response.status}).`);
        await refresh();
        return true;
      } catch (saveError: unknown) {
        setError(saveError instanceof Error ? saveError.message : 'Could not update your profile photo.');
        return false;
      } finally {
        setSaving(false);
      }
    },
    [refresh, userId]
  );

  const remove = useCallback(async (): Promise<boolean> => {
    if (userId === null) {
      setError('Sign in to update your profile photo.');
      return false;
    }
    setSaving(true);
    setError(null);
    try {
      const response = await fetch(PHOTO_URL, { method: 'DELETE', credentials: 'include' });
      if (!response.ok) throw new Error(`Could not delete your profile photo (HTTP ${response.status}).`);
      setPhoto(null);
      return true;
    } catch (saveError: unknown) {
      setError(saveError instanceof Error ? saveError.message : 'Could not delete your profile photo.');
      return false;
    } finally {
      setSaving(false);
    }
  }, [userId]);

  return { photoUrl, loading, saving, error, upload, remove, refresh };
}
