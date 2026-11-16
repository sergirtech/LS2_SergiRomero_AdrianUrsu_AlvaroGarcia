import { NavLink } from 'react-router';

interface Props {
  open: boolean;
}

const ITEMS = [
  { label: 'Home', icon: 'M3 11l9-8 9 8v10h-6v-6H9v6H3z', to: '/' },
  // Placeholder pages until US-06 (upload), US-07/US-11 (my videos) and the playback history are built.
  { label: 'Upload', icon: 'M12 16V4m0 0l-5 5m5-5l5 5M4 20h16', to: '/upload' },
  { label: 'My videos', icon: 'M4 5h16v14H4zM10 9v6l5-3z', to: '/my-videos' },
  { label: 'History', icon: 'M12 7v5l3 3M3 12a9 9 0 1 0 3-6.7M3 4v4h4', to: '/profile/history' },
];

const Sidebar = ({ open }: Props) => {
  return (
    <nav className={`pt-sidebar${open ? '' : ' pt-sidebar-collapsed'}`} aria-label="Main">
      {ITEMS.map((item) => (
        <NavLink
          key={item.label}
          to={item.to}
          end
          aria-label={item.label}
          title={item.label}
          className={({ isActive }) => `pt-sidebar-item${isActive ? ' active' : ''}`}
        >
          <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
            <path
              d={item.icon}
              stroke="currentColor"
              strokeWidth="1.8"
              fill="none"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
          <span>{item.label}</span>
        </NavLink>
      ))}
    </nav>
  );
};

export default Sidebar;
