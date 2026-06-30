<!-- Comment to post on element-hq/element-x-android#4417 -->

I had a go at this. Instead of fully *hiding* redacted messages (as in #4434), I went with
collapsing runs of 3 or more consecutive deletions into a single tappable group, reusing
the existing state-change grouping UI:

> **Deleted 11 messages from Alice, Bob**

Tap to expand or collapse; runs of 1 or 2 stay inline. Same idea as Element Web.

The header lists the original authors, up to 3 names. If more than 3 people are involved
it shows the first few and then "and others", e.g. "Deleted 30 messages from Alice, Bob,
Carol and others".

I think this avoids the two reasons #4434 was declined:

- It collapses rather than hides. Deletions stay visible and auditable (count plus original
  authors, with the full run one tap away), so nothing is removed and moderation activity
  isn't masked.
- Day dividers and read receipts are preserved. A run is broken by any non-redacted item,
  so day separators are never swallowed, and the run's read receipts are aggregated onto
  the group exactly as the existing state-change grouping already does.

It's opt-in behind an advanced setting, off by default.

One honest limitation: I can show the original authors of the deleted messages, but not
who deleted them. The Rust SDK surfaces `MsgLikeKind.Redacted` as a bare object with no
redacter or reason (and `EventTimelineItem` exposes no redaction metadata either).

That's also why I didn't build the smarter behaviour floated on #4434 (collapse self/DM
deletions but keep moderator/admin removals visible): without the redacter, the app can't
tell a self-deletion apart from a moderator/admin redaction. So "removed by the moderator"
and any self-vs-moderator logic need the SDK to expose that data first. It would be a clean
follow-up, and this header is the obvious place for it.

The implementation is ready and green (pure timeline transform, no SDK changes, unit tests
plus previews; I have before/after/expanded screenshots to share).

Before I open a PR: would you accept this app-side, or is the team's position still that it
belongs in the Rust SDK? Happy to go either way. I can open the PR as-is, or treat this as
reference UX for an SDK-side version.
