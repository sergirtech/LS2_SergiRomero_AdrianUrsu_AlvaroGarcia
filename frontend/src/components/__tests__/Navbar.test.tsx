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

  it('links Sign in to the login page and the logo to home', () => {
    renderNavbar();
    expect(screen.getByRole('link', { name: 'Sign in' })).toHaveAttribute('href', '/login');
    expect(screen.getByRole('link', { name: 'ProTube' })).toHaveAttribute('href', '/');
  });

  it('shows the username and Sign out when signed in', () => {
    const onSignOut = jest.fn();
    renderNavbar({ user: { id: 1, email: 'ana@mail.com', username: 'ana' }, onSignOut });

    expect(screen.getByText('ana')).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Sign in' })).not.toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Sign out' }));
    expect(onSignOut).toHaveBeenCalledTimes(1);
  });

  it('shows neither Sign in nor Sign out while the session is being checked', () => {
    renderNavbar({ authReady: false });
    expect(screen.queryByRole('link', { name: 'Sign in' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Sign out' })).not.toBeInTheDocument();
  });
});
