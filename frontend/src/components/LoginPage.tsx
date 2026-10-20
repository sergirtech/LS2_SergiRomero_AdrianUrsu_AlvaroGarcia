import { Link } from 'react-router';

// Placeholder: the form arrives in F4.
const LoginPage = () => {
  return (
    <section className="pt-auth">
      <h2 className="pt-section-title">Sign in</h2>
      <p>
        Don't have an account? <Link to="/register">Create one</Link>
      </p>
    </section>
  );
};

export default LoginPage;
