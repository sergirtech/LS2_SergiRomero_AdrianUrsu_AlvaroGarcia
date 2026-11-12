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
  onCreateList: (name: string) => boolean;
  onAddVideo: (listId: string, videoId: string) => boolean;
  onRemoveVideo: (listId: string, videoId: string) => boolean;
  onDeleteList: (listId: string) => boolean;
}

interface ListCardProps {
  list: UserList;
  videos: Video[];
  onAddVideo: Props['onAddVideo'];
  onRemoveVideo: Props['onRemoveVideo'];
  onDeleteList: Props['onDeleteList'];
}

const ListCard = ({ list, videos, onAddVideo, onRemoveVideo, onDeleteList }: ListCardProps) => {
  const [selectedVideoId, setSelectedVideoId] = useState('');
  const availableVideos = videos.filter((video) => !list.videoIds.includes(video.id));
  const listedVideos = list.videoIds
    .map((videoId) => videos.find((video) => video.id === videoId))
    .filter((video): video is Video => video !== undefined);

  const submitSong = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (selectedVideoId && onAddVideo(list.id, selectedVideoId)) setSelectedVideoId('');
  };

  return (
    <article className="pt-list-card">
      <header className="pt-list-card-header">
        <div>
          <h3>{list.name}</h3>
          <p>
            {list.videoIds.length} {list.videoIds.length === 1 ? 'song' : 'songs'}
          </p>
        </div>
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
  onAddVideo,
  onRemoveVideo,
  onDeleteList,
}: Props) => {
  const [listName, setListName] = useState('');

  const submitList = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (onCreateList(listName)) setListName('');
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
