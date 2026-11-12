import { fireEvent, render, screen, within } from '@testing-library/react';
import '@testing-library/jest-dom';
import { MemoryRouter } from 'react-router';

import ProfileListsPage from '../ProfileListsPage';
import type { Video } from '../VideoCard';
import type { UserList } from '../../useProfileLists';

const videos: Video[] = [
  { id: 'song-1', title: 'First song', channel: 'Artist One', viewCount: 100 },
  { id: 'song-2', title: 'Second song', channel: 'Artist Two', viewCount: 200 },
];

const existingList: UserList = { id: 'list-1', name: 'Road trip', videoIds: ['song-1'] };

const renderListsPage = (overrides: Partial<Parameters<typeof ProfileListsPage>[0]> = {}) => {
  const props: Parameters<typeof ProfileListsPage>[0] = {
    lists: [],
    videos,
    loading: false,
    error: null,
    canEdit: true,
    onCreateList: jest.fn(() => true),
    onAddVideo: jest.fn(() => true),
    onRemoveVideo: jest.fn(() => true),
    onDeleteList: jest.fn(() => true),
    ...overrides,
  };

  render(
    <MemoryRouter>
      <ProfileListsPage {...props} />
    </MemoryRouter>
  );
  return props;
};

describe('ProfileListsPage', () => {
  it('creates a named list', () => {
    const props = renderListsPage();

    fireEvent.change(screen.getByLabelText('New list name'), { target: { value: '  Workout  ' } });
    fireEvent.click(screen.getByRole('button', { name: 'Create list' }));

    expect(props.onCreateList).toHaveBeenCalledWith('  Workout  ');
  });

  it('adds an available song and clears the selector after a successful add', () => {
    const props = renderListsPage({ lists: [existingList] });
    const songSelect = screen.getByLabelText('Add a song to Road trip');

    expect(within(songSelect).queryByRole('option', { name: 'First song' })).not.toBeInTheDocument();
    fireEvent.change(songSelect, { target: { value: 'song-2' } });
    fireEvent.click(screen.getByRole('button', { name: 'Add song' }));

    expect(props.onAddVideo).toHaveBeenCalledWith('list-1', 'song-2');
    expect(songSelect).toHaveValue('');
  });

  it('renders songs and supports removing a song and deleting its list', () => {
    const props = renderListsPage({ lists: [existingList] });

    expect(screen.getByRole('link', { name: 'First song' })).toHaveAttribute('href', '/watch/song-1');
    fireEvent.click(screen.getByRole('button', { name: 'Remove First song from Road trip' }));
    fireEvent.click(screen.getByRole('button', { name: 'Delete Road trip list' }));

    expect(props.onRemoveVideo).toHaveBeenCalledWith('list-1', 'song-1');
    expect(props.onDeleteList).toHaveBeenCalledWith('list-1');
  });

  it('shows persistence errors and loading status', () => {
    const { rerender } = render(
      <MemoryRouter>
        <ProfileListsPage
          lists={[]}
          videos={[]}
          loading
          error="Could not save your lists."
          canEdit
          onCreateList={jest.fn()}
          onAddVideo={jest.fn()}
          onRemoveVideo={jest.fn()}
          onDeleteList={jest.fn()}
        />
      </MemoryRouter>
    );

    expect(screen.getByRole('alert')).toHaveTextContent('Could not save your lists.');
    expect(screen.getByRole('status')).toHaveTextContent('Loading your lists...');

    rerender(
      <MemoryRouter>
        <ProfileListsPage
          lists={[]}
          videos={[]}
          loading={false}
          error={null}
          canEdit={false}
          onCreateList={jest.fn()}
          onAddVideo={jest.fn()}
          onRemoveVideo={jest.fn()}
          onDeleteList={jest.fn()}
        />
      </MemoryRouter>
    );
    expect(screen.getByRole('link', { name: 'Sign in' })).toHaveAttribute('href', '/login');
  });
});
