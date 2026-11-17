import { useState, type FormEvent } from 'react';
import { Link } from 'react-router';
import type { Video } from './VideoCard';
import type { UserList } from '../useProfileLists';

interface Props {
  lists: UserList[];
  videos: Video[];
  loading: boolean;
  error: string | null;
  canEdit: boolean;
  onCreateList: (name: string) => Promise<boolean>;
  onRenameList: (listId: string, name: string) => Promise<boolean>;
  onAddVideo: (listId: string, videoId: string) => Promise<boolean>;
  onRemoveVideo: (listId: string, videoId: string) => Promise<boolean>;
  onDeleteList: (listId: string) => Promise<boolean>;
}

interface ListCardProps {
  list: UserList;
  videos: Video[];
  onRenameList: Props['onRenameList'];
  onAddVideo: Props['onAddVideo'];
  onRemoveVideo: Props['onRemoveVideo'];
  onDeleteList: Props['onDeleteList'];
}

const ListCard = ({ list, videos, onRenameList, onAddVideo, onRemoveVideo, onDeleteList }: ListCardProps) => {
  const [selectedVideoId, setSelectedVideoId] = useState('');
  const [editingName, setEditingName] = useState(false);
  const [listName, setListName] = useState(list.name);
  const availableVideos = videos.filter((video) => !list.videoIds.includes(video.id));
  const listedVideos = list.videoIds.map((videoId) => {
    const video = videos.find((item) => item.id === videoId);
    return { id: videoId, title: video?.title ?? `Video ${videoId}`, channel: video?.channel ?? 'Unavailable video' };
  });

  const submitSong = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (selectedVideoId && (await onAddVideo(list.id, selectedVideoId))) setSelectedVideoId('');
  };

  const submitName = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (await onRenameList(list.id, listName)) {
      setListName(listName.trim());
      setEditingName(false);
    }
  };

  const renameButton = (
    <button
      type="button"
      onClick={() => {
        setListName(list.name);
        setEditingName(true);
      }}
      aria-label={`Rename ${list.name} list`}
    >
      Edit name
    </button>
  );

  return (
    <article className="pt-list-card">
      <header className="pt-list-card-header">
        <div>
          {editingName ? (
            <form onSubmit={submitName}>
              <label className="visually-hidden" htmlFor={`list-name-${list.id}`}>
                Rename {list.name}
              </label>
              <input
                id={`list-name-${list.id}`}
                value={listName}
                onChange={(event) => setListName(event.target.value)}
                maxLength={80}
                required
              />
              <button type="submit" disabled={!listName.trim()}>
                Save name
              </button>
              <button type="button" onClick={() => setEditingName(false)}>
                Cancel
              </button>
            </form>
          ) : (
            <h3>{list.name}</h3>
          )}
          <p>
            {list.videoIds.length} {list.videoIds.length === 1 ? 'song' : 'songs'}
          </p>
        </div>
        {!editingName && renameButton}
        <button
          type="button"
          className="pt-list-delete"
          aria-label={`Delete ${list.name} list`}
          onClick={() => onDeleteList(list.id)}
        >
          Delete list
        </button>
      </header>

      <form className="pt-list-add-form" onSubmit={submitSong}>
        <label htmlFor={`song-${list.id}`}>Add a song to {list.name}</label>
        <div>
          <select
            id={`song-${list.id}`}
            value={selectedVideoId}
            onChange={(event) => setSelectedVideoId(event.target.value)}
            disabled={availableVideos.length === 0}
          >
            <option value="">Choose a song</option>
            {availableVideos.map((video) => (
              <option key={video.id} value={video.id}>
                {video.title}
              </option>
            ))}
          </select>
          <button type="submit" className="pt-list-primary" disabled={!selectedVideoId}>
            Add song
          </button>
        </div>
      </form>

      {listedVideos.length ? (
        <ul className="pt-list-songs">
          {listedVideos.map((video) => (
            <li key={video.id}>
              <Link to={`/watch/${encodeURIComponent(video.id)}`}>{video.title}</Link>
              <span>{video.channel}</span>
              <button
                type="button"
                aria-label={`Remove ${video.title} from ${list.name}`}
                onClick={() => onRemoveVideo(list.id, video.id)}
              >
                Remove
              </button>
            </li>
          ))}
        </ul>
      ) : (
        <p className="pt-list-empty">No songs in this list yet.</p>
      )}
    </article>
  );
};

const ProfileListsPage = ({
  lists,
  videos,
  loading,
  error,
  canEdit,
  onCreateList,
  onRenameList,
  onAddVideo,
  onRemoveVideo,
  onDeleteList,
}: Props) => {
  const [listName, setListName] = useState('');

  const submitList = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (await onCreateList(listName)) setListName('');
  };

  return (
    <section className="pt-profile-lists" aria-labelledby="pt-lists-title">
      <h2 id="pt-lists-title">Lists</h2>
      <p className="pt-lists-description">Create lists and add songs to keep your videos organized.</p>

      {!canEdit && (
        <p className="pt-lists-signin">
          <Link to="/login">Sign in</Link> to create and manage your lists.
        </p>
      )}

      {canEdit && (
        <form className="pt-list-create-form" onSubmit={submitList}>
          <label htmlFor="new-list-name">New list name</label>
          <div>
            <input
              id="new-list-name"
              type="text"
              value={listName}
              onChange={(event) => setListName(event.target.value)}
              placeholder="e.g. Songs to listen to"
              maxLength={80}
              required
            />
            <button type="submit" className="pt-list-primary" disabled={!listName.trim() || loading}>
              Create list
            </button>
          </div>
        </form>
      )}

      {error && (
        <p className="pt-list-error" role="alert">
          {error}
        </p>
      )}

      {loading ? (
        <p className="pt-list-empty" role="status">
          Loading your lists...
        </p>
      ) : lists.length ? (
        <div className="pt-list-grid">
          {lists.map((list) => (
            <ListCard
              key={list.id}
              list={list}
              videos={videos}
              onRenameList={onRenameList}
              onAddVideo={onAddVideo}
              onRemoveVideo={onRemoveVideo}
              onDeleteList={onDeleteList}
            />
          ))}
        </div>
      ) : (
        <p className="pt-list-empty">You haven’t created any lists yet.</p>
      )}
    </section>
  );
};

export default ProfileListsPage;
