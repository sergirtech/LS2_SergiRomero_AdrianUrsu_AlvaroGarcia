import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import fetchMock from 'jest-fetch-mock';
import { useMyComments } from '../useMyComments';

const Harness = () => {
  const comments = useMyComments(1, true);
  return (
    <>
      <p data-testid="titles">{comments.comments.map((comment) => comment.videoTitle).join(',')}</p>
      {comments.error && <p role="alert">{comments.error}</p>}
      <button type="button" onClick={() => void comments.edit(1, 'changed')}>
        Edit
      </button>
      <button type="button" onClick={() => void comments.remove(1)}>
        Delete
      </button>
    </>
  );
};

describe('useMyComments', () => {
  beforeEach(() => fetchMock.resetMocks());

  it('loads comments and refreshes after edits and deletes', async () => {
    fetchMock.mockResponse((request) => {
      if (request.url === 'other/comments/mine') {
        return Promise.resolve(
          JSON.stringify([{ id: 1, videoId: 2, videoTitle: 'Title', text: 'body', createdAt: 'now' }])
        );
      }
      if (request.method === 'DELETE') return Promise.resolve('');
      return Promise.resolve(JSON.stringify({}));
    });
    render(<Harness />);

    expect(await screen.findByText('Title')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Edit' }));
    await waitFor(() =>
      expect(fetchMock.mock.calls.filter(([url]) => String(url).endsWith('/comments/mine'))).toHaveLength(2)
    );
    fireEvent.click(screen.getByRole('button', { name: 'Delete' }));
    await waitFor(() =>
      expect(fetchMock.mock.calls.filter(([url]) => String(url).endsWith('/comments/mine'))).toHaveLength(3)
    );
  });

  it('surfaces errors when the comments endpoint fails', async () => {
    fetchMock.mockResponseOnce('', { status: 503 });
    render(<Harness />);
    expect(await screen.findByRole('alert')).toHaveTextContent('HTTP 503');
  });
});
