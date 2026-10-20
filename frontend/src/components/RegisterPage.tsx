import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router';
import { AuthError, useAuth, type RegisterData } from '../useAuth';

type FieldErrors = Partial<Record<keyof RegisterData, string>>;

// Same rules as RegisterRequest in the backend, so most errors show up before sending the form.
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const validateRegister = ({ email, username, password }: RegisterData): FieldErrors => {
  const errors: FieldErrors = {};
  if (!email) {
    errors.email = 'Email is required';
  } else if (!EMAIL_PATTERN.test(email)) {
    errors.email = 'Enter a valid email address';
  }
  if (!username.trim()) {
    errors.username = 'Username is required';
  } else if (username.length < 3 || username.length > 30) {
    errors.username = 'Username must be between 3 and 30 characters';
  }
  if (!password) {
    errors.password = 'Password is required';
  } else if (password.length < 8 || password.length > 72) {
    errors.password = 'Password must be between 8 and 72 characters';
  }
  return errors;
};

const RegisterPage = () => {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState<RegisterData>({ email: '', username: '', password: '' });
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});
  const [formError, setFormError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleChange = (field: keyof RegisterData, value: string) => {
    setForm((current) => ({ ...current, [field]: value }));
    setFieldErrors((current) => ({ ...current, [field]: undefined }));
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    // The backend rejects emails with surrounding spaces (@Email), so trim before validating and sending.
    const data = { ...form, email: form.email.trim() };
    const errors = validateRegister(data);
    setFieldErrors(errors);
    setFormError('');
    if (Object.keys(errors).length > 0) {
      return;
    }

    setSubmitting(true);
    try {
      await register(data);
      navigate('/');
    } catch (error) {
      if (error instanceof AuthError) {
        // 400 comes with a message per field; 409 and 0 only with a general message.
        const hasFieldErrors = Object.keys(error.fieldErrors).length > 0;
        setFieldErrors(error.fieldErrors);
        setFormError(hasFieldErrors ? '' : error.message);
      } else {
        setFormError('Something went wrong. Try again.');
      }
      setSubmitting(false);
    }
  };

  const field = (name: keyof RegisterData, label: string, type: string, autoComplete: string) => (
    <div className="mb-3">
      <label htmlFor={`register-${name}`} className="form-label">
        {label}
      </label>
      <input
        id={`register-${name}`}
        type={type}
        autoComplete={autoComplete}
        className={`form-control${fieldErrors[name] ? ' is-invalid' : ''}`}
        value={form[name]}
        onChange={(e) => handleChange(name, e.target.value)}
        aria-invalid={Boolean(fieldErrors[name])}
        aria-describedby={fieldErrors[name] ? `register-${name}-error` : undefined}
      />
      {fieldErrors[name] && (
        <div id={`register-${name}-error`} className="invalid-feedback">
          {fieldErrors[name]}
        </div>
      )}
    </div>
  );

  return (
    <section className="pt-auth">
      <h2 className="pt-section-title">Create account</h2>
      <form onSubmit={handleSubmit} noValidate>
        {formError && (
          <div className="alert alert-danger" role="alert">
            {formError}
          </div>
        )}
        {field('email', 'Email', 'email', 'email')}
        {field('username', 'Username', 'text', 'username')}
        {field('password', 'Password', 'password', 'new-password')}
        <button type="submit" className="btn btn-primary w-100" disabled={submitting}>
          {submitting ? 'Creating account...' : 'Create account'}
        </button>
      </form>
      <p className="mt-3">
        Already have an account? <Link to="/login">Sign in</Link>
      </p>
    </section>
  );
};

export default RegisterPage;
