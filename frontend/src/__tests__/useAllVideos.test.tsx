import { fireEvent, render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import fetchMock from 'jest-fetch-mock';
import { useAllVideos } from '../useAllVideos';

const apiVideo = (id: number, title: string) => ({
  id,
  title,
  description: `${title} description`,
  ownerId: 1,
  ownerUsername: 'creator',
  createdAt: '2026-11-12T10:00:00Z',
});

const CatalogHarness = ({ enabled = true }: { enabled?: boolean }) => {
  const catalog = useAllVideos(enabled);

  return (
    <>
      <p data-testid="titles">{catalog.videos.map((video) => video.title).join(', ')}</p>
      {catalog.loading && <p role="status">Loading videos...</p>}
      {catalog.loadingMore && <p role="status">Loading more videos...</p>}
      {catalog.error && <p role="alert">{catalog.error}</p>}
      <button type="button" disabled={!catalog.hasMore || catalog.loadingMore} onClick={catalog.loadMore}>
        Next page
      </button>
      <button type="button" onClick={catalog.retry}>
        Retry
      </button>
    </>
  );
};

describe('useAllVideos', () => {
  beforeEach(() => {
    fetchMock.resetMocks();
  });

  it('loads the initial page and appends cursor pages without duplicate requests', async () => {
    fetchMock
      .mockResponseOnce(JSON.stringify({ items: [apiVideo(1, 'First'), apiVideo(2, 'Second')], nextCursor: 2 }))
      .mockResponseOnce(JSON.stringify({ items: [apiVideo(2, 'Duplicate'), apiVideo(3, 'Third')], nextCursor: null }));

    render(<CatalogHarness />);

    expect(await screen.findByText('First, Second')).toBeInTheDocument();
    expect(fetchMock.mock.calls[0][0]).toBe('other/videos');
    fireEvent.click(screen.getByRole('button', { name: 'Next page' }));
    fireEvent.click(screen.getByRole('button', { name: 'Next page' }));

    expect(await screen.findByText('First, Second, Third')).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledTimes(2);
    expect(fetchMock.mock.calls[1][0]).toBe('other/videos?cursor=2');
    expect(screen.getByRole('button', { name: 'Next page' })).toBeDisabled();
  });

  it('does not fetch until the catalog is enabled', async () => {
    fetchMock.mockResponseOnce(JSON.stringify({ items: [apiVideo(1, 'First')], nextCursor: null }));
    const { rerender } = render(<CatalogHarness enabled={false} />);

    expect(fetchMock).not.toHaveBeenCalled();
    rerender(<CatalogHarness enabled />);

    expect(await screen.findByText('First')).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledTimes(1);
  });

  it('shows an initial load error and retries the first page', async () => {
    fetchMock.mockRejectOnce(new Error('offline'));
    fetchMock.mockResponseOnce(JSON.stringify({ items: [apiVideo(1, 'Recovered')], nextCursor: null }));
    render(<CatalogHarness />);

    expect(await screen.findByRole('alert')).toHaveTextContent('offline');
    fireEvent.click(screen.getByRole('button', { name: 'Retry' }));

    expect(await screen.findByText('Recovered')).toBeInTheDocument();
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });

  it('keeps loaded videos and retries a failed continuation page', async () => {
    fetchMock
      .mockResponseOnce(JSON.stringify({ items: [apiVideo(1, 'First')], nextCursor: 1 }))
      .mockResponseOnce('', { status: 503 })
      .mockResponseOnce(JSON.stringify({ items: [apiVideo(2, 'Second')], nextCursor: null }));
    render(<CatalogHarness />);

    expect(await screen.findByText('First')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Next page' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('HTTP 503');
    expect(screen.getByTestId('titles')).toHaveTextContent('First');
    fireEvent.click(screen.getByRole('button', { name: 'Retry' }));

    expect(await screen.findByText('First, Second')).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledTimes(3);
    expect(fetchMock.mock.calls[2][0]).toBe('other/videos?cursor=1');
  });
});
