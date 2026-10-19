import { fireEvent, render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';

import Navbar from '../Navbar';

describe('Navbar', () => {
  it('reports search input changes', () => {
    const onQueryChange = jest.fn();
    render(<Navbar query="" onQueryChange={onQueryChange} onToggleSidebar={jest.fn()} />);
    fireEvent.change(screen.getByLabelText('Search videos'), { target: { value: 'cats' } });
    expect(onQueryChange).toHaveBeenCalledWith('cats');
  });

  it('does not reload the page on search submit', () => {
    render(<Navbar query="cats" onQueryChange={jest.fn()} onToggleSidebar={jest.fn()} />);
    const event = new Event('submit', { bubbles: true, cancelable: true });
    screen.getByRole('search').dispatchEvent(event);
    expect(event.defaultPrevented).toBe(true);
  });

  it('toggles the sidebar from the menu button', () => {
    const onToggleSidebar = jest.fn();
    render(<Navbar query="" onQueryChange={jest.fn()} onToggleSidebar={onToggleSidebar} />);
    fireEvent.click(screen.getByLabelText('Toggle menu'));
    expect(onToggleSidebar).toHaveBeenCalledTimes(1);
  });
});
