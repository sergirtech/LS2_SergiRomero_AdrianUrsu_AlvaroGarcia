import { fireEvent, render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import { MemoryRouter } from 'react-router';

import Navbar from '../Navbar';

const renderNavbar = (props: Partial<Parameters<typeof Navbar>[0]> = {}) =>
  render(
    <MemoryRouter>
      <Navbar
        query=""
        onQueryChange={jest.fn()}
        onToggleSidebar={jest.fn()}
        user={null}
        authReady
        onSignOut={jest.fn()}
        darkMode={false}
        onDarkModeChange={jest.fn()}
        {...props}
      />
    </MemoryRouter>
  );

describe('Navbar', () => {
  it('reports search input changes', () => {
    const onQueryChange = jest.fn();
    renderNavbar({ onQueryChange });
    fireEvent.change(screen.getByLabelText('Search videos'), { target: { value: 'cats' } });
    expect(onQueryChange).toHaveBeenCalledWith('cats');
  });

  it('does not reload the page on search submit', () => {
    renderNavbar({ query: 'cats' });
    const event = new Event('submit', { bubbles: true, cancelable: true });
    screen.getByRole('search').dispatchEvent(event);
    expect(event.defaultPrevented).toBe(true);
  });

  it('toggles the sidebar from the menu button', () => {
    const onToggleSidebar = jest.fn();
    renderNavbar({ onToggleSidebar });
    fireEvent.click(screen.getByLabelText('Toggle menu'));
    expect(onToggleSidebar).toHaveBeenCalledTimes(1);
  });

  it('links the logo to home and shows Sign in in the user access panel', () => {
    renderNavbar();
    expect(screen.getByRole('link', { name: 'Tube Enjoyers' })).toHaveAttribute('href', '/');

    fireEvent.click(screen.getByRole('button', { name: 'User access' }));
    expect(screen.getByRole('link', { name: 'Sign in' })).toHaveAttribute('href', '/login');
  });

  it('shows the username and Sign out in the user access panel when signed in', () => {
    const onSignOut = jest.fn();
    renderNavbar({ user: { id: 1, email: 'ana@mail.com', username: 'ana' }, onSignOut });

    fireEvent.click(screen.getByRole('button', { name: 'User access' }));
    expect(screen.getByText('ana')).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Sign in' })).not.toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Sign out' }));
    expect(onSignOut).toHaveBeenCalledTimes(1);
  });

  it('does not show account actions while the session is being checked', () => {
    renderNavbar({ authReady: false });
    fireEvent.click(screen.getByRole('button', { name: 'User access' }));
    expect(screen.getByText('Checking session...')).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Sign in' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Sign out' })).not.toBeInTheDocument();
  });

  it('opens display settings and reports dark mode changes', () => {
    const onDarkModeChange = jest.fn();
    renderNavbar({ onDarkModeChange });

    fireEvent.click(screen.getByRole('button', { name: 'Settings' }));
    expect(screen.getByRole('region', { name: 'Display settings' })).toBeInTheDocument();
    fireEvent.click(screen.getByRole('switch', { name: 'Dark mode' }));

    expect(onDarkModeChange).toHaveBeenCalledWith(true);
  });
});
