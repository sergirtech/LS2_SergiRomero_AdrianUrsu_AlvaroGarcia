import { act, fireEvent, render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import fetchMock from 'jest-fetch-mock';
import { MemoryRouter } from 'react-router';

import App from '../App';
import { AuthProvider } from '../useAuth';
import { publishProfileStatsChange } from '../profileStats';

// Jest has no CSS transform (frontend/AGENTS.md), so App's stylesheet is replaced by an empty module.
jest.mock('../App.css', () => ({}));

const ana = { id: 1, email: 'ana@mail.com', username: 'ana' };
const sampleVideos = [
  {
    id: 1,
    title: 'Mark Ronson - Uptown Funk (Official Video) ft. Bruno Mars',
    description: 'Uptown Funk',
    ownerId: 1,
    ownerUsername: 'Mark Ronson',
    createdAt: '2026-11-12T10:00:00Z',
  },
  {
    id: 2,
    title: "Meat Loaf - I'd Do Anything For Love (But I Won't Do That) (Official Music Video)",
    description: 'Meat Loaf video',
    ownerId: 1,
    ownerUsername: 'Meat Loaf',
    createdAt: '2026-11-12T10:00:00Z',
  },
  {
    id: 3,
    title: 'Taylor Swift - Shake It Off',
    description: 'Official music video',
    ownerId: 1,
    ownerUsername: 'Taylor Swift',
    createdAt: '2026-11-12T10:00:00Z',
  },
  {
    id: 4,
    title: 'Ed Sheeran - BLOW (with Chris Stapleton & Bruno Mars) [Official Music Video]',
    description: 'BLOW',
    ownerId: 1,
    ownerUsername: 'Ed Sheeran',
    createdAt: '2026-11-12T10:00:00Z',
  },
  {
    id: 5,
    title: 'Danny Ocean - Me Rehúso (Official Audio)',
    description: 'Me Rehúso',
    ownerId: 1,
    ownerUsername: 'Danny Ocean',
    createdAt: '2026-11-12T10:00:00Z',
  },
];
const storedComment = [
  {
    id: 8,
    videoId: 3,
    videoTitle: 'Taylor Swift - Shake It Off',
    authorId: 2,
    authorUsername: 'viewer',
    parentId: null,
    text: 'Stored comment',
    createdAt: '2026-11-12T10:00:00Z',
    likeCount: 0,
    likedByCurrentUser: false,
  },
];

const response = (body: unknown, status = 200) => ({
  body: typeof body === 'string' ? body : JSON.stringify(body),
  init: { status },
});

const mockApi = (
  options: {
    user?: typeof ana | null;
    logoutOffline?: boolean;
    playlists?: { id: number; name: string; videoIds: number[] }[];
  } = {}
) => {
  const { user = null, logoutOffline = false, playlists: storedPlaylists = [] } = options;
  const playlists = [...storedPlaylists];
  let nextPlaylistId = playlists.reduce((largest, playlist) => Math.max(largest, playlist.id), 0) + 1;
  fetchMock.mockResponse(async (request) => {
    const url = request.url;
    const method = request.method;
    if (url.endsWith('/auth/me')) return response(user ?? '', user ? 200 : 401);
    if (url.endsWith('/auth/login')) return response(ana);
    if (url.endsWith('/auth/register')) return response(ana, 201);
    if (url.endsWith('/auth/logout')) {
      if (logoutOffline) throw new Error('offline');
      return response('', 204);
    }
    if (url.endsWith('/users/me/photo')) return response('', 404);
    if (url.endsWith('/playlists')) {
      if (method === 'GET') return response(playlists);
      if (method === 'POST') {
        const requestBody = (await request.json()) as { name: string };
        const playlist = { id: nextPlaylistId++, ...requestBody, videoIds: [] };
        playlists.unshift(playlist);
        return response(playlist, 201);
      }
    }
    const playlistMatch = url.match(/\/playlists\/(\d+)(?:\/videos\/(\d+))?$/);
    if (playlistMatch) {
      const playlist = playlists.find((item) => item.id === Number(playlistMatch[1]));
      if (!playlist) return response('', 404);
      if (method === 'PUT') {
        playlist.name = ((await request.json()) as { name: string }).name;
        return response(playlist);
      }
      if (method === 'DELETE' && playlistMatch[2] === undefined) {
        playlists.splice(playlists.indexOf(playlist), 1);
        return response('', 204);
      }
      if (playlistMatch[2] !== undefined) {
        const videoId = Number(playlistMatch[2]);
        if (method === 'POST' && !playlist.videoIds.includes(videoId)) playlist.videoIds.push(videoId);
        if (method === 'DELETE') playlist.videoIds = playlist.videoIds.filter((id) => id !== videoId);
        return response(playlist);
      }
    }
    if (url.endsWith('/videos/mine')) return response({ items: sampleVideos, nextCursor: null });
    if (/\/videos\/\d+\/comments$/.test(url)) return response(storedComment);
    if (url.endsWith('/videos')) return response({ items: sampleVideos, nextCursor: null });
    if (url.includes('/videos?cursor=')) return response({ items: [], nextCursor: null });

    const videoMatch = url.match(/\/videos\/(\d+)$/);
    if (videoMatch) {
      const video = sampleVideos.find((item) => item.id === Number(videoMatch[1]));
      return video ? response(video) : response('', 404);
    }
    return response('', 404);
  });
};

// Renders and lets AuthProvider finish its initial GET /me, so no state update happens after the test.
const renderApp = async (path: string) => {
  render(
    <AuthProvider>
      <MemoryRouter initialEntries={[path]}>
        <App />
      </MemoryRouter>
    </AuthProvider>
  );
  await act(async () => {
    await new Promise((resolve) => setTimeout(resolve, 0));
  });
};

describe('App authentication', () => {
  beforeEach(() => {
    fetchMock.resetMocks();
    window.localStorage.clear();
  });

  it('shows the user in the Navbar after logging in (L1)', async () => {
    mockApi();
    await renderApp('/login');

    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'ana@mail.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'secret123' } });
    fireEvent.click(screen.getByRole('button', { name: 'Sign in' }));

    expect(await screen.findByRole('button', { name: 'User access' })).toBeInTheDocument();
    await screen.findAllByRole('article');
    fireEvent.click(screen.getByRole('button', { name: 'User access' }));
    expect(screen.getByRole('button', { name: 'Sign out' })).toBeInTheDocument();
    expect(screen.getByText('ana')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Recommended' })).toBeInTheDocument();
  });

  it('shows the new user in the Navbar after registering (R1)', async () => {
    mockApi();
    await renderApp('/register');

    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'ana@mail.com' } });
    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'ana' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'secret123' } });
    fireEvent.click(screen.getByRole('button', { name: 'Create account' }));

    expect(await screen.findByRole('button', { name: 'User access' })).toBeInTheDocument();
    await screen.findAllByRole('article');
    fireEvent.click(screen.getByRole('button', { name: 'User access' }));
    expect(screen.getByRole('button', { name: 'Sign out' })).toBeInTheDocument();
    expect(screen.getByText('ana')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Recommended' })).toBeInTheDocument();
  });

  it('signs out and shows Sign in again (L3)', async () => {
    mockApi({ user: ana });
    await renderApp('/');

    fireEvent.click(screen.getByRole('button', { name: 'User access' }));
    fireEvent.click(screen.getByRole('button', { name: 'Sign out' }));

    expect(await screen.findByRole('link', { name: 'Sign in' })).toBeInTheDocument();
    expect(screen.queryByText('ana')).not.toBeInTheDocument();
    expect(fetchMock.mock.calls.some(([url]) => String(url).endsWith('/auth/logout'))).toBe(true);
  });

  it('stays signed in when the server cannot be reached on sign out', async () => {
    mockApi({ user: ana, logoutOffline: true });
    await renderApp('/');

    fireEvent.click(screen.getByRole('button', { name: 'User access' }));
    fireEvent.click(screen.getByRole('button', { name: 'Sign out' }));
    await act(async () => {});

    expect(screen.getByRole('button', { name: 'Sign out' })).toBeInTheDocument();
  });

  it('shows the current user profile and updates its statistics', async () => {
    mockApi({ user: ana });
    await renderApp('/profile');

    expect(screen.getByRole('heading', { name: 'ana' })).toBeInTheDocument();
    expect(screen.getByText('ana@mail.com')).toBeInTheDocument();
    expect(await screen.findByRole('heading', { name: 'Uploaded videos' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: sampleVideos[0].title })).toHaveAttribute('href', '/watch/1');
    expect(screen.getByLabelText('Channel statistics')).toHaveTextContent('0');

    act(() => {
      publishProfileStatsChange({ followers: 1 });
    });

    expect(screen.getByLabelText('Channel statistics')).toHaveTextContent('0');
    expect(screen.getByLabelText('Channel statistics')).toHaveTextContent('1');

    act(() => publishProfileStatsChange({ followers: 1 }));

    expect(screen.getByLabelText('Channel statistics')).toHaveTextContent('2');
  });

  it('creates a persisted list and adds a catalog song to it', async () => {
    mockApi({ user: ana });
    await renderApp('/profile/lists');

    fireEvent.change(screen.getByLabelText('New list name'), { target: { value: 'Road trip' } });
    fireEvent.click(screen.getByRole('button', { name: 'Create list' }));

    expect(await screen.findByRole('heading', { name: 'Road trip' })).toBeInTheDocument();
    expect(screen.getByLabelText('Channel statistics')).toHaveTextContent('1');
    await screen.findByRole('option', { name: /Mark Ronson/ });

    fireEvent.change(screen.getByLabelText('Add a song to Road trip'), { target: { value: '1' } });
    fireEvent.click(screen.getByRole('button', { name: 'Add song' }));

    expect(
      await screen.findByRole('link', { name: 'Mark Ronson - Uptown Funk (Official Video) ft. Bruno Mars' })
    ).toHaveAttribute('href', '/watch/1');
    expect(fetchMock.mock.calls.some(([url]) => String(url).endsWith('/playlists/1/videos/1'))).toBe(true);
  });

  it('loads signed-in user lists from the API', async () => {
    mockApi({ user: ana, playlists: [{ id: 1, name: 'Road trip', videoIds: [1] }] });

    await renderApp('/profile/lists');

    expect(await screen.findByRole('heading', { name: 'Road trip' })).toBeInTheDocument();
    expect(
      await screen.findByRole('link', { name: 'Mark Ronson - Uptown Funk (Official Video) ft. Bruno Mars' })
    ).toHaveAttribute('href', '/watch/1');
    expect(screen.getByLabelText('Channel statistics')).toHaveTextContent('1');
  });

  it('loads the catalog from the API and filters it by title', async () => {
    mockApi();
    await renderApp('/');

    expect(await screen.findAllByRole('article')).toHaveLength(5);
    expect(screen.getAllByTestId('thumb-placeholder')).toHaveLength(5);

    fireEvent.change(screen.getByLabelText('Search videos'), { target: { value: 'Taylor' } });
    expect(screen.getAllByRole('article')).toHaveLength(1);
    expect(screen.getByRole('heading', { name: 'Taylor Swift - Shake It Off' })).toBeInTheDocument();
  });

  it('restores and persists the theme for anonymous visitors', async () => {
    window.localStorage.setItem('protube:theme', 'dark');
    mockApi();
    await renderApp('/');

    expect(document.documentElement).toHaveAttribute('data-bs-theme', 'dark');
    fireEvent.click(screen.getByRole('button', { name: 'Settings' }));
    fireEvent.click(screen.getByRole('switch', { name: 'Dark mode' }));

    expect(document.documentElement).toHaveAttribute('data-bs-theme', 'light');
    expect(window.localStorage.getItem('protube:theme')).toBe('light');
  });

  it('opens a selected video at its own watch URL', async () => {
    mockApi();
    await renderApp('/');

    fireEvent.click(await screen.findByRole('link', { name: /Taylor Swift - Shake It Off/ }));

    expect(await screen.findByRole('heading', { name: 'Taylor Swift - Shake It Off' })).toBeInTheDocument();
    expect(screen.getByLabelText('Taylor Swift - Shake It Off')).toHaveAttribute('src', 'other/videos/3/file');
    expect(await screen.findByText('Stored comment')).toBeInTheDocument();
    expect(screen.getAllByRole('article')).toHaveLength(4);
    expect(screen.getByRole('link', { name: /Mark Ronson - Uptown Funk/ })).toHaveAttribute('href', '/watch/1');
  });
});
