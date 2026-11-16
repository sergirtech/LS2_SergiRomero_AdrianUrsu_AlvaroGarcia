import { fireEvent, render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import { MemoryRouter, Route, Routes } from 'react-router';

import Sidebar from '../Sidebar';

const renderSidebar = (path = '/', open = true) =>
  render(
    <MemoryRouter initialEntries={[path]}>
      <Sidebar open={open} />
      <Routes>
        <Route path="/" element={<p>Home page</p>} />
        <Route path="/watch/:videoId" element={<p>Watch page</p>} />
        <Route path="/upload" element={<p>Upload page</p>} />
      </Routes>
    </MemoryRouter>
  );

describe('Sidebar', () => {
  it('links every item to its page', () => {
    renderSidebar();
    expect(screen.getByRole('link', { name: 'Home' })).toHaveAttribute('href', '/');
    expect(screen.getByRole('link', { name: 'Upload' })).toHaveAttribute('href', '/upload');
    expect(screen.getByRole('link', { name: 'My videos' })).toHaveAttribute('href', '/my-videos');
    expect(screen.getByRole('link', { name: 'History' })).toHaveAttribute('href', '/profile/history');
  });

  it('marks only the current page as active', () => {
    renderSidebar('/');
    expect(screen.getByRole('link', { name: 'Home' })).toHaveClass('active');
    expect(screen.getByRole('link', { name: 'Upload' })).not.toHaveClass('active');
  });

  it('takes the user back home from any other page', () => {
    renderSidebar('/watch/5');
    const home = screen.getByRole('link', { name: 'Home' });
    expect(home).not.toHaveClass('active');

    fireEvent.click(home);

    expect(screen.getByText('Home page')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Home' })).toHaveClass('active');
  });

  it('opens another section when clicked', () => {
    renderSidebar('/');

    fireEvent.click(screen.getByRole('link', { name: 'Upload' }));

    expect(screen.getByText('Upload page')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Upload' })).toHaveClass('active');
  });

  it('collapses when closed', () => {
    renderSidebar('/', false);
    expect(screen.getByRole('navigation')).toHaveClass('pt-sidebar-collapsed');
  });
});
