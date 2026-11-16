import { Link } from 'react-router';

interface Props {
  title: string;
}

/** Placeholder for a section whose user story is not implemented yet (same message as the profile placeholders). */
const ComingSoonPage = ({ title }: Props) => (
  <section className="pt-profile-placeholder" aria-live="polite">
    <h2>{title}</h2>
    <p>This page will be available soon.</p>
    <Link to="/">Back to videos</Link>
  </section>
);

export default ComingSoonPage;
