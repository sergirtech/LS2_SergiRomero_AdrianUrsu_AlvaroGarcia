import { fireEvent, render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import { MemoryRouter } from 'react-router';
import ProfileCommentsManager from '../ProfileCommentsManager';

describe('ProfileCommentsManager', () => {
  it('lists the signed-in user comments with video links and controls', () => {
    render(
      <MemoryRouter>
        <ProfileCommentsManager
          comments={[
            { id: 8, videoId: 3, videoTitle: 'Video title', text: 'My comment', createdAt: '2026-11-16T08:00:00Z' },
          ]}
          loading={false}
          error={null}
          onRetry={jest.fn()}
          onEdit={jest.fn().mockResolvedValue(undefined)}
          onDelete={jest.fn().mockResolvedValue(undefined)}
        />
      </MemoryRouter>
    );

    expect(screen.getByRole('link', { name: 'Video title' })).toHaveAttribute('href', '/watch/3');
    expect(screen.getByText('My comment')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Edit' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Delete' })).toBeInTheDocument();
  });

  it('offers retry after an API error', () => {
    const onRetry = jest.fn();
    render(
      <MemoryRouter>
        <ProfileCommentsManager
          comments={[]}
          loading={false}
          error="offline"
          onRetry={onRetry}
          onEdit={jest.fn()}
          onDelete={jest.fn()}
        />
      </MemoryRouter>
    );

    fireEvent.click(screen.getByRole('button', { name: 'Retry' }));
    expect(onRetry).toHaveBeenCalledTimes(1);
  });
});
