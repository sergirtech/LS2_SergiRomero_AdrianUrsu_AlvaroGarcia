import { act, renderHook, waitFor } from '@testing-library/react';
import fetchMock from 'jest-fetch-mock';
import { useProfilePhoto } from '../useProfilePhoto';

describe('useProfilePhoto', () => {
  const createObjectURL = jest.fn(() => 'blob:profile-photo');
  const revokeObjectURL = jest.fn();

  beforeAll(() => {
    Object.defineProperty(URL, 'createObjectURL', { configurable: true, value: createObjectURL });
    Object.defineProperty(URL, 'revokeObjectURL', { configurable: true, value: revokeObjectURL });
  });

  beforeEach(() => {
    fetchMock.resetMocks();
    createObjectURL.mockClear();
    revokeObjectURL.mockClear();
  });

  it('uses the default photo when none is stored and ignores signed-out users', async () => {
    fetchMock.mockResponseOnce('', { status: 404 });
    const { result, rerender } = renderHook(({ userId, enabled }) => useProfilePhoto(userId, enabled), {
      initialProps: { userId: 1 as number | null, enabled: true },
    });

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.photoUrl).toBeNull();
    rerender({ userId: null, enabled: false });
    expect(fetchMock).toHaveBeenCalledTimes(1);
  });

  it('uploads a supported image and refreshes the displayed photo', async () => {
    fetchMock.mockResponseOnce('', { status: 404 });
    fetchMock.mockResponseOnce('', { status: 204 });
    fetchMock.mockResponseOnce('image bytes', { status: 200, headers: { 'Content-Type': 'image/png' } });
    const { result } = renderHook(() => useProfilePhoto(1, true));
    await waitFor(() => expect(result.current.loading).toBe(false));

    let uploaded = false;
    await act(async () => {
      uploaded = await result.current.upload(new File(['image'], 'avatar.png', { type: 'image/png' }));
    });

    expect(uploaded).toBe(true);
    await waitFor(() => expect(result.current.photoUrl).toBe('blob:profile-photo'));
    expect(fetchMock.mock.calls[1][1]).toEqual(expect.objectContaining({ method: 'PUT', credentials: 'include' }));
    expect(createObjectURL).toHaveBeenCalledTimes(1);
  });

  it('rejects invalid files and reports API failures without a success fallback', async () => {
    fetchMock.mockResponseOnce('', { status: 404 });
    const { result } = renderHook(() => useProfilePhoto(1, true));
    await waitFor(() => expect(result.current.loading).toBe(false));

    let uploaded = true;
    await act(async () => {
      uploaded = await result.current.upload(new File(['bad'], 'avatar.gif', { type: 'image/gif' }));
    });
    expect(uploaded).toBe(false);
    expect(result.current.error).toContain('PNG, JPG, JPEG, or SVG');
    expect(fetchMock).toHaveBeenCalledTimes(1);

    fetchMock.mockResponseOnce('', { status: 500 });
    await act(async () => {
      uploaded = await result.current.upload(new File(['image'], 'avatar.jpg', { type: 'image/jpeg' }));
    });
    expect(uploaded).toBe(false);
    expect(result.current.error).toContain('HTTP 500');
  });

  it('deletes the photo and reports delete failures', async () => {
    fetchMock.mockResponseOnce('', { status: 404 });
    const { result } = renderHook(() => useProfilePhoto(1, true));
    await waitFor(() => expect(result.current.loading).toBe(false));
    fetchMock.mockResponseOnce('', { status: 204 });

    let deleted = false;
    await act(async () => {
      deleted = await result.current.remove();
    });
    expect(deleted).toBe(true);
    expect(fetchMock.mock.calls[fetchMock.mock.calls.length - 1][1]).toEqual(
      expect.objectContaining({ method: 'DELETE' })
    );

    fetchMock.mockResponseOnce('', { status: 500 });
    await act(async () => {
      deleted = await result.current.remove();
    });
    expect(deleted).toBe(false);
    expect(result.current.error).toContain('HTTP 500');
  });
});
