import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import fetchMock from 'jest-fetch-mock';
import { MemoryRouter } from 'react-router';

import { mockApi, reply, videoComment } from '../../testUtils/mockApi';
import CommentsSection from '../CommentsSection';

const COMMENTS = '/videos/1/comments';
const POST_COMMENT = `POST ${COMMENTS}`;

const renderSection = async (signedIn = true) => {
  render(
    <MemoryRouter>
      <CommentsSection videoId={1} signedIn={signedIn} />
    </MemoryRouter>
  );
  await act(async () => {});
};

const input = () => screen.getByRole('textbox', { name: 'Add a comment' });
const button = () => screen.getByRole('button', { name: 'Comment' });
const write = (text: string) => fireEvent.change(input(), { target: { value: text } });
const postCalls = () => fetchMock.mock.calls.filter(([, init]) => init?.method === 'POST');
const texts = () => screen.getAllByRole('listitem').map((item) => item.querySelector('p')?.textContent);

describe('CommentsSection', () => {
  beforeEach(() => {
    fetchMock.resetMocks();
  });

  it('shows the comments of the video in the order of the API', async () => {
    mockApi({ [COMMENTS]: [videoComment(2, 'Newest', 'bob'), videoComment(1, 'Oldest')] });
    await renderSection();

    expect(texts()).toEqual(['Newest', 'Oldest']);
    expect(screen.getByText('@bob')).toBeInTheDocument();
    expect(screen.queryByRole('status')).not.toBeInTheDocument();
  });

  it('shows an empty state', async () => {
    mockApi({ [COMMENTS]: [] });
    await renderSection();

    expect(screen.getByText('No comments yet.')).toBeInTheDocument();
  });

  it('shows an error when the comments cannot be loaded', async () => {
    mockApi({ [COMMENTS]: reply(503) });
    await renderSection();

    expect(screen.getByRole('alert')).toHaveTextContent('Could not load the comments (HTTP 503).');
    expect(screen.queryByText('No comments yet.')).not.toBeInTheDocument();
  });

  it('saves a trimmed comment with the session and shows the saved one first', async () => {
    mockApi({
      [COMMENTS]: [videoComment(1, 'Oldest')],
      [POST_COMMENT]: reply(201, videoComment(2, 'My comment', 'ana')),
    });
    await renderSection();

    write('  My comment  ');
    fireEvent.click(button());

    expect(await screen.findByText('My comment')).toBeInTheDocument();
    expect(texts()).toEqual(['My comment', 'Oldest']);
    expect(input()).toHaveValue('');
    expect(postCalls()).toHaveLength(1);
    expect(postCalls()[0][1]).toMatchObject({
      credentials: 'include',
      body: JSON.stringify({ text: 'My comment' }),
    });
  });

  it('does not send blank comments', async () => {
    mockApi({ [COMMENTS]: [] });
    await renderSection();

    expect(button()).toBeDisabled();
    write('   ');
    expect(button()).toBeDisabled();
    fireEvent.submit(input().closest('form')!);

    expect(postCalls()).toHaveLength(0);
  });

  it('sends the comment only once while it is being saved', async () => {
    mockApi({ [COMMENTS]: [], [POST_COMMENT]: reply(201, videoComment(2, 'Once')) });
    await renderSection();

    write('Once');
    fireEvent.click(button());
    expect(button()).toBeDisabled();
    fireEvent.submit(input().closest('form')!);

    expect(await screen.findByText('Once')).toBeInTheDocument();
    expect(postCalls()).toHaveLength(1);
  });

  it.each([
    ['the backend message', reply(400, { message: 'Comment must have between 1 and 1000 characters' }), /between 1/],
    ['an expired session', reply(401), 'Your session has expired. Sign in again.'],
    ['another error', reply(500), 'Could not add the comment (HTTP 500).'],
  ])('keeps the text and shows %s when saving fails', async (_case, response, message) => {
    mockApi({ [COMMENTS]: [], [POST_COMMENT]: response });
    await renderSection();

    write('Do not lose me');
    fireEvent.click(button());

    expect(await screen.findByRole('alert')).toHaveTextContent(message);
    expect(input()).toHaveValue('Do not lose me');
    expect(screen.queryAllByRole('listitem')).toHaveLength(0);
    await waitFor(() => expect(button()).toBeEnabled());
  });

  it('keeps a comment added while the list was still loading, without duplicates', async () => {
    let answerList: (body: string) => void = () => {};
    const list = new Promise<string>((resolve) => (answerList = resolve));
    fetchMock.mockResponse((request) =>
      request.method === 'POST' ? Promise.resolve(JSON.stringify(videoComment(3, 'Fast'))) : list
    );
    await renderSection();

    write('Fast');
    fireEvent.click(button());
    expect(await screen.findByText('Fast')).toBeInTheDocument();
    await act(async () => answerList(JSON.stringify([videoComment(3, 'Fast'), videoComment(1, 'Oldest')])));

    expect(await screen.findByText('Oldest')).toBeInTheDocument();
    expect(texts()).toEqual(['Fast', 'Oldest']);
  });

  it('asks visitors to sign in and still shows the comments', async () => {
    mockApi({ [COMMENTS]: [videoComment(1, 'Public comment')] });
    await renderSection(false);

    expect(screen.getByRole('link', { name: 'Sign in to add a comment' })).toHaveAttribute('href', '/login');
    expect(screen.queryByRole('textbox')).not.toBeInTheDocument();
    expect(screen.getByText('Public comment')).toBeInTheDocument();
  });
});
