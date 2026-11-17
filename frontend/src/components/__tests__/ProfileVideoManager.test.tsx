import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { MemoryRouter } from 'react-router';
import ProfileVideoManager, { type ProfileVideoManagerProps } from '../ProfileVideoManager';

const video = {
  id: '1',
  title: 'First video',
  description: 'Video description',
  channel: 'ana',
};

const makeProps = (overrides: Partial<ProfileVideoManagerProps> = {}): ProfileVideoManagerProps => ({
  user: { id: 1, email: 'ana@example.com', username: 'ana' },
  videos: [],
  loading: false,
  loadingMore: false,
  hasMore: false,
  error: null,
  onRetry: jest.fn(),
  onLoadMore: jest.fn(),
  onUpload: jest.fn().mockResolvedValue(undefined),
  onEdit: jest.fn().mockResolvedValue(undefined),
  onDelete: jest.fn().mockResolvedValue(undefined),
  ...overrides,
});

const renderManager = (props = makeProps()) =>
  render(
    <MemoryRouter>
      <ProfileVideoManager {...props} />
    </MemoryRouter>
  );

const submitUpload = () => {
  const form = screen.getByRole('button', { name: 'Upload video' }).closest('form');
  if (!form) throw new Error('Upload form was not rendered.');
  fireEvent.submit(form);
};

describe('ProfileVideoManager', () => {
  it('asks signed-out visitors to sign in', () => {
    renderManager(makeProps({ user: null }));

    expect(screen.getByRole('link', { name: 'Sign in' })).toHaveAttribute('href', '/login');
    expect(screen.queryByRole('button', { name: 'Upload video' })).not.toBeInTheDocument();
  });

  it('validates upload fields and reports unsupported file types', () => {
    const props = makeProps();
    renderManager(props);

    fireEvent.change(screen.getByLabelText('Title'), { target: { value: 'A video' } });
    fireEvent.change(screen.getByLabelText('Description'), { target: { value: 'Description' } });
    fireEvent.change(screen.getByLabelText(/MP4 file/), {
      target: { files: [new File(['data'], 'clip.mov', { type: 'video/quicktime' })] },
    });
    submitUpload();

    expect(screen.getByRole('alert')).toHaveTextContent('Only .mp4 files are supported.');
    expect(props.onUpload).not.toHaveBeenCalled();
  });

  it('uploads a valid MP4 and announces success', async () => {
    const props = makeProps();
    renderManager(props);
    const file = new File(['video-data'], 'clip.mp4', { type: 'video/mp4' });

    fireEvent.change(screen.getByLabelText('Title'), { target: { value: '  A video  ' } });
    fireEvent.change(screen.getByLabelText('Description'), { target: { value: '  Description  ' } });
    fireEvent.change(screen.getByLabelText(/MP4 file/), { target: { files: [file] } });
    submitUpload();

    expect(await screen.findByRole('status')).toHaveTextContent('Video uploaded successfully.');
    expect(props.onUpload).toHaveBeenCalledWith('A video', 'Description', file);
    expect(screen.getByLabelText('Title')).toHaveValue('');
  });

  it('shows upload failures and retries loading after a list error', async () => {
    const props = makeProps({
      error: 'Could not load videos (HTTP 503).',
      onUpload: jest.fn().mockRejectedValue(new Error('Upload rejected.')),
    });
    renderManager(props);

    fireEvent.change(screen.getByLabelText('Title'), { target: { value: 'A video' } });
    fireEvent.change(screen.getByLabelText('Description'), { target: { value: 'Description' } });
    fireEvent.change(screen.getByLabelText(/MP4 file/), {
      target: { files: [new File(['data'], 'clip.mp4', { type: 'video/mp4' })] },
    });
    submitUpload();

    expect(await screen.findByText('Upload rejected.')).toBeInTheDocument();
    expect(screen.getByText('Could not load videos (HTTP 503).')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Retry loading videos' }));
    expect(props.onRetry).toHaveBeenCalledTimes(1);
  });

  it('edits video metadata and confirms deletion', async () => {
    const props = makeProps({ videos: [video], hasMore: true });
    renderManager(props);

    fireEvent.click(screen.getByRole('button', { name: 'Edit' }));
    fireEvent.change(screen.getByLabelText('Edit title'), { target: { value: 'Updated video' } });
    fireEvent.change(screen.getByLabelText('Edit description'), { target: { value: 'Updated description' } });
    fireEvent.click(screen.getByRole('button', { name: 'Save changes' }));
    await screen.findByRole('status');
    expect(props.onEdit).toHaveBeenCalledWith('1', 'Updated video', 'Updated description');

    fireEvent.click(screen.getByRole('button', { name: 'Delete' }));
    fireEvent.click(screen.getByRole('button', { name: 'Confirm delete' }));
    await waitFor(() => expect(props.onDelete).toHaveBeenCalledWith('1'));
    fireEvent.click(screen.getByRole('button', { name: 'Load more videos' }));
    expect(props.onLoadMore).toHaveBeenCalledTimes(1);
  });

  it('shows empty and loading states', () => {
    const { unmount } = renderManager(makeProps({ loading: true }));
    expect(screen.getByRole('status')).toHaveTextContent('Loading your videos...');
    unmount();

    renderManager(makeProps());
    expect(screen.getByText('You haven’t uploaded any videos yet.')).toBeInTheDocument();
  });
});
