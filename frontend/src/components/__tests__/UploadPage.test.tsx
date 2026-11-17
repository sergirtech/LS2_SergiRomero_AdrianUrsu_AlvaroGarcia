import { act, fireEvent, render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import fetchMock from 'jest-fetch-mock';
import { MemoryRouter, Route, Routes } from 'react-router';

import { AuthProvider } from '../../useAuth';
import { mockApi, reply, videoDetail } from '../../testUtils/mockApi';
import UploadPage from '../UploadPage';

const ana = { id: 1, email: 'ana@mail.com', username: 'ana' };

const renderUpload = async () => {
  render(
    <AuthProvider>
      <MemoryRouter initialEntries={['/upload']}>
        <Routes>
          <Route path="/upload" element={<UploadPage />} />
          <Route path="/login" element={<p>Login page</p>} />
          <Route path="/watch/:videoId" element={<p>Watch page</p>} />
        </Routes>
      </MemoryRouter>
    </AuthProvider>
  );
  await act(async () => {});
};

const mp4 = (name = 'holiday.mp4', size = 3) => new File([new Uint8Array(size)], name, { type: 'video/mp4' });

const chooseFile = (file: File) => fireEvent.change(screen.getByLabelText('Video file'), { target: { files: [file] } });

const uploadRequest = () => fetchMock.mock.calls.find(([, init]) => init?.method === 'POST');

describe('UploadPage', () => {
  beforeEach(() => {
    fetchMock.resetMocks();
  });

  it('sends visitors to the login page', async () => {
    mockApi({ '/auth/me': reply(401) });
    await renderUpload();

    expect(screen.getByText('Login page')).toBeInTheDocument();
  });

  it('uploads the file with its metadata and opens the new video', async () => {
    mockApi({ '/auth/me': ana, '/videos': reply(201, videoDetail(42, 'Holiday')) });
    await renderUpload();

    chooseFile(mp4());
    // The title is prefilled from the file name.
    expect(screen.getByLabelText('Title')).toHaveValue('holiday');
    fireEvent.change(screen.getByLabelText('Title'), { target: { value: '  Summer holiday ' } });
    fireEvent.change(screen.getByLabelText('Description'), { target: { value: 'At the beach' } });
    fireEvent.change(screen.getByLabelText('Tags'), { target: { value: 'travel, , beach' } });
    fireEvent.change(screen.getByLabelText('Category'), { target: { value: 'Entertainment' } });
    fireEvent.click(screen.getByRole('button', { name: 'Upload' }));

    expect(await screen.findByText('Watch page')).toBeInTheDocument();
    const [url, init] = uploadRequest() ?? [];
    expect(url).toBe('other/videos');
    expect(init?.credentials).toBe('include');
    const body = init?.body as FormData;
    expect((body.get('file') as File).name).toBe('holiday.mp4');
    expect(body.get('title')).toBe('Summer holiday');
    expect(body.get('description')).toBe('At the beach');
    expect(body.getAll('tags')).toEqual(['travel', 'beach']);
    expect(body.get('category')).toBe('Entertainment');
  });

  it('checks the file and the title before uploading', async () => {
    mockApi({ '/auth/me': ana });
    await renderUpload();

    fireEvent.click(screen.getByRole('button', { name: 'Upload' }));
    expect(screen.getByText('Choose a video file')).toBeInTheDocument();
    expect(screen.getByText('Title is required')).toBeInTheDocument();

    chooseFile(mp4('clip.avi'));
    fireEvent.click(screen.getByRole('button', { name: 'Upload' }));
    expect(screen.getByText('Only .mp4 files are supported')).toBeInTheDocument();

    chooseFile(mp4('empty.mp4', 0));
    fireEvent.click(screen.getByRole('button', { name: 'Upload' }));
    expect(screen.getByText('The video file is empty')).toBeInTheDocument();

    const big = mp4('big.mp4');
    Object.defineProperty(big, 'size', { value: 101 * 1024 * 1024 });
    chooseFile(big);
    fireEvent.click(screen.getByRole('button', { name: 'Upload' }));
    expect(screen.getByText('The video file must be 100 MB or smaller')).toBeInTheDocument();

    fireEvent.change(screen.getByLabelText('Tags'), {
      target: { value: Array.from({ length: 11 }, (_, i) => `tag${i}`).join(',') },
    });
    fireEvent.click(screen.getByRole('button', { name: 'Upload' }));
    expect(screen.getByText('Use at most 10 tags')).toBeInTheDocument();
    expect(uploadRequest()).toBeUndefined();
  });

  it('shows the error the server gives and lets the user try again', async () => {
    mockApi({ '/auth/me': ana, '/videos': reply(400, { message: 'The file is not a valid MP4 video' }) });
    await renderUpload();

    chooseFile(mp4());
    fireEvent.click(screen.getByRole('button', { name: 'Upload' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('The file is not a valid MP4 video');
    expect(screen.getByRole('button', { name: 'Upload' })).toBeEnabled();
  });

  it('explains an expired session and a server that cannot be reached', async () => {
    mockApi({ '/auth/me': ana, '/videos': reply(401) });
    await renderUpload();
    chooseFile(mp4());
    fireEvent.click(screen.getByRole('button', { name: 'Upload' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('Your session has expired. Sign in again.');

    fetchMock.mockReject(new Error('offline'));
    fireEvent.click(screen.getByRole('button', { name: 'Upload' }));
    expect(await screen.findByText('Could not reach the server. Try again later.')).toBeInTheDocument();
  });
});
