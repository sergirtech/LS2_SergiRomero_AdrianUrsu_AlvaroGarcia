import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router';
import { AuthError, useAuth } from '../useAuth';

interface LoginData {
  email: string;
  password: string;
}

type FieldErrors = Partial<Record<keyof LoginData, string>>;

// Only "required" checks: length rules belong to registration, and a wrong password must just fail with 401.
const validateLogin = ({ email, password }: LoginData): FieldErrors => {
  const errors: FieldErrors = {};
  if (!email) {
    errors.email = 'Email is required';
  }
  if (!password) {
    errors.password = 'Password is required';
  }
  return errors;
};

const LoginPage = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState<LoginData>({ email: '', password: '' });
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleChange = (field: keyof LoginData, value: string) => {
    setForm((current) => ({ ...current, [field]: value }));
    setFieldErrors((current) => ({ ...current, [field]: undefined }));
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const data = { ...form, email: form.email.trim() };
    const errors = validateLogin(data);
    setFieldErrors(errors);
    setFormError('');
    if (Object.keys(errors).length > 0) {
      return;
    }

    setSubmitting(true);
    try {
      await login(data.email, data.password);
      navigate('/');
    } catch (error) {
      // 401 keeps the backend's generic message, so it doesn't reveal whether the email exists.
      setFormError(error instanceof AuthError ? error.message : 'Something went wrong. Try again.');
      setForm((current) => ({ ...current, password: '' }));
      setSubmitting(false);
    }
  };

  const field = (name: keyof LoginData, label: string, type: string, autoComplete: string) => (
    <div className="mb-3">
      <label htmlFor={`login-${name}`} className="form-label">
        {label}
      </label>
      <input
        id={`login-${name}`}
        type={type}
        autoComplete={autoComplete}
        className={`form-control${fieldErrors[name] ? ' is-invalid' : ''}`}
        value={form[name]}
        onChange={(e) => handleChange(name, e.target.value)}
        aria-invalid={Boolean(fieldErrors[name])}
        aria-describedby={fieldErrors[name] ? `login-${name}-error` : undefined}
      />
      {fieldErrors[name] && (
        <div id={`login-${name}-error`} className="invalid-feedback">
          {fieldErrors[name]}
        </div>
      )}
    </div>
  );

  return (
    <section className="pt-auth">
      <h2 className="pt-section-title">Sign in</h2>
      <form onSubmit={handleSubmit} noValidate>
        {formError && (
          <div className="alert alert-danger" role="alert">
            {formError}
          </div>
        )}
        {field('email', 'Email', 'email', 'email')}
        {field('password', 'Password', 'password', 'current-password')}
        <button type="submit" className="btn btn-primary w-100" disabled={submitting}>
          {submitting ? 'Signing in...' : 'Sign in'}
        </button>
      </form>
      <p className="mt-3">
        Don't have an account? <Link to="/register">Create one</Link>
      </p>
    </section>
  );
};

export default LoginPage;
