import { fireEvent, render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';

import VideoGrid, { VideoGridSkeleton } from '../VideoGrid';

describe('VideoGrid', () => {
  it('renders correctly', () => {
    const { asFragment } = render(<VideoGrid videos={['video 1 test', 'video 2 test']} />);
    expect(asFragment()).toMatchSnapshot();
  });

  it('renders a card per video with its thumbnail from the media store', () => {
    render(<VideoGrid videos={['video1', 'video2']} />);
    expect(screen.getAllByRole('article')).toHaveLength(2);
    expect(screen.getByRole('heading', { name: 'video1' })).toBeInTheDocument();
    expect(document.querySelector('img')).toHaveAttribute('src', 'development/video1.webp');
  });

  it('shows a placeholder when the thumbnail fails to load', () => {
    render(<VideoGrid videos={['video1']} />);
    fireEvent.error(document.querySelector('img')!);
    expect(screen.getByTestId('thumb-placeholder')).toBeInTheDocument();
  });

  it('shows an empty message when there are no videos', () => {
    render(<VideoGrid videos={[]} />);
    expect(screen.getByText('No videos found.')).toBeInTheDocument();
  });

  it('renders loading skeleton cards', () => {
    render(<VideoGridSkeleton count={3} />);
    expect(screen.getByLabelText('Loading videos').children).toHaveLength(3);
  });
});
