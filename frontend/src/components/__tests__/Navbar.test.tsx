import { fireEvent, render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import { MemoryRouter } from 'react-router';

import Navbar from '../Navbar';

const renderNavbar = (props: Partial<Parameters<typeof Navbar>[0]> = {}) =>
  render(
    <MemoryRouter>
      <Navbar query="" onQueryChange={jest.fn()} onToggleSidebar={jest.fn()} {...props} />
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
});
