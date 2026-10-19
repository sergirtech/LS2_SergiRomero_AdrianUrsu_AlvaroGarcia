import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';

import Sidebar from '../Sidebar';

describe('Sidebar', () => {
  it('renders the navigation items with Home active', () => {
    render(<Sidebar open />);
    expect(screen.getAllByRole('link')).toHaveLength(4);
    expect(screen.getByRole('link', { name: 'Home' })).toHaveClass('active');
  });

  it('collapses when closed', () => {
    render(<Sidebar open={false} />);
    expect(screen.getByRole('navigation')).toHaveClass('pt-sidebar-collapsed');
  });
});
