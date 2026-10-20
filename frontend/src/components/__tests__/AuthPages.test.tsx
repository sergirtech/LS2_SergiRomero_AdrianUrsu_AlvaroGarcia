import { act, fireEvent, render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import fetchMock from 'jest-fetch-mock';
import { MemoryRouter, Route, Routes } from 'react-router';

import { AuthProvider } from '../../useAuth';
import LoginPage from '../LoginPage';
import RegisterPage from '../RegisterPage';

// Renders and lets AuthProvider finish its initial GET /me, so no state update happens after the test.
const renderAt = async (path: string) => {
  render(
    <AuthProvider>
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
        </Routes>
      </MemoryRouter>
    </AuthProvider>
  );
  await act(async () => {});
};

describe('Auth pages', () => {
  beforeEach(() => {
    fetchMock.resetMocks();
    fetchMock.mockResponse('', { status: 401 });
  });

  it('shows the login page at /login', async () => {
    await renderAt('/login');
    expect(screen.getByRole('heading', { name: 'Sign in' })).toBeInTheDocument();
  });

  it('shows the register page at /register', async () => {
    await renderAt('/register');
    expect(screen.getByRole('heading', { name: 'Create account' })).toBeInTheDocument();
  });

  it('navigates between login and register without reloading', async () => {
    await renderAt('/login');
    fireEvent.click(screen.getByRole('link', { name: 'Create one' }));
    expect(screen.getByRole('heading', { name: 'Create account' })).toBeInTheDocument();

    fireEvent.click(screen.getByRole('link', { name: 'Sign in' }));
    expect(screen.getByRole('heading', { name: 'Sign in' })).toBeInTheDocument();
  });
});
