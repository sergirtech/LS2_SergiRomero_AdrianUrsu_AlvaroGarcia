---
description: Write the Pull Request description for the current branch against main.
agent: agent
---

Write a Pull Request description for the changes in the current branch compared to `main`
(`git log main..HEAD` and `git diff main...HEAD`).

Rules:

* Base everything on the actual diff and commits; don't invent changes, tasks or test results.
* Title: a Conventional Commit line (`type(scope): description`) — it becomes the squash-merge commit.
* Mention the task / user story from `REQUIREMENT.md` if the commits or branch name reference one.
* If AI was used, say so and link the prompt in `prompts/`.
* Keep it short; output only the Markdown, using exactly these sections:

## Context
Why the change is needed and which task / user story it addresses.

## Changes
Bullet list of the relevant changes, grouped by `backend/`, `frontend/`, `tooling/` or config when it helps.

## How to test
Numbered steps a reviewer can follow on Windows/PowerShell: commands (e.g. `.\mvnw.cmd clean verify` in `backend/`,
`npm run test` in `frontend/`), required env vars by name only, and the manual checks (endpoint or screen) with the
expected result.
