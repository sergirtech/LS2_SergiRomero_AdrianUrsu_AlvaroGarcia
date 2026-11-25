# Video comments (US-09)

**Approved on:** _pending team approval_
**Approved by:** _pending — approve in the PR of `feature/video-comment`_
**Used for:** implementing US-09 (comment on a video) end to end, adapting the useful parts of the `US_division`
branch to the video catalog model already in `main` (D21, D22).

## Prompt

Initial prompt:

```
Before starting, read and analyze the README.md, the DEVELOPMENT.md, the AGENTS.md and finally the REQUIREMENT.md.
After reading and analizing it, we now have to do the US-09. Its very important that everytime you make an important
change you ask me to aprove it. Create a new branch from main called "feature/video-comment" for this implementation.
Maybe the implementation is already done on a branch called "US_division", if thats the case you can use what is
already done, so you should just adapt it in the new branch "feature/video-comment", if not implement it in the new
branch.
```

Decisions given during the session (summarized):

```
- Scope: only US-09. For now, just implement the necessary things to complete the US-09. Later we will work on the
  US-10.
- Comments of the videoGrabber .json files: import them into the database.
- Keep H2 for dev and PostgreSQL for prod. The goal is to create comments by existing users or from being registered
  and logged in and persist them using PostgreSQL.
- Session expiry stays as it is, because US-02 requires it.
```

## Notes

* Result: decision D23 in `docs/decisions.md`; backend 160 tests with JaCoCo thresholds met; frontend 109 tests.
* From `US_division` only the idea was reused: its code depends on another video model and includes US-10 (replies,
  likes) and comment editing and deletion, which are out of scope here.
* Review in particular: the author's name is stored with each comment, the list is not paginated, and the 4 imported
  comments longer than 1000 characters are cut.
