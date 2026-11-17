import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import fetchMock from 'jest-fetch-mock';
import { useState } from 'react';
import { useMyVideos } from '../useMyVideos';

const anaVideo = (id: number, title: string) => ({
  id,
  title,
  description: `${title} description`,
  ownerId: 1,
  ownerUsername: 'ana',
  createdAt: '2026-11-12T10:00:00Z',
});

const HookHarness = ({ refreshCatalog }: { refreshCatalog: () => Promise<void> }) => {
  const videos = useMyVideos(1, true, refreshCatalog);

  return (
    <>
      <p data-testid="titles">{videos.videos.map((video) => video.title).join(', ')}</p>
      {videos.error && <p role="alert">{videos.error}</p>}
      <button
        type="button"
        onClick={() =>
          void videos.uploadVideo({ title: 'Uploaded', description: 'Upload', file: new File(['mp4'], 'v.mp4') })
        }
      >
        Upload
      </button>
      <button type="button" onClick={() => void videos.editVideo('1', 'Updated', 'Updated description')}>
        Edit
      </button>
      <button type="button" onClick={() => void videos.deleteVideo('1')}>
        Delete
      </button>
    </>
  );
};

describe('useMyVideos', () => {
  beforeEach(() => {
    fetchMock.resetMocks();
  });

  it('loads owned videos and refreshes profile and catalog after upload, edit, and delete', async () => {
    const refreshCatalog = jest.fn().mockResolvedValue(undefined);
    const uploaded = anaVideo(2, 'Uploaded');
    fetchMock.mockResponse(async (request) => {
      if (request.url === 'other/videos' && request.method === 'POST') return JSON.stringify(uploaded);
      if (request.url === 'other/videos/mine' || request.url === 'other/videos') {
        return JSON.stringify({ items: [anaVideo(1, 'First'), uploaded], nextCursor: null });
      }
      if (request.url === 'other/videos/1' && request.method === 'PUT') return JSON.stringify(anaVideo(1, 'Updated'));
      if (request.url === 'other/videos/1' && request.method === 'DELETE') return '';
      return { status: 404, body: '' };
    });

    render(<HookHarness refreshCatalog={refreshCatalog} />);
    expect(await screen.findByText('First, Uploaded')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'Upload' }));
    await waitFor(() => expect(refreshCatalog).toHaveBeenCalledTimes(1));
    const uploadCall = fetchMock.mock.calls.find(([, init]) => init?.method === 'POST');
    expect(uploadCall?.[1]?.body).toBeInstanceOf(FormData);
    expect(uploadCall?.[1]?.headers).toBeUndefined();

    fireEvent.click(screen.getByRole('button', { name: 'Edit' }));
    await waitFor(() => expect(refreshCatalog).toHaveBeenCalledTimes(2));
    fireEvent.click(screen.getByRole('button', { name: 'Delete' }));
    await waitFor(() => expect(refreshCatalog).toHaveBeenCalledTimes(3));
    expect(fetchMock.mock.calls.map(([, init]) => init?.method).filter(Boolean)).toEqual(['POST', 'PUT', 'DELETE']);
  });

  it('reports server upload errors to the caller', async () => {
    fetchMock.mockResponseOnce(JSON.stringify({ items: [], nextCursor: null }));
    fetchMock.mockResponseOnce(JSON.stringify({ message: 'Video is too large.' }), { status: 413 });
    const refreshCatalog = jest.fn().mockResolvedValue(undefined);
    const Harness = () => {
      const [failure, setFailure] = useState('');
      const videos = useMyVideos(1, true, refreshCatalog);
      return (
        <>
          <button
            type="button"
            onClick={() =>
              void videos
                .uploadVideo({ title: 'Large', description: 'Video', file: new File(['x'], 'x.mp4') })
                .catch((error: unknown) => setFailure(error instanceof Error ? error.message : 'Unexpected error'))
            }
          >
            Upload large
          </button>
          {failure && <p role="alert">{failure}</p>}
        </>
      );
    };
    render(<Harness />);

    fireEvent.click(screen.getByRole('button', { name: 'Upload large' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Video is too large.');
    expect(refreshCatalog).not.toHaveBeenCalled();
  });

  it('reloads owned videos after upload even if the initial profile request is still in flight', async () => {
    let finishInitialPage: ((body: string) => void) | undefined;
    let mineRequests = 0;
    const uploaded = anaVideo(2, 'Uploaded');
    const initialPage = new Promise<string>((resolve) => {
      finishInitialPage = resolve;
    });
    fetchMock.mockResponse(async (request) => {
      if (request.url === 'other/videos' && request.method === 'POST') return JSON.stringify(uploaded);
      if (request.url === 'other/videos/mine') {
        mineRequests += 1;
        if (mineRequests === 1) return initialPage;
        return JSON.stringify({ items: [anaVideo(1, 'First'), uploaded], nextCursor: null });
      }
      if (request.url === 'other/videos') return JSON.stringify({ items: [], nextCursor: null });
      return { status: 404, body: '' };
    });
    render(<HookHarness refreshCatalog={jest.fn().mockResolvedValue(undefined)} />);

    fireEvent.click(screen.getByRole('button', { name: 'Upload' }));
    await waitFor(() => expect(finishInitialPage).toBeDefined());
    finishInitialPage?.(JSON.stringify({ items: [anaVideo(1, 'First')], nextCursor: null }));

    expect(await screen.findByText('First, Uploaded')).toBeInTheDocument();
    expect(mineRequests).toBe(2);
  });
});
