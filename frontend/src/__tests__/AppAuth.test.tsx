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
    fetchMock.mockResponseOnce('', { status: 401 }).mockResponseOnce(JSON.stringify(ana));
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
    fetchMock.mockResponseOnce('', { status: 401 }).mockResponseOnce(JSON.stringify(ana), { status: 201 });
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
    fetchMock.mockResponseOnce(JSON.stringify(ana)).mockResponseOnce('', { status: 204 });
    await renderApp('/');

    fireEvent.click(screen.getByRole('button', { name: 'User access' }));
    fireEvent.click(screen.getByRole('button', { name: 'Sign out' }));

    expect(await screen.findByRole('link', { name: 'Sign in' })).toBeInTheDocument();
    expect(screen.queryByText('ana')).not.toBeInTheDocument();
    expect(fetchMock.mock.calls[1][0]).toBe('other/auth/logout');
  });

  it('stays signed in when the server cannot be reached on sign out', async () => {
    fetchMock.mockResponseOnce(JSON.stringify(ana)).mockRejectOnce(new Error('offline'));
    await renderApp('/');

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

    fireEvent.change(screen.getByLabelText('Add a song to Road trip'), { target: { value: '0' } });
    fireEvent.click(screen.getByRole('button', { name: 'Add song' }));

    expect(
      screen.getByRole('link', { name: 'Mark Ronson - Uptown Funk (Official Video) ft. Bruno Mars' })
    ).toHaveAttribute('href', '/watch/0');
    expect(JSON.parse(window.localStorage.getItem('protube:user-lists:1') ?? '[]')).toEqual([
      expect.objectContaining({ name: 'Road trip', videoIds: ['0'] }),
    ]);
  });

  it('restores the signed-in user lists from browser storage', async () => {
    window.localStorage.setItem(
      'protube:user-lists:1',
      JSON.stringify([{ id: 'list-1', name: 'Road trip', videoIds: ['0'] }])
    );
    fetchMock.mockResponseOnce(JSON.stringify(ana));

    await renderApp('/profile/lists');

    expect(await screen.findByRole('heading', { name: 'Road trip' })).toBeInTheDocument();
    expect(
      screen.getByRole('link', { name: 'Mark Ronson - Uptown Funk (Official Video) ft. Bruno Mars' })
    ).toHaveAttribute('href', '/watch/0');
    expect(screen.getByLabelText('Channel statistics')).toHaveTextContent('1');
  });

  it('shows only the first five video thumbnails and filters them by title', async () => {
    fetchMock.mockResponseOnce('', { status: 401 });
    await renderApp('/');

    expect(screen.getAllByRole('article')).toHaveLength(5);
    expect(screen.getByAltText('Mark Ronson - Uptown Funk (Official Video) ft. Bruno Mars thumbnail')).toHaveAttribute(
      'src',
      'development/0.webp'
    );
    expect(screen.getByAltText('Danny Ocean - Me Rehúso (Official Audio) thumbnail')).toHaveAttribute(
      'src',
      'development/4.webp'
    );

    fireEvent.change(screen.getByLabelText('Search videos'), { target: { value: 'Taylor' } });
    expect(screen.getAllByRole('article')).toHaveLength(1);
    expect(screen.getByRole('heading', { name: 'Taylor Swift - Shake It Off' })).toBeInTheDocument();
  });

  it('opens a selected video at its own watch URL', async () => {
    fetchMock.mockResponseOnce('', { status: 401 }).mockResponseOnce(
      JSON.stringify({
        meta: {
          comments: [{ author: 'viewer', text: 'Stored comment' }],
        },
      })
    );
    await renderApp('/');

    fireEvent.click(screen.getByRole('link', { name: /Taylor Swift - Shake It Off/ }));

    expect(await screen.findByRole('heading', { name: 'Taylor Swift - Shake It Off' })).toBeInTheDocument();
    expect(screen.getByLabelText('Taylor Swift - Shake It Off')).toHaveAttribute('src', 'development/2.mp4');
    expect(await screen.findByText('Stored comment')).toBeInTheDocument();
    expect(screen.getAllByRole('article')).toHaveLength(4);
    expect(screen.getByRole('link', { name: /Mark Ronson - Uptown Funk/ })).toHaveAttribute('href', '/watch/0');
  });
});
