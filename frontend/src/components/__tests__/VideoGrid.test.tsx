import { fireEvent, render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import { MemoryRouter } from 'react-router';

import VideoGrid, { VideoGridSkeleton } from '../VideoGrid';

const videos = [
  { id: '0', title: 'video1', channel: 'channel1', viewCount: 1200 },
  { id: '1', title: 'video2', channel: 'channel2', viewCount: 2400 },
];

describe('VideoGrid', () => {
  it('renders correctly', () => {
    const { asFragment } = render(
      <MemoryRouter>
        <VideoGrid
          videos={[
            { id: '0', title: 'video 1 test', channel: 'channel1', viewCount: 1200 },
            { id: '1', title: 'video 2 test', channel: 'channel2', viewCount: 2400 },
          ]}
        />
      </MemoryRouter>
    );
    expect(asFragment()).toMatchSnapshot();
  });

  it('renders a card per video with its thumbnail from the media store', () => {
    render(
      <MemoryRouter>
        <VideoGrid videos={videos} />
      </MemoryRouter>
    );
    expect(screen.getAllByRole('article')).toHaveLength(2);
    expect(screen.getByRole('heading', { name: 'video1' })).toBeInTheDocument();
    expect(screen.getByAltText('video1 thumbnail')).toHaveAttribute('src', 'development/0.webp');
    expect(
      screen.getByText((_, element) => element?.tagName === 'P' && element.textContent === 'channel1·1.2K views')
    ).toBeInTheDocument();
  });

  it('shows a placeholder when the thumbnail fails to load', () => {
    render(
      <MemoryRouter>
        <VideoGrid videos={[videos[0]]} />
      </MemoryRouter>
    );
    fireEvent.error(document.querySelector('img')!);
    expect(screen.getByTestId('thumb-placeholder')).toBeInTheDocument();
  });

  it('shows an empty message when there are no videos', () => {
    render(
      <MemoryRouter>
        <VideoGrid videos={[]} />
      </MemoryRouter>
    );
    expect(screen.getByText('No videos found.')).toBeInTheDocument();
  });

  it('renders loading skeleton cards', () => {
    render(
      <MemoryRouter>
        <VideoGridSkeleton count={3} />
      </MemoryRouter>
    );
    expect(screen.getByLabelText('Loading videos').children).toHaveLength(3);
  });
});
