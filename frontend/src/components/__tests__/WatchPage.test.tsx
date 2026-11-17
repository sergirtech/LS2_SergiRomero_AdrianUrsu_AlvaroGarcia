import { act, fireEvent, render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import fetchMock from 'jest-fetch-mock';
import { MemoryRouter, Route, Routes } from 'react-router';

import { AuthProvider } from '../../useAuth';
import WatchPage from '../WatchPage';

const videos = [
  { id: '1', title: 'Featured video', channel: 'Featured channel', description: 'Video details', thumbnailUrl: null },
  {
    id: '2',
    title: 'Recommended video',
    channel: 'Other channel',
    description: 'Recommended details',
    thumbnailUrl: null,
  },
];

const ana = { id: 1, email: 'ana@mail.com', username: 'ana' };
const videoResponse = {
  id: 1,
  title: 'Featured video',
  description: 'Video details',
  ownerId: 1,
  ownerUsername: 'Featured channel',
  createdAt: '2026-11-12T10:00:00Z',
};
const metadata = [
  {
    id: 1,
    videoId: 1,
    videoTitle: 'Featured video',
    authorId: 2,
    authorUsername: 'first-viewer',
    parentId: null,
    text: 'A stored comment',
    createdAt: '2026-11-12T10:00:00Z',
    likeCount: 0,
    likedByCurrentUser: false,
  },
  {
    id: 2,
    videoId: 1,
    videoTitle: 'Featured video',
    authorId: 3,
    authorUsername: 'second-viewer',
    parentId: 1,
    text: 'Another stored comment',
    createdAt: '2026-11-12T10:01:00Z',
    likeCount: 0,
    likedByCurrentUser: false,
  },
];

const response = (body: unknown, status = 200) => ({
  body: typeof body === 'string' ? body : JSON.stringify(body),
  init: { status },
});

const mockApi = ({
  user = null,
  videoStatus = 200,
  commentsStatus = 200,
}: {
  user?: typeof ana | null;
  videoStatus?: number;
  commentsStatus?: number;
} = {}) => {
  let savedComments = metadata;
  fetchMock.mockResponse(async (request) => {
    const url = request.url;
    if (url.endsWith('/auth/me')) return response(user ?? '', user ? 200 : 401);
    if (url.endsWith('/videos/1/comments')) {
      if (request.method === 'POST') {
        savedComments = [
          ...metadata,
          {
            id: 4,
            videoId: 1,
            videoTitle: 'Featured video',
            authorId: 1,
            authorUsername: 'ana',
            parentId: null,
            text: 'My temporary comment',
            createdAt: '2026-11-12T10:02:00Z',
            likeCount: 0,
            likedByCurrentUser: false,
          },
        ];
        return response(savedComments[2], 201);
      }
      return response(commentsStatus === 200 ? savedComments : '', commentsStatus);
    }
    if (url.endsWith('/videos/1')) return response(videoStatus === 200 ? videoResponse : '', videoStatus);
    return response('', 404);
  });
};

const renderWatchPage = async (path = '/watch/1') => {
  render(
    <AuthProvider>
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route path="/watch/:videoId" element={<WatchPage videos={videos} />} />
        </Routes>
      </MemoryRouter>
    </AuthProvider>
  );
  await act(async () => {});
};

describe('WatchPage', () => {
  beforeEach(() => {
    fetchMock.resetMocks();
  });

  it('autoplays the selected video, loads saved comments and shows recommendations', async () => {
    mockApi({ user: ana });
    await renderWatchPage();

    const player = screen.getByLabelText('Featured video');
    expect(player).toHaveAttribute('src', 'other/videos/1/file');
    expect(player).toHaveAttribute('autoplay');
    expect(player).toHaveAttribute('controls');
    expect(screen.getByText('Video details')).toBeInTheDocument();
    expect(await screen.findByText('A stored comment')).toBeInTheDocument();
    expect(screen.getByText('Another stored comment')).toBeInTheDocument();
    expect(screen.getAllByRole('article')).toHaveLength(1);
    expect(screen.getByRole('link', { name: /Recommended video/ })).toHaveAttribute('href', '/watch/2');
  });

  it('lets a signed-in user add a persistent comment', async () => {
    mockApi({ user: ana });
    await renderWatchPage();

    fireEvent.change(screen.getByRole('textbox', { name: 'Add a comment' }), {
      target: { value: '  My temporary comment  ' },
    });
    fireEvent.click(screen.getByRole('button', { name: 'Comment' }));

    expect(await screen.findByText('My temporary comment')).toBeInTheDocument();
    expect(screen.getByText('@ana')).toBeInTheDocument();
    expect(screen.getByRole('textbox', { name: 'Add a comment' })).toHaveValue('');
    expect(fetchMock.mock.calls.some(([, init]) => init?.method === 'POST')).toBe(true);
  });

  it('prompts signed-out users to sign in before commenting', async () => {
    mockApi();
    await renderWatchPage();

    expect(await screen.findByRole('link', { name: 'Sign in to add a comment' })).toHaveAttribute('href', '/login');
    expect(await screen.findByText('A stored comment')).toBeInTheDocument();
  });

  it('shows an error when saved comments cannot be loaded', async () => {
    mockApi({ commentsStatus: 503 });
    await renderWatchPage();

    expect(await screen.findByRole('alert')).toHaveTextContent('Could not load comments (HTTP 503).');
  });

  it('reports an API failure when comments are unavailable', async () => {
    mockApi({ commentsStatus: 404 });
    await renderWatchPage();

    expect(await screen.findByRole('alert')).toHaveTextContent('Could not load comments (HTTP 404).');
  });

  it('shows a not-found message for an unknown video', async () => {
    mockApi({ videoStatus: 404 });
    await renderWatchPage('/watch/unknown');

    expect(screen.getByRole('heading', { name: 'Video not found' })).toBeInTheDocument();
    expect(fetchMock.mock.calls.some(([url]) => String(url).endsWith('/videos/unknown'))).toBe(true);
    expect(fetchMock.mock.calls.some(([url]) => String(url).endsWith('/videos/unknown/comments'))).toBe(false);
  });

  it('shows an API error without rendering a broken video player', async () => {
    mockApi({ videoStatus: 503 });
    await renderWatchPage();

    expect(await screen.findByRole('alert')).toHaveTextContent('Could not load this video (HTTP 503).');
    expect(screen.queryByLabelText('Featured video')).not.toBeInTheDocument();
  });
});
