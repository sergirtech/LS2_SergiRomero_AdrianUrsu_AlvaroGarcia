import { act, renderHook, waitFor } from '@testing-library/react';
import fetchMock from 'jest-fetch-mock';
import type { ReactNode } from 'react';

import { AuthError, AuthProvider, useAuth } from '../useAuth';

const ana = { id: 1, email: 'ana@mail.com', username: 'ana' };

const wrapper = ({ children }: { children: ReactNode }) => <AuthProvider>{children}</AuthProvider>;

// Renders the hook and waits until the initial GET /me has finished.
const renderAuth = async () => {
  const hook = renderHook(() => useAuth(), { wrapper });
  await waitFor(() => expect(hook.result.current.status).toBe('ready'));
  return hook;
};

describe('useAuth', () => {
  beforeEach(() => {
    fetchMock.resetMocks();
  });

  it('restores the signed-in user from the session', async () => {
    fetchMock.mockResponseOnce(JSON.stringify(ana));
    const { result } = await renderAuth();
    expect(result.current.user).toEqual(ana);
    expect(fetchMock).toHaveBeenCalledWith(
      'other/auth/me',
      expect.objectContaining({ method: 'GET', credentials: 'include' })
    );
  });

  it('starts signed out when there is no session', async () => {
    fetchMock.mockResponseOnce('', { status: 401 });
    const { result } = await renderAuth();
    expect(result.current.user).toBeNull();
  });

  it('starts signed out when the server is unreachable', async () => {
    fetchMock.mockRejectOnce(new Error('offline'));
    const { result } = await renderAuth();
    expect(result.current.user).toBeNull();
  });

  it('registers and signs the new user in', async () => {
    fetchMock.mockResponseOnce('', { status: 401 }).mockResponseOnce(JSON.stringify(ana), { status: 201 });
    const { result } = await renderAuth();

    await act(() => result.current.register({ email: 'ana@mail.com', username: 'ana', password: 'secret123' }));

    expect(result.current.user).toEqual(ana);
    const [url, init] = fetchMock.mock.calls[1];
    expect(url).toBe('other/auth/register');
    expect(init).toMatchObject({ method: 'POST', credentials: 'include' });
    expect(JSON.parse(init!.body as string)).toEqual({ email: 'ana@mail.com', username: 'ana', password: 'secret123' });
  });

  it('exposes field errors when registration data is invalid', async () => {
    fetchMock
      .mockResponseOnce('', { status: 401 })
      .mockResponseOnce(JSON.stringify({ message: 'Invalid data', errors: { email: 'Enter a valid email address' } }), {
        status: 400,
      });
    const { result } = await renderAuth();

    const error = await result.current.register({ email: 'x', username: 'ana', password: 'secret123' }).catch((e) => e);

    expect(error).toBeInstanceOf(AuthError);
    expect(error).toMatchObject({ status: 400, fieldErrors: { email: 'Enter a valid email address' } });
    expect(result.current.user).toBeNull();
  });

  it('reports a duplicate user with status 409', async () => {
    fetchMock
      .mockResponseOnce('', { status: 401 })
      .mockResponseOnce(JSON.stringify({ message: 'Email already registered' }), { status: 409 });
    const { result } = await renderAuth();

    await expect(
      result.current.register({ email: 'ana@mail.com', username: 'ana', password: 'secret123' })
    ).rejects.toMatchObject({ status: 409, message: 'Email already registered', fieldErrors: {} });
  });

  it('logs in with valid credentials', async () => {
    fetchMock.mockResponseOnce('', { status: 401 }).mockResponseOnce(JSON.stringify(ana));
    const { result } = await renderAuth();

    await act(() => result.current.login('ana@mail.com', 'secret123'));

    expect(result.current.user).toEqual(ana);
    expect(fetchMock.mock.calls[1][0]).toBe('other/auth/login');
  });

  it('rejects invalid credentials with status 401', async () => {
    fetchMock
      .mockResponseOnce('', { status: 401 })
      .mockResponseOnce(JSON.stringify({ message: 'Incorrect email or password' }), { status: 401 });
    const { result } = await renderAuth();

    await expect(result.current.login('ana@mail.com', 'wrong')).rejects.toMatchObject({
      status: 401,
      message: 'Incorrect email or password',
    });
    expect(result.current.user).toBeNull();
  });

  it('uses a generic message when the error has no body', async () => {
    fetchMock.mockResponseOnce('', { status: 401 }).mockResponseOnce('', { status: 500 });
    const { result } = await renderAuth();

    await expect(result.current.login('ana@mail.com', 'secret123')).rejects.toMatchObject({
      status: 500,
      message: 'Something went wrong. Try again.',
    });
  });

  it('reports a network failure with status 0', async () => {
    fetchMock.mockResponseOnce('', { status: 401 }).mockRejectOnce(new Error('offline'));
    const { result } = await renderAuth();

    await expect(result.current.login('ana@mail.com', 'secret123')).rejects.toMatchObject({ status: 0 });
  });

  it('logs out and clears the user', async () => {
    fetchMock.mockResponseOnce(JSON.stringify(ana)).mockResponseOnce('', { status: 204 });
    const { result } = await renderAuth();

    await act(() => result.current.logout());

    expect(result.current.user).toBeNull();
    expect(fetchMock.mock.calls[1]).toEqual(['other/auth/logout', expect.objectContaining({ method: 'POST' })]);
  });

  it('fails when used outside an AuthProvider', () => {
    jest.spyOn(console, 'error').mockImplementation(() => {});
    expect(() => renderHook(() => useAuth())).toThrow('useAuth must be used inside an AuthProvider');
  });
});
