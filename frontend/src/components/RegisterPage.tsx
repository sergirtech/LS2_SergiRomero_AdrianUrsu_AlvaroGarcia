import { Link } from 'react-router';

// Placeholder: the form arrives in F3.
const RegisterPage = () => {
  return (
    <section className="pt-auth">
      <h2 className="pt-section-title">Create account</h2>
      <p>
        Already have an account? <Link to="/login">Sign in</Link>
      </p>
    </section>
  );
};

export default RegisterPage;
