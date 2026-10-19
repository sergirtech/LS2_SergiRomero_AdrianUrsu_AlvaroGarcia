interface Props {
  open: boolean;
}

const ITEMS = [
  { label: 'Home', icon: 'M3 11l9-8 9 8v10h-6v-6H9v6H3z' },
  { label: 'Upload', icon: 'M12 16V4m0 0l-5 5m5-5l5 5M4 20h16' },
  { label: 'My videos', icon: 'M4 5h16v14H4zM10 9v6l5-3z' },
  { label: 'History', icon: 'M12 7v5l3 3M3 12a9 9 0 1 0 3-6.7M3 4v4h4' },
];

const Sidebar = ({ open }: Props) => {
  return (
    <nav className={`pt-sidebar${open ? '' : ' pt-sidebar-collapsed'}`} aria-label="Main">
      {ITEMS.map((item, index) => (
        <a key={item.label} href="#" className={`pt-sidebar-item${index === 0 ? ' active' : ''}`}>
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
        </a>
      ))}
    </nav>
  );
};

export default Sidebar;
