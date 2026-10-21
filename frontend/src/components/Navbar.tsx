import { useState } from 'react';
import { Link } from 'react-router';
import gearIcon from '../assets/icons/gear.svg';
import moonIcon from '../assets/icons/moon.svg';
import userIcon from '../assets/user/default-user.svg';
import logo from '../assets/logo/tubeEnjoyersLogo.svg';
import type { User } from '../useAuth';

interface Props {
  query: string;
  onQueryChange: (query: string) => void;
  onToggleSidebar: () => void;
  user: User | null;
  authReady: boolean;
  onSignOut: () => void;
  darkMode: boolean;
  onDarkModeChange: (enabled: boolean) => void;
}

type ActivePanel = 'account' | 'settings' | null;

const Navbar = ({
  query,
  onQueryChange,
  onToggleSidebar,
  user,
  authReady,
  onSignOut,
  darkMode,
  onDarkModeChange,
}: Props) => {
  const [activePanel, setActivePanel] = useState<ActivePanel>(null);
  const togglePanel = (panel: Exclude<ActivePanel, null>) => {
    setActivePanel((current) => (current === panel ? null : panel));
  };

  return (
    <header className="pt-navbar">
      <div className="pt-navbar-start">
        <button type="button" className="pt-icon-btn" aria-label="Toggle menu" onClick={onToggleSidebar}>
          <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
            <path d="M3 6h18M3 12h18M3 18h18" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
          </svg>
        </button>
        <Link to="/" className="pt-brand">
          <img src={logo} alt="Tube Enjoyers" />
        </Link>
      </div>

      <form className="pt-search" role="search" onSubmit={(event) => event.preventDefault()}>
        <input
          type="search"
          className="form-control"
          placeholder="Search videos"
          aria-label="Search videos"
          value={query}
          onChange={(event) => onQueryChange(event.target.value)}
        />
        <button type="submit" className="pt-search-btn" aria-label="Search">
          <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true">
            <circle cx="11" cy="11" r="7" stroke="currentColor" strokeWidth="2" fill="none" />
            <path d="M20 20l-3.5-3.5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
          </svg>
        </button>
      </form>

      <div className="pt-navbar-end">
        <button
          type="button"
          className="pt-icon-btn"
          aria-label="User access"
          aria-expanded={activePanel === 'account'}
          onClick={() => togglePanel('account')}
        >
          <img src={userIcon} alt="" />
        </button>
        <button
          type="button"
          className="pt-icon-btn"
          aria-label="Settings"
          aria-expanded={activePanel === 'settings'}
          onClick={() => togglePanel('settings')}
        >
          <img className="pt-settings-icon" src={gearIcon} alt="" />
        </button>

        {activePanel === 'account' && (
          <section className="pt-access-panel" aria-label="User access panel">
            <div className="pt-access-user">
              <img src={userIcon} alt="" />
              <span>{authReady ? (user ? user.username : 'Welcome to ProTube') : 'Checking session...'}</span>
            </div>
            {authReady &&
              (user ? (
                <button type="button" className="pt-panel-action" onClick={onSignOut}>
                  Sign out
                </button>
              ) : (
                <Link to="/login" className="pt-panel-action">
                  Sign in
                </Link>
              ))}
          </section>
        )}

        {activePanel === 'settings' && (
          <section className="pt-settings-panel" aria-label="Display settings">
            <h2>Display</h2>
            <button
              type="button"
              className="pt-theme-toggle"
              role="switch"
              aria-checked={darkMode}
              aria-label="Dark mode"
              onClick={() => onDarkModeChange(!darkMode)}
            >
              <img src={moonIcon} alt="" />
              <span>Dark mode</span>
              <span className={`pt-switch-indicator${darkMode ? ' is-on' : ''}`} aria-hidden="true" />
            </button>
          </section>
        )}
      </div>
    </header>
  );
};

export default Navbar;
