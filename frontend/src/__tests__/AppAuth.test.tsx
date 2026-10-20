import { act, fireEvent, render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import fetchMock from 'jest-fetch-mock';
import { MemoryRouter } from 'react-router';

import App from '../App';
import { AuthProvider } from '../useAuth';

// Jest has no CSS transform (frontend/AGENTS.md), so App's stylesheet is replaced by an empty module.
jest.mock('../App.css', () => ({}));

// The video list uses axios; it's not part of these tests.
jest.mock('../useAllVideos', () => ({
  useAllVideos: () => ({ loading: 'success', message: '', value: [] }),
}));

const ana = { id: 1, email: 'ana@mail.com', username: 'ana' };

// Renders and lets AuthProvider finish its initial GET /me, so no state update happens after the test.
const renderApp = async (path: string) => {
  render(
    <AuthProvider>
      <MemoryRouter initialEntries={[path]}>
        <App />
      </MemoryRouter>
    </AuthProvider>
  );
  await act(async () => {});
};

describe('App authentication', () => {
  beforeEach(() => {
    fetchMock.resetMocks();
  });

  it('shows the user in the Navbar after logging in (L1)', async () => {
    fetchMock.mockResponseOnce('', { status: 401 }).mockResponseOnce(JSON.stringify(ana));
    await renderApp('/login');

    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'ana@mail.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'secret123' } });
    fireEvent.click(screen.getByRole('button', { name: 'Sign in' }));

    expect(await screen.findByRole('button', { name: 'Sign out' })).toBeInTheDocument();
    expect(screen.getByText('ana')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Recommended' })).toBeInTheDocument();
  });

  it('shows the new user in the Navbar after registering (R1)', async () => {
    fetchMock.mockResponseOnce('', { status: 401 }).mockResponseOnce(JSON.stringify(ana), { status: 201 });
    await renderApp('/register');

    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'ana@mail.com' } });
    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'ana' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'secret123' } });
    fireEvent.click(screen.getByRole('button', { name: 'Create account' }));

    expect(await screen.findByRole('button', { name: 'Sign out' })).toBeInTheDocument();
    expect(screen.getByText('ana')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Recommended' })).toBeInTheDocument();
  });

  it('signs out and shows Sign in again (L3)', async () => {
    fetchMock.mockResponseOnce(JSON.stringify(ana)).mockResponseOnce('', { status: 204 });
    await renderApp('/');

    fireEvent.click(screen.getByRole('button', { name: 'Sign out' }));

    expect(await screen.findByRole('link', { name: 'Sign in' })).toBeInTheDocument();
    expect(screen.queryByText('ana')).not.toBeInTheDocument();
    expect(fetchMock.mock.calls[1][0]).toBe('other/auth/logout');
  });

  it('stays signed in when the server cannot be reached on sign out', async () => {
    fetchMock.mockResponseOnce(JSON.stringify(ana)).mockRejectOnce(new Error('offline'));
    await renderApp('/');

    fireEvent.click(screen.getByRole('button', { name: 'Sign out' }));
    await act(async () => {});

    expect(screen.getByRole('button', { name: 'Sign out' })).toBeInTheDocument();
  });
});
