import { fireEvent, render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import { MemoryRouter, Route, Routes } from 'react-router';

import ProfilePage from '../ProfilePage';

describe('ProfilePage', () => {
  const renderProfile = (path = '/profile') =>
    render(
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route
            path="/profile/*"
            element={
              <ProfilePage
                user={{ id: 1, email: 'ana@example.com', username: 'ana' }}
                stats={{ lists: 5, followers: 12 }}
                videos={[]}
                lists={[]}
                listsLoading={false}
                listsError={null}
                onCreateList={jest.fn()}
                onAddVideo={jest.fn()}
                onRemoveVideo={jest.fn()}
                onDeleteList={jest.fn()}
              />
            }
          />
        </Routes>
      </MemoryRouter>
    );

  it('shows the signed-in user and profile statistics', () => {
    renderProfile();

    expect(screen.getByRole('heading', { name: 'ana' })).toBeInTheDocument();
    expect(screen.getByText('ana@example.com')).toBeInTheDocument();
    expect(screen.getByLabelText('Channel statistics')).toHaveTextContent('5');
    expect(screen.getByLabelText('Channel statistics')).toHaveTextContent('12');
    expect(screen.getByRole('link', { name: 'My profile' })).toHaveAttribute('href', '/profile');
  });

  it('navigates to a placeholder screen from a profile shortcut', () => {
    renderProfile();

    fireEvent.click(screen.getByRole('link', { name: 'Playback history' }));

    expect(screen.getByRole('heading', { name: 'Playback history' })).toBeInTheDocument();
    expect(screen.getByText('This page will be available soon.')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Back to my profile' })).toHaveAttribute('href', '/profile');
  });

  it('opens the lists and configuration sections', () => {
    renderProfile('/profile/lists');

    expect(screen.getByRole('heading', { name: 'Lists' })).toBeInTheDocument();
    fireEvent.click(screen.getByRole('link', { name: 'Configuration' }));
    expect(screen.getByRole('heading', { name: 'Configuration' })).toBeInTheDocument();
  });

  it('shows a sign-in prompt when no user is available', () => {
    render(
      <MemoryRouter>
        <ProfilePage
          user={null}
          stats={{ lists: 0, followers: 0 }}
          videos={[]}
          lists={[]}
          listsLoading={false}
          listsError={null}
          onCreateList={jest.fn()}
          onAddVideo={jest.fn()}
          onRemoveVideo={jest.fn()}
          onDeleteList={jest.fn()}
        />
      </MemoryRouter>
    );

    expect(screen.getByRole('heading', { name: 'Your profile' })).toBeInTheDocument();
    expect(screen.getByText('Sign in to see your profile information')).toBeInTheDocument();
  });
});
