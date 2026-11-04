import { Link, NavLink, useLocation } from 'react-router';
import logo from '../assets/logo/tubeEnjoyersLogo.svg';
import defaultUser from '../assets/user/default-user.svg';
import type { User } from '../useAuth';
import type { ProfileStats } from '../profileStats';
import type { Video } from './VideoCard';
import type { UserList } from '../useProfileLists';
import ProfileListsPage from './ProfileListsPage';

interface Props {
  user: User | null;
  stats: ProfileStats;
  videos: Video[];
  lists: UserList[];
  listsLoading: boolean;
  listsError: string | null;
  onCreateList: (name: string) => boolean;
  onAddVideo: (listId: string, videoId: string) => boolean;
  onRemoveVideo: (listId: string, videoId: string) => boolean;
  onDeleteList: (listId: string) => boolean;
}

const PROFILE_LINKS = [
  { label: 'Playback history', path: '/profile/history', icon: 'M12 8v4l3 2M4.5 7.5A9 9 0 1 1 3 12M3 4v4h4' },
  { label: 'Downloaded videos', path: '/profile/downloads', icon: 'M12 3v12m0 0 5-5m-5 5-5-5M4 19h16' },
  {
    label: 'Favourites',
    path: '/profile/favourites',
    icon: 'M20.8 4.6a5.4 5.4 0 0 0-7.6 0L12 5.8l-1.2-1.2a5.4 5.4 0 0 0-7.6 7.6L12 21l8.8-8.8a5.4 5.4 0 0 0 0-7.6Z',
  },
  { label: 'Account', path: '/profile/account', icon: 'M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm-8 9a8 8 0 0 1 16 0' },
  {
    label: 'Help and support',
    path: '/profile/help',
    icon: 'M9.1 9a3 3 0 1 1 5.7 1.4c-.8 1.2-2.8 1.6-2.8 3.6M12 17.5v.1M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z',
  },
];

const PLACEHOLDER_TITLES: Record<string, string> = {
  '/profile/history': 'Playback history',
  '/profile/downloads': 'Downloaded videos',
  '/profile/favourites': 'Favourites',
  '/profile/account': 'Account',
  '/profile/help': 'Help and support',
  '/profile/lists': 'Lists',
  '/profile/configuration': 'Configuration',
};

const ProfilePage = ({
  user,
  stats,
  videos,
  lists,
  listsLoading,
  listsError,
  onCreateList,
  onAddVideo,
  onRemoveVideo,
  onDeleteList,
}: Props) => {
  const { pathname } = useLocation();
  const isProfileHome = pathname === '/profile' || pathname === '/profile/';

  return (
    <section className="pt-profile" aria-label="User profile">
      <header className="pt-profile-header">
        <Link to="/" className="pt-profile-brand">
          <img src={logo} alt="Tube Enjoyers" />
        </Link>

        <div className="pt-profile-summary">
          <img className="pt-profile-avatar" src={defaultUser} alt="" />
          <div className="pt-profile-identity">
            <h1>{user?.username ?? 'Your profile'}</h1>
            <p>{user?.email ?? 'Sign in to see your profile information'}</p>
          </div>

          <div className="pt-profile-stats" aria-label="Channel statistics">
            <div>
              <strong>{stats.lists}</strong>
              <span>Lists</span>
            </div>
            <div>
              <strong>{stats.followers}</strong>
              <span>Followers</span>
            </div>
          </div>
        </div>
      </header>

      <nav className="pt-profile-tabs" aria-label="Profile sections">
        <NavLink to="/profile" end className={({ isActive }) => `pt-profile-tab${isActive ? ' active' : ''}`}>
          My profile
        </NavLink>
        <NavLink to="/profile/lists" className={({ isActive }) => `pt-profile-tab${isActive ? ' active' : ''}`}>
          Lists
        </NavLink>
        <NavLink to="/profile/configuration" className={({ isActive }) => `pt-profile-tab${isActive ? ' active' : ''}`}>
          Configuration
        </NavLink>
      </nav>

      {isProfileHome ? (
        <nav className="pt-profile-links" aria-label="Profile shortcuts">
          {PROFILE_LINKS.map((item) => (
            <Link key={item.path} to={item.path} className="pt-profile-link">
              <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true">
                <path
                  d={item.icon}
                  stroke="currentColor"
                  strokeWidth="1.7"
                  fill="none"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
              </svg>
              <span>{item.label}</span>
            </Link>
          ))}
        </nav>
      ) : pathname === '/profile/lists' ? (
        <ProfileListsPage
          lists={lists}
          videos={videos}
          loading={listsLoading}
          error={listsError}
          canEdit={user !== null}
          onCreateList={onCreateList}
          onAddVideo={onAddVideo}
          onRemoveVideo={onRemoveVideo}
          onDeleteList={onDeleteList}
        />
      ) : (
        <section className="pt-profile-placeholder" aria-live="polite">
          <h2>{PLACEHOLDER_TITLES[pathname] ?? 'Profile'}</h2>
          <p>This page will be available soon.</p>
          <Link to="/profile">Back to my profile</Link>
        </section>
      )}
    </section>
  );
};

export default ProfilePage;
