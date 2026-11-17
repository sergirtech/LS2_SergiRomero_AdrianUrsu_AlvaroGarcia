import { act, fireEvent, render, screen, within } from '@testing-library/react';
import '@testing-library/jest-dom';
import fetchMock from 'jest-fetch-mock';
import { MemoryRouter, Route, Routes } from 'react-router';

import { AuthProvider } from '../../useAuth';
import { mockApi, reply, videoDetail, videoPage, videoSummary } from '../../testUtils/mockApi';
import MyVideosPage from '../MyVideosPage';

const ana = { id: 1, email: 'ana@mail.com', username: 'ana' };
const MINE = '/videos/mine?page=0&size=12';

const owned = (id: number, title: string) => ({
  ...videoDetail(id, title),
  ownerId: 1,
  tags: ['travel', 'beach'],
  category: 'Entertainment',
});

const renderMyVideos = async (path = '/my-videos') => {
  render(
    <AuthProvider>
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route path="/my-videos" element={<MyVideosPage />} />
          <Route path="/login" element={<p>Login page</p>} />
        </Routes>
      </MemoryRouter>
    </AuthProvider>
  );
  await act(async () => {});
};

const requestsWith = (method: string) => fetchMock.mock.calls.filter(([, init]) => init?.method === method);

describe('MyVideosPage', () => {
  beforeEach(() => {
    fetchMock.resetMocks();
  });

  it('sends visitors to the login page', async () => {
    mockApi({ '/auth/me': reply(401) });
    await renderMyVideos();

    expect(screen.getByText('Login page')).toBeInTheDocument();
  });

  it('lists the user videos and loads more pages', async () => {
    mockApi({
      '/auth/me': ana,
      [MINE]: videoPage([videoSummary(2, 'Second'), videoSummary(1, 'First')], true),
      '/videos/mine?page=1&size=12': videoPage([videoSummary(0, 'Oldest')], false, 1),
    });
    await renderMyVideos();

    expect(await screen.findByRole('link', { name: 'Second' })).toHaveAttribute('href', '/watch/2');
    expect(fetchMock.mock.calls.find(([url]) => String(url).endsWith(MINE))?.[1]?.credentials).toBe('include');

    fireEvent.click(screen.getByRole('button', { name: 'Load more' }));

    expect(await screen.findByRole('link', { name: 'Oldest' })).toBeInTheDocument();
    expect(screen.getAllByRole('listitem')).toHaveLength(3);
    expect(screen.queryByRole('button', { name: 'Load more' })).not.toBeInTheDocument();
  });

  it('shows an empty state with a link to upload', async () => {
    mockApi({ '/auth/me': ana, [MINE]: videoPage([]) });
    await renderMyVideos();

    expect(await screen.findByText(/haven't uploaded any videos yet/)).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Upload your first one' })).toHaveAttribute('href', '/upload');
  });

  it('offers to retry when the list fails', async () => {
    mockApi({ '/auth/me': ana, [MINE]: reply(500, { message: 'Database down' }) });
    await renderMyVideos();

    expect(await screen.findByRole('alert')).toHaveTextContent('Database down');

    mockApi({ '/auth/me': ana, [MINE]: videoPage([videoSummary(1, 'First')]) });
    fireEvent.click(screen.getByRole('button', { name: 'Try again' }));
    expect(await screen.findByRole('link', { name: 'First' })).toBeInTheDocument();
  });

  it('edits the metadata of a video', async () => {
    mockApi({
      '/auth/me': ana,
      [MINE]: videoPage([videoSummary(1, 'First')]),
      '/videos/1': owned(1, 'First'),
    });
    await renderMyVideos();

    fireEvent.click(await screen.findByRole('button', { name: 'Edit First' }));
    const form = await screen.findByRole('form', { name: 'Edit video' });
    expect(within(form).getByLabelText('Tags')).toHaveValue('travel, beach');
    expect(within(form).getByLabelText('Category')).toHaveValue('Entertainment');

    fireEvent.change(within(form).getByLabelText('Title'), { target: { value: '' } });
    fireEvent.click(within(form).getByRole('button', { name: 'Save changes' }));
    expect(within(form).getByText('Title is required')).toBeInTheDocument();
    expect(requestsWith('PUT')).toHaveLength(0);

    mockApi({ '/auth/me': ana, '/videos/1': { ...owned(1, 'Renamed'), category: null } });
    fireEvent.change(within(form).getByLabelText('Title'), { target: { value: 'Renamed' } });
    fireEvent.change(within(form).getByLabelText('Category'), { target: { value: '' } });
    fireEvent.click(within(form).getByRole('button', { name: 'Save changes' }));

    expect(await screen.findByText('Changes to "Renamed" saved.')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Renamed' })).toBeInTheDocument();
    const [url, init] = requestsWith('PUT')[0];
    expect(url).toBe('other/videos/1');
    expect(JSON.parse(String(init?.body))).toEqual({
      title: 'Renamed',
      description: 'A description',
      tags: ['travel', 'beach'],
      category: null,
    });
  });

  it('shows the error when saving is denied and can cancel', async () => {
    mockApi({ '/auth/me': ana, [MINE]: videoPage([videoSummary(1, 'First')]), '/videos/1': owned(1, 'First') });
    await renderMyVideos();
    fireEvent.click(await screen.findByRole('button', { name: 'Edit First' }));
    const form = await screen.findByRole('form', { name: 'Edit video' });

    mockApi({ '/auth/me': ana, '/videos/1': reply(403, { message: 'You can only change your own videos (video 1)' }) });
    fireEvent.click(within(form).getByRole('button', { name: 'Save changes' }));
    expect(await within(form).findByRole('alert')).toHaveTextContent('You can only change your own videos');

    fireEvent.click(within(form).getByRole('button', { name: 'Cancel' }));
    expect(screen.queryByRole('form', { name: 'Edit video' })).not.toBeInTheDocument();
  });

  it('opens the editor from the ?edit= link of the watch page', async () => {
    mockApi({ '/auth/me': ana, [MINE]: videoPage([videoSummary(1, 'First')]), '/videos/1': owned(1, 'First') });
    await renderMyVideos('/my-videos?edit=1');

    const form = await screen.findByRole('form', { name: 'Edit video' });
    expect(await within(form).findByLabelText('Title')).toHaveValue('First');
  });

  it('reports a video that cannot be loaded for editing', async () => {
    mockApi({ '/auth/me': ana, [MINE]: videoPage([videoSummary(1, 'First')]), '/videos/1': reply(500) });
    await renderMyVideos();

    fireEvent.click(await screen.findByRole('button', { name: 'Edit First' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Could not load the video (HTTP 500).');
  });

  it('asks for confirmation before deleting', async () => {
    mockApi({
      '/auth/me': ana,
      [MINE]: videoPage([videoSummary(1, 'First'), videoSummary(2, 'Second')]),
      '/videos/1': reply(204),
    });
    await renderMyVideos();

    fireEvent.click(await screen.findByRole('button', { name: 'Delete First' }));
    fireEvent.click(screen.getByRole('button', { name: 'Keep it' }));
    expect(requestsWith('DELETE')).toHaveLength(0);

    fireEvent.click(screen.getByRole('button', { name: 'Delete First' }));
    fireEvent.click(screen.getByRole('button', { name: 'Yes, delete' }));

    expect(await screen.findByText('"First" was deleted.')).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'First' })).not.toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Second' })).toBeInTheDocument();
    expect(requestsWith('DELETE')[0][0]).toBe('other/videos/1');
  });

  it('keeps the video when the delete fails', async () => {
    mockApi({
      '/auth/me': ana,
      [MINE]: videoPage([videoSummary(1, 'First')]),
      '/videos/1': reply(403, { message: 'You can only change your own videos (video 1)' }),
    });
    await renderMyVideos();

    fireEvent.click(await screen.findByRole('button', { name: 'Delete First' }));
    fireEvent.click(screen.getByRole('button', { name: 'Yes, delete' }));

    expect(await screen.findByText('You can only change your own videos (video 1)')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'First' })).toBeInTheDocument();
  });
});
