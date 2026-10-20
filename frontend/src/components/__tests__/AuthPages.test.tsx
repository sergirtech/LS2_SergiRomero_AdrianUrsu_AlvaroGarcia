import { fireEvent, render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import { MemoryRouter, Route, Routes } from 'react-router';

import LoginPage from '../LoginPage';
import RegisterPage from '../RegisterPage';

const renderAt = (path: string) =>
  render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
      </Routes>
    </MemoryRouter>
  );

describe('Auth pages', () => {
  it('shows the login page at /login', () => {
    renderAt('/login');
    expect(screen.getByRole('heading', { name: 'Sign in' })).toBeInTheDocument();
  });

  it('shows the register page at /register', () => {
    renderAt('/register');
    expect(screen.getByRole('heading', { name: 'Create account' })).toBeInTheDocument();
  });

  it('navigates between login and register without reloading', () => {
    renderAt('/login');
    fireEvent.click(screen.getByRole('link', { name: 'Create one' }));
    expect(screen.getByRole('heading', { name: 'Create account' })).toBeInTheDocument();

    fireEvent.click(screen.getByRole('link', { name: 'Sign in' }));
    expect(screen.getByRole('heading', { name: 'Sign in' })).toBeInTheDocument();
  });
});
