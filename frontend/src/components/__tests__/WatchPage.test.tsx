import { act, fireEvent, render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import fetchMock from 'jest-fetch-mock';
import { MemoryRouter, Route, Routes } from 'react-router';

import { AuthProvider } from '../../useAuth';
import WatchPage from '../WatchPage';

const videos = [
  { id: '0', title: 'Featured video', channel: 'Featured channel', viewCount: 1200 },
  { id: '1', title: 'Recommended video', channel: 'Other channel', viewCount: 2400 },
];

const ana = { id: 1, email: 'ana@mail.com', username: 'ana' };
const metadata = {
  meta: {
    comments: [
      { author: 'first-viewer', text: 'A stored comment' },
      { author: 'second-viewer', text: 'Another stored comment' },
    ],
  },
};

const renderWatchPage = async (path = '/watch/0') => {
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
    fetchMock.mockResponseOnce(JSON.stringify(ana)).mockResponseOnce(JSON.stringify(metadata));
    await renderWatchPage();

    const player = screen.getByLabelText('Featured video');
    expect(player).toHaveAttribute('src', 'development/0.mp4');
    expect(player).toHaveAttribute('autoplay');
    expect(player).toHaveAttribute('controls');
    expect(await screen.findByText('A stored comment')).toBeInTheDocument();
    expect(screen.getByText('Another stored comment')).toBeInTheDocument();
    expect(screen.getAllByRole('article')).toHaveLength(1);
    expect(screen.getByRole('link', { name: /Recommended video/ })).toHaveAttribute('href', '/watch/1');
  });

  it('lets a signed-in user add a temporary comment', async () => {
    fetchMock.mockResponseOnce(JSON.stringify(ana)).mockResponseOnce(JSON.stringify(metadata));
    await renderWatchPage();

    fireEvent.change(screen.getByRole('textbox', { name: 'Add a comment' }), {
      target: { value: '  My temporary comment  ' },
    });
    fireEvent.click(screen.getByRole('button', { name: 'Comment' }));

    expect(await screen.findByText('My temporary comment')).toBeInTheDocument();
    expect(screen.getByText('@ana')).toBeInTheDocument();
    expect(screen.getByRole('textbox', { name: 'Add a comment' })).toHaveValue('');
  });

  it('prompts signed-out users to sign in before commenting', async () => {
    fetchMock.mockResponseOnce('', { status: 401 }).mockResponseOnce(JSON.stringify(metadata));
    await renderWatchPage();

    expect(await screen.findByRole('link', { name: 'Sign in to add a comment' })).toHaveAttribute('href', '/login');
    expect(await screen.findByText('A stored comment')).toBeInTheDocument();
  });

  it('shows an error when saved comments cannot be loaded', async () => {
    fetchMock.mockResponseOnce('', { status: 401 }).mockResponseOnce('Unavailable', { status: 503 });
    await renderWatchPage();

    expect(await screen.findByRole('alert')).toHaveTextContent('Could not load comments (HTTP 503).');
  });

  it('shows a not-found message for an unknown video', async () => {
    fetchMock.mockResponseOnce('', { status: 401 });
    await renderWatchPage('/watch/unknown');

    expect(screen.getByRole('heading', { name: 'Video not found' })).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledTimes(1);
  });
});
