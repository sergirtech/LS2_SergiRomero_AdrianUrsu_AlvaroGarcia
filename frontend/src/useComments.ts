import { useEffect, useState } from 'react';
import { addComment, fetchComments, type CommentDto } from './videosApi';

/** Comments of a video from GET /api/videos/{id}/comments, newest first (US-09). add() saves one and shows it. */
export function useComments(videoId: number) {
  const [comments, setComments] = useState<CommentDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const controller = new AbortController();
    setComments([]);
    setLoading(true);
    setError('');
    fetchComments(videoId, controller.signal)
      .then((loaded) => {
        // Keeps a comment that was added while the list was still loading.
        setComments((current) => [
          ...current.filter((added) => !loaded.some((comment) => comment.id === added.id)),
          ...loaded,
        ]);
        setLoading(false);
      })
      .catch((err: unknown) => {
        if (controller.signal.aborted) return;
        setError(err instanceof Error ? err.message : 'Could not load comments.');
        setLoading(false);
      });
    return () => controller.abort();
  }, [videoId]);

  const add = async (text: string) => {
    const created = await addComment(videoId, text);
    setComments((current) => [created, ...current.filter((comment) => comment.id !== created.id)]);
  };

  return { comments, loading, error, add };
}
