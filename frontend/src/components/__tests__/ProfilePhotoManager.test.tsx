import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import ProfilePhotoManager from '../ProfilePhotoManager';

describe('ProfilePhotoManager', () => {
  it('uploads selected images and allows removal of an existing photo', async () => {
    const onUpload = jest.fn(async () => true);
    const onDelete = jest.fn(async () => true);
    const { rerender } = render(
      <ProfilePhotoManager
        photoUrl={null}
        loading={false}
        saving={false}
        error={null}
        onUpload={onUpload}
        onDelete={onDelete}
      />
    );

    fireEvent.change(screen.getByLabelText(/Profile photo/), {
      target: { files: [new File(['image'], 'avatar.png', { type: 'image/png' })] },
    });
    await waitFor(() => expect(onUpload).toHaveBeenCalledWith(expect.objectContaining({ name: 'avatar.png' })));

    rerender(
      <ProfilePhotoManager
        photoUrl="blob:photo"
        loading={false}
        saving={false}
        error="Could not load"
        onUpload={onUpload}
        onDelete={onDelete}
      />
    );
    expect(screen.getByRole('alert')).toHaveTextContent('Could not load');
    fireEvent.click(screen.getByRole('button', { name: 'Remove profile photo' }));
    expect(onDelete).toHaveBeenCalled();
  });
});
