import { Link } from 'react-router';

interface Props {
  query: string;
  onQueryChange: (query: string) => void;
  onToggleSidebar: () => void;
}

const Navbar = ({ query, onQueryChange, onToggleSidebar }: Props) => {
  return (
    <header className="pt-navbar">
      <div className="pt-navbar-start">
        <button type="button" className="pt-icon-btn" aria-label="Toggle menu" onClick={onToggleSidebar}>
          <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
            <path d="M3 6h18M3 12h18M3 18h18" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
          </svg>
        </button>
        <Link to="/" className="pt-brand">
          <img src="/protube-logo-removebg-preview.png" alt="ProTube" />
        </Link>
      </div>

      <form className="pt-search" role="search" onSubmit={(e) => e.preventDefault()}>
        <input
          type="search"
          className="form-control"
          placeholder="Search videos"
          aria-label="Search videos"
          value={query}
          onChange={(e) => onQueryChange(e.target.value)}
        />
        <button type="submit" className="pt-search-btn" aria-label="Search">
          <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true">
            <circle cx="11" cy="11" r="7" stroke="currentColor" strokeWidth="2" fill="none" />
            <path d="M20 20l-3.5-3.5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
          </svg>
        </button>
      </form>

      <div className="pt-navbar-end">
        <Link to="/login" className="btn btn-outline-primary rounded-pill px-3">
          Sign in
        </Link>
      </div>
    </header>
  );
};

export default Navbar;
