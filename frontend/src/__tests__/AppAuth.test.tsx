import { act, fireEvent, render, screen, within } from '@testing-library/react';
import '@testing-library/jest-dom';
import fetchMock from 'jest-fetch-mock';
import { MemoryRouter } from 'react-router';

import App from '../App';
import { AuthProvider } from '../useAuth';
import { publishProfileStatsChange } from '../profileStats';
import { mockApi, reply, videoDetail, videoPage, videoSummary } from '../testUtils/mockApi';

// Jest has no CSS transform (frontend/AGENTS.md), so App's stylesheet is replaced by an empty module.
jest.mock('../App.css', () => ({}));

const ana = { id: 1, email: 'ana@mail.com', username: 'ana' };

// Renders and lets AuthProvider finish its initial GET /me, so no state update happens after the test.
const renderApp = async (path: string) => {
  render(
    <AuthProvider>
      <MemoryRouter initialEntries={[path]}>
        <App />
      </MemoryRouter>
    </AuthProvider>
  );
  await act(async () => {});
};

describe('App authentication', () => {
  beforeEach(() => {
    fetchMock.resetMocks();
    window.localStorage.clear();
  });

  it('shows the user in the Navbar after logging in (L1)', async () => {
    mockApi({ '/auth/me': reply(401), '/auth/login': ana, '/videos?page=0&size=12': videoPage([]) });
    await renderApp('/login');

    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'ana@mail.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'secret123' } });
    fireEvent.click(screen.getByRole('button', { name: 'Sign in' }));

    expect(await screen.findByRole('button', { name: 'User access' })).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'User access' }));
    expect(screen.getByRole('button', { name: 'Sign out' })).toBeInTheDocument();
    expect(screen.getByText('ana')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Recommended' })).toBeInTheDocument();
  });

  it('shows the new user in the Navbar after registering (R1)', async () => {
    mockApi({ '/auth/me': reply(401), '/auth/register': reply(201, ana), '/videos?page=0&size=12': videoPage([]) });
    await renderApp('/register');

    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'ana@mail.com' } });
    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'ana' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'secret123' } });
    fireEvent.click(screen.getByRole('button', { name: 'Create account' }));

    expect(await screen.findByRole('button', { name: 'User access' })).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'User access' }));
    expect(screen.getByRole('button', { name: 'Sign out' })).toBeInTheDocument();
    expect(screen.getByText('ana')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Recommended' })).toBeInTheDocument();
  });

  it('signs out and shows Sign in again (L3)', async () => {
    mockApi({ '/auth/me': ana, '/auth/logout': reply(204), '/videos?page=0&size=12': videoPage([]) });
    await renderApp('/');

    fireEvent.click(screen.getByRole('button', { name: 'User access' }));
    fireEvent.click(screen.getByRole('button', { name: 'Sign out' }));

    expect(await screen.findByRole('link', { name: 'Sign in' })).toBeInTheDocument();
    expect(screen.queryByText('ana')).not.toBeInTheDocument();
    expect(fetchMock.mock.calls.map(([url]) => url)).toContain('other/auth/logout');
  });

  it('stays signed in when the server cannot be reached on sign out', async () => {
    mockApi({ '/auth/me': ana, '/videos?page=0&size=12': videoPage([]) });
    await renderApp('/');
    fetchMock.mockRejectOnce(new Error('offline'));

    fireEvent.click(screen.getByRole('button', { name: 'User access' }));
    fireEvent.click(screen.getByRole('button', { name: 'Sign out' }));
    await act(async () => {});

    expect(screen.getByRole('button', { name: 'Sign out' })).toBeInTheDocument();
  });

  it('shows the current user profile and updates its statistics', async () => {
    fetchMock.mockResponseOnce(JSON.stringify(ana));
    await renderApp('/profile');

    expect(screen.getByRole('heading', { name: 'ana' })).toBeInTheDocument();
    expect(screen.getByText('ana@mail.com')).toBeInTheDocument();
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
    fetchMock.mockResponseOnce(JSON.stringify(ana));
    await renderApp('/profile/lists');

    fireEvent.change(screen.getByLabelText('New list name'), { target: { value: 'Road trip' } });
    fireEvent.click(screen.getByRole('button', { name: 'Create list' }));

    expect(await screen.findByRole('heading', { name: 'Road trip' })).toBeInTheDocument();
    expect(screen.getByLabelText('Channel statistics')).toHaveTextContent('1');

    fireEvent.change(screen.getByLabelText('Add a song to Road trip'), { target: { value: '1' } });
    fireEvent.click(screen.getByRole('button', { name: 'Add song' }));

    expect(
      screen.getByRole('link', { name: 'Mark Ronson - Uptown Funk (Official Video) ft. Bruno Mars' })
    ).toHaveAttribute('href', '/watch/1');
    expect(JSON.parse(window.localStorage.getItem('protube:user-lists:1') ?? '[]')).toEqual([
      expect.objectContaining({ name: 'Road trip', videoIds: ['1'] }),
    ]);
  });

  it('restores the signed-in user lists from browser storage', async () => {
    window.localStorage.setItem(
      'protube:user-lists:1',
      JSON.stringify([{ id: 'list-1', name: 'Road trip', videoIds: ['1'] }])
    );
    fetchMock.mockResponseOnce(JSON.stringify(ana));

    await renderApp('/profile/lists');

    expect(await screen.findByRole('heading', { name: 'Road trip' })).toBeInTheDocument();
    expect(
      screen.getByRole('link', { name: 'Mark Ronson - Uptown Funk (Official Video) ft. Bruno Mars' })
    ).toHaveAttribute('href', '/watch/1');
    expect(screen.getByLabelText('Channel statistics')).toHaveTextContent('1');
  });

  it('loads the catalog from the API and the next page on demand (US-04)', async () => {
    mockApi({
      '/auth/me': reply(401),
      '/videos?page=0&size=12': videoPage([videoSummary(1, 'Uptown Funk'), videoSummary(2, 'Shake It Off')], true),
      '/videos?page=1&size=12': videoPage([videoSummary(3, 'Me Rehúso')], false, 1),
    });
    await renderApp('/');

    expect(await screen.findAllByRole('article')).toHaveLength(2);
    expect(screen.getByAltText('Uptown Funk thumbnail')).toHaveAttribute('src', 'development/key-1.webp');

    fireEvent.click(screen.getByRole('button', { name: 'Load more' }));

    expect(await screen.findByRole('heading', { name: 'Me Rehúso' })).toBeInTheDocument();
    expect(screen.getAllByRole('article')).toHaveLength(3);
    expect(screen.queryByRole('button', { name: 'Load more' })).not.toBeInTheDocument();

    fireEvent.change(screen.getByLabelText('Search videos'), { target: { value: 'Shake' } });
    expect(screen.getAllByRole('article')).toHaveLength(1);
  });

  it('shows an error and retries loading the catalog', async () => {
    mockApi({ '/auth/me': reply(401), '/videos?page=0&size=12': reply(503) });
    await renderApp('/');

    expect(await screen.findByRole('alert')).toHaveTextContent('Could not load videos (HTTP 503).');

    mockApi({ '/auth/me': reply(401), '/videos?page=0&size=12': videoPage([videoSummary(1, 'Uptown Funk')]) });
    fireEvent.click(screen.getByRole('button', { name: 'Try again' }));

    expect(await screen.findByRole('heading', { name: 'Uptown Funk' })).toBeInTheDocument();
  });

  it('opens a selected video at its own watch URL', async () => {
    mockApi({
      '/auth/me': reply(401),
      '/videos?page=0&size=12': videoPage([videoSummary(1, 'Uptown Funk'), videoSummary(2, 'Shake It Off')]),
      '/videos/2': videoDetail(2, 'Shake It Off'),
      '/key-2.json': { meta: { comments: [{ author: 'viewer', text: 'Stored comment' }] } },
    });
    await renderApp('/');

    fireEvent.click(await screen.findByRole('link', { name: /Shake It Off/ }));

    expect(await screen.findByRole('heading', { name: 'Shake It Off' })).toBeInTheDocument();
    expect(screen.getByLabelText('Shake It Off')).toHaveAttribute('src', 'development/key-2.mp4');
    expect(await screen.findByText('Stored comment')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /Uptown Funk/ })).toHaveAttribute('href', '/watch/1');
  });

  it('navigates between sections with the sidebar', async () => {
    mockApi({
      '/auth/me': ana,
      '/videos?page=0&size=12': videoPage([]),
      '/videos/mine?page=0&size=12': videoPage([]),
    });
    await renderApp('/watch/1');
    const sidebar = within(screen.getByRole('navigation', { name: 'Main' }));

    fireEvent.click(sidebar.getByRole('link', { name: 'Upload' }));
    expect(screen.getByRole('heading', { name: 'Upload a video' })).toBeInTheDocument();

    fireEvent.click(sidebar.getByRole('link', { name: 'My videos' }));
    expect(screen.getByRole('heading', { name: 'My videos' })).toBeInTheDocument();

    fireEvent.click(sidebar.getByRole('link', { name: 'History' }));
    expect(screen.getByRole('heading', { name: 'Playback history' })).toBeInTheDocument();

    fireEvent.click(sidebar.getByRole('link', { name: 'Home' }));
    expect(await screen.findByRole('heading', { name: 'Recommended' })).toBeInTheDocument();
  });
});
