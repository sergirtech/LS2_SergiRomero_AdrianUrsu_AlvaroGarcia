import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import fetchMock from 'jest-fetch-mock';
import { MemoryRouter, Route, Routes } from 'react-router';

import { AuthProvider } from '../../useAuth';
import LoginPage from '../LoginPage';

const ana = { id: 1, email: 'ana@mail.com', username: 'ana' };

// Renders and lets AuthProvider finish its initial GET /me, so no state update happens after the test.
const renderLogin = async () => {
  render(
    <AuthProvider>
      <MemoryRouter initialEntries={['/login']}>
        <Routes>
          <Route path="/" element={<p>Home page</p>} />
          <Route path="/login" element={<LoginPage />} />
        </Routes>
      </MemoryRouter>
    </AuthProvider>
  );
  await act(async () => {});
};

const submit = (email: string, password: string) => {
  fireEvent.change(screen.getByLabelText('Email'), { target: { value: email } });
  fireEvent.change(screen.getByLabelText('Password'), { target: { value: password } });
  fireEvent.click(screen.getByRole('button', { name: 'Sign in' }));
};

const loginCalls = () => fetchMock.mock.calls.filter(([url]) => url === 'other/auth/login');

describe('LoginPage', () => {
  beforeEach(() => {
    fetchMock.resetMocks();
    fetchMock.mockResponseOnce('', { status: 401 });
  });

  it('signs in and goes to the home page (L1)', async () => {
    fetchMock.mockResponseOnce(JSON.stringify(ana));
    await renderLogin();

    submit(' ana@mail.com  ', 'secret123');

    expect(await screen.findByText('Home page')).toBeInTheDocument();
    const [, init] = loginCalls()[0];
    expect(JSON.parse(init!.body as string)).toEqual({ email: 'ana@mail.com', password: 'secret123' });
  });

  it('shows the error and stays on the page with wrong credentials (L2)', async () => {
    fetchMock.mockResponseOnce(JSON.stringify({ message: 'Incorrect email or password' }), { status: 401 });
    await renderLogin();

    submit('ana@mail.com', 'wrong-password');

    expect(await screen.findByRole('alert')).toHaveTextContent('Incorrect email or password');
    expect(screen.queryByText('Home page')).not.toBeInTheDocument();
    expect(screen.getByLabelText('Password')).toHaveValue('');
    expect(screen.getByLabelText('Email')).toHaveValue('ana@mail.com');
    expect(screen.getByRole('button', { name: 'Sign in' })).toBeEnabled();
  });

  it('requires email and password without calling the API', async () => {
    await renderLogin();

    submit('   ', '');

    expect(screen.getByText('Email is required')).toBeInTheDocument();
    expect(screen.getByText('Password is required')).toBeInTheDocument();
    expect(loginCalls()).toHaveLength(0);
  });

  it('clears a field error when the user edits that field', async () => {
    await renderLogin();
    submit('', 'secret123');

    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'a' } });

    expect(screen.queryByText('Email is required')).not.toBeInTheDocument();
  });

  it('disables the button while the request is in progress', async () => {
    fetchMock.mockResponseOnce(() => new Promise(() => {}));
    await renderLogin();

    submit('ana@mail.com', 'secret123');

    await waitFor(() => expect(screen.getByRole('button', { name: 'Signing in...' })).toBeDisabled());
  });

  it('shows a generic error for unexpected failures', async () => {
    fetchMock.mockResponseOnce('not json');
    await renderLogin();

    submit('ana@mail.com', 'secret123');

    expect(await screen.findByRole('alert')).toHaveTextContent('Something went wrong. Try again.');
  });
});
