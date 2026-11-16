import { act, fireEvent, render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import fetchMock from 'jest-fetch-mock';
import { MemoryRouter, Route, Routes } from 'react-router';

import { AuthProvider } from '../../useAuth';
import { mockApi, reply, videoDetail, videoPage, videoSummary } from '../../testUtils/mockApi';
import WatchPage from '../WatchPage';

const ana = { id: 1, email: 'ana@mail.com', username: 'ana' };
const metadata = {
  meta: {
    comments: [
      { author: 'first-viewer', text: 'A stored comment' },
      { author: 'second-viewer', text: 'Another stored comment' },
    ],
  },
};

const api = (overrides: Record<string, unknown> = {}) =>
  mockApi({
    '/auth/me': ana,
    '/videos/1': videoDetail(1, 'Featured video'),
    '/videos?page=0&size=12': videoPage([videoSummary(1, 'Featured video'), videoSummary(2, 'Recommended video')]),
    '/key-1.json': metadata,
    ...overrides,
  });

const renderWatchPage = async (path = '/watch/1') => {
  render(
    <AuthProvider>
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route path="/watch/:videoId" element={<WatchPage />} />
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

  it('plays the video from the API, loads saved comments and shows recommendations', async () => {
    api();
    await renderWatchPage();

    const player = await screen.findByLabelText('Featured video');
    expect(player).toHaveAttribute('src', 'development/key-1.mp4');
    expect(player).toHaveAttribute('poster', 'development/key-1.webp');
    expect(player).toHaveAttribute('autoplay');
    expect(player).toHaveAttribute('controls');
    expect(screen.getByText('Channel 1')).toBeInTheDocument();
    expect(await screen.findByText('A stored comment')).toBeInTheDocument();
    expect(await screen.findAllByRole('article')).toHaveLength(1);
    expect(screen.getByRole('link', { name: /Recommended video/ })).toHaveAttribute('href', '/watch/2');
  });

  it('lets a signed-in user add a temporary comment', async () => {
    api();
    await renderWatchPage();

    fireEvent.change(await screen.findByRole('textbox', { name: 'Add a comment' }), {
      target: { value: '  My temporary comment  ' },
    });
    fireEvent.click(screen.getByRole('button', { name: 'Comment' }));

    expect(await screen.findByText('My temporary comment')).toBeInTheDocument();
    expect(screen.getByText('@ana')).toBeInTheDocument();
    expect(screen.getByRole('textbox', { name: 'Add a comment' })).toHaveValue('');
  });

  it('prompts signed-out users to sign in before commenting', async () => {
    api({ '/auth/me': reply(401) });
    await renderWatchPage();

    expect(await screen.findByRole('link', { name: 'Sign in to add a comment' })).toHaveAttribute('href', '/login');
    expect(await screen.findByText('A stored comment')).toBeInTheDocument();
  });

  it('shows an error when saved comments cannot be loaded', async () => {
    api({ '/key-1.json': reply(503) });
    await renderWatchPage();

    expect(await screen.findByRole('alert')).toHaveTextContent('Could not load comments (HTTP 503).');
  });

  it('shows a not-found message for an unknown video', async () => {
    api({ '/videos/1': reply(404) });
    await renderWatchPage();

    expect(await screen.findByRole('heading', { name: 'Video not found' })).toBeInTheDocument();
  });

  it('shows an error when the video cannot be loaded', async () => {
    api({ '/videos/1': reply(500) });
    await renderWatchPage();

    expect(await screen.findByRole('alert')).toHaveTextContent('Could not load the video (HTTP 500).');
  });
});
