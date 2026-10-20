import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import fetchMock from 'jest-fetch-mock';
import { MemoryRouter, Route, Routes } from 'react-router';

import { AuthProvider } from '../../useAuth';
import RegisterPage from '../RegisterPage';

const ana = { id: 1, email: 'ana@mail.com', username: 'ana' };

// Renders and lets AuthProvider finish its initial GET /me, so no state update happens after the test.
const renderRegister = async () => {
  render(
    <AuthProvider>
      <MemoryRouter initialEntries={['/register']}>
        <Routes>
          <Route path="/" element={<p>Home page</p>} />
          <Route path="/register" element={<RegisterPage />} />
        </Routes>
      </MemoryRouter>
    </AuthProvider>
  );
  await act(async () => {});
};

const fill = (email: string, username: string, password: string) => {
  fireEvent.change(screen.getByLabelText('Email'), { target: { value: email } });
  fireEvent.change(screen.getByLabelText('Username'), { target: { value: username } });
  fireEvent.change(screen.getByLabelText('Password'), { target: { value: password } });
  fireEvent.click(screen.getByRole('button', { name: 'Create account' }));
};

// Calls after the initial GET /me made by AuthProvider.
const registerCalls = () => fetchMock.mock.calls.filter(([url]) => url === 'other/auth/register');

describe('RegisterPage', () => {
  beforeEach(() => {
    fetchMock.resetMocks();
    fetchMock.mockResponseOnce('', { status: 401 });
  });

  it('creates the account and goes to the home page (R1)', async () => {
    fetchMock.mockResponseOnce(JSON.stringify(ana), { status: 201 });
    await renderRegister();

    fill('  ana@mail.com ', 'ana', 'secret123');

    expect(await screen.findByText('Home page')).toBeInTheDocument();
    const [, init] = registerCalls()[0];
    expect(JSON.parse(init!.body as string)).toEqual({ email: 'ana@mail.com', username: 'ana', password: 'secret123' });
  });

  it('shows required errors and does not call the API with an empty form (R3)', async () => {
    await renderRegister();
    fill('', '', '');

    expect(screen.getByText('Email is required')).toBeInTheDocument();
    expect(screen.getByText('Username is required')).toBeInTheDocument();
    expect(screen.getByText('Password is required')).toBeInTheDocument();
    expect(registerCalls()).toHaveLength(0);
  });

  it('rejects an invalid email, short username and short password (R3)', async () => {
    await renderRegister();
    fill('ana@mail', 'an', 'short');

    expect(screen.getByText('Enter a valid email address')).toBeInTheDocument();
    expect(screen.getByText('Username must be between 3 and 30 characters')).toBeInTheDocument();
    expect(screen.getByText('Password must be between 8 and 72 characters')).toBeInTheDocument();
    expect(screen.getByLabelText('Email')).toHaveAttribute('aria-invalid', 'true');
    expect(registerCalls()).toHaveLength(0);
  });

  it('clears a field error when the user edits that field', async () => {
    await renderRegister();
    fill('', 'ana', 'secret123');
    expect(screen.getByText('Email is required')).toBeInTheDocument();

    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'a' } });

    expect(screen.queryByText('Email is required')).not.toBeInTheDocument();
  });

  it('shows the duplicate account error from the server (R2)', async () => {
    fetchMock.mockResponseOnce(JSON.stringify({ message: 'This email is already registered' }), { status: 409 });
    await renderRegister();

    fill('ana@mail.com', 'ana', 'secret123');

    expect(await screen.findByRole('alert')).toHaveTextContent('This email is already registered');
    expect(screen.queryByText('Home page')).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Create account' })).toBeEnabled();
  });

  it('shows field errors returned by the server (R3)', async () => {
    fetchMock.mockResponseOnce(
      JSON.stringify({ message: 'Invalid data', errors: { email: 'Enter a valid email address' } }),
      { status: 400 }
    );
    await renderRegister();

    fill('ana@mail.com', 'ana', 'secret123');

    expect(await screen.findByText('Enter a valid email address')).toBeInTheDocument();
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });

  it('disables the button while the request is in progress', async () => {
    fetchMock.mockResponseOnce(() => new Promise(() => {}));
    await renderRegister();

    fill('ana@mail.com', 'ana', 'secret123');

    await waitFor(() => expect(screen.getByRole('button', { name: 'Creating account...' })).toBeDisabled());
  });

  it('shows a generic error for unexpected failures', async () => {
    fetchMock.mockResponseOnce('not json', { status: 201 });
    await renderRegister();

    fill('ana@mail.com', 'ana', 'secret123');

    expect(await screen.findByRole('alert')).toHaveTextContent('Something went wrong. Try again.');
  });
});
