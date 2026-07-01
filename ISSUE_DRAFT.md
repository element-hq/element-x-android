# [Enhancement] Collapse runs of deleted (redacted) messages in the timeline

<!--
  For element-meta (product/design enhancement), filed before the implementation PR,
  same flow as e.g. the timeline multi-select request #3255.
  Platform: Element X (Android first; applies to iOS too).
-->

## The problem

When a bunch of messages are deleted in a row (a moderator clearing spam, a bot purge, or
a user removing a burst of their own messages), the timeline renders **one "Message
removed" placeholder per deletion**. A 30-message spam wave becomes 30 grey placeholders
the user has to scroll through to reach real content.

This is most painful in exactly the rooms where it matters most: large, public, actively
moderated communities.

## Prior art

Collapsing runs of redacted messages is a long-established feature in Element **Web**, and
it's a clear readability win. Element X has no equivalent today.

Related: #4417 ("hide / collapse redacted messages").

## Proposed behaviour

Collapse a run of **3 or more consecutive deleted messages** into a single, tappable group,
reusing the same grouping UI already used for state-change events (joins, leaves, name
changes). One header line replaces the wall:

> **Deleted 30 messages from Alice, Bob, Carol and others**

- Tap to expand the full run, tap again to collapse.
- Runs of 1 or 2 stay as normal inline "Message removed" markers (no wall to hide).
- The header shows the **count**, the **original authors** (up to 3 names, then "and
  others"), and a small leading **avatar stack**.

### Why "collapse", not "hide"

Deletions stay visible and auditable; anyone can expand to see how many and from whom. We
just stop them dominating the scrollback. This sidesteps the moderation concern that fully
hiding redactions could mask abuse.

### Opt-in

Proposed as an **advanced setting, off by default** ("Collapse deleted messages"), so the
default timeline is unchanged. It could be on by default to match Web if product prefers;
that's a one-line change.

## Known limitation: showing *who* deleted

Element Web can sometimes attribute the deletion ("removed by the moderator"). On Element X
we **can't do this today**. The Rust SDK FFI surfaces a redaction as `MsgLikeKind.Redacted`,
a bare object with **no redacter and no reason**. So we reliably know the **original
author** of each deleted message, but not the account that deleted it, and not whether it
was a self-deletion or a moderator/admin action.

Showing the original authors is the accurate version shippable today. Attributing the
redacter is a natural follow-up once the SDK exposes redaction metadata, and the header
above is the obvious place to add "removed by @mod" with no UI rework.

## Scope / status

- The Android implementation is ready (pure timeline-item transform, no SDK or sync
  changes, reuses the existing `GroupedEvents`; unit tests plus previews; behind the
  advanced setting).
- Happy to open the PR as soon as there's product/design buy-in on the approach, the label
  wording, and the default.

## Questions for product / design

1. Is a threshold of **3** to collapse OK?
2. Default **off** (advanced setting) or **on** (matching Web)?
3. Label wording: "Deleted N messages from A, B, C and others", and confirming we drop
   per-author counts (they read as noise in testing).
4. Avatar stack in the header: keep it, or go count-only?
