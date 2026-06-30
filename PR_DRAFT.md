# Collapse runs of deleted messages in the timeline

<!-- PR title (= release note): Collapse runs of deleted messages in the timeline -->

Relates to #4417. The approach here was discussed and agreed with the team on that issue.

## Summary

When a bunch of messages get deleted in a row (a moderator clearing spam, a bot purge, or
someone removing a burst of their own messages), the timeline shows one "Message removed"
placeholder for each one. Ten deletions become ten grey tiles you have to scroll past to
get back to the actual conversation.

This PR collapses runs of 3 or more consecutive deleted messages into a single, tappable
group. It reuses the mechanism that already groups state-change events (joins, leaves,
name changes), so one line replaces the wall:

> 11 removed messages

Tap it to expand the whole run, tap again to collapse. Runs of one or two deletions are
left alone, so the occasional "Message removed" still reads fine in context. It is on by
default, with no setting, the same as the existing state-change grouping.

## What it looks like

In place of a run of consecutive "Message removed" tiles, the timeline shows a single
centred line, "N removed messages", styled like the existing "N room changes" group. Tap
it to expand the run back into the individual tiles, tap again to collapse.

## Motivation

Collapsing runs of redacted messages is a long-standing feature in Element Web, and it's
a real readability win in busy, heavily-moderated rooms. Element X has nothing like it
today, so large deletion runs just dominate the scrollback. This is the recurring ask in
#4417.

### Why this isn't #4434 again

The earlier PR (#4434) simply hid redacted events and was declined, for two fair reasons:
hiding deletions outright can mask moderation activity, and rewriting the timeline in the
app can break day dividers and read receipts.

This one is different on both points:

- It collapses instead of hiding. Deletions stay visible and auditable, the count is shown
  and the full run is one tap away. Nothing is removed.
- Day dividers and read receipts survive (details below). A run is broken by any
  non-redacted item, so a day separator is never swallowed, and the run's read receipts
  are aggregated onto the group the same way state-change groups already do it.

## Why the header shows only a count

The header reads "N removed messages" and intentionally does not say who the messages
belonged to or who removed them.

Element Web can sometimes attribute the deletion ("removed by the moderator"), but that
data isn't available to the app today: the Rust SDK surfaces a redaction as a bare
`MsgLikeKind.Redacted` with no `redacted_because` / redacter, so we can't tell who
performed the removal. Showing the original authors instead would be misleading, since it
reads as if they removed their own messages when it may have been a moderator. So a plain
count is the honest thing to show for now.

If the SDK later exposes the redacter, this header is the obvious place to add "removed by
X" like Web, with no UI rework. (Discussed and agreed with the team on #4417.)

## How it works

It reuses the existing grouping machinery rather than adding a new UI primitive.

A pure helper, `List<TimelineItem>.collapseRedactedRuns()`, walks the already-built
timeline, finds maximal runs of consecutive redacted events
(`TimelineItemRedactedContent`), and replaces any run of `MIN_REDACTED_RUN_SIZE` (3) or
more with a `TimelineItem.GroupedEvents`, the same type used for state-change collapsing.
Shorter runs and everything else pass through untouched.

A couple of things the #4434 review flagged, and how they're handled:

- Day dividers: the run only accumulates consecutive redacted events. Anything else (a
  real message, a state change, or a day separator) flushes the current run and is added
  unchanged, so a run never spans or absorbs a day boundary.
- Read receipts: the receipts of the collapsed events are aggregated onto the
  `GroupedEvents`, which already renders them, exactly like state-change groups.

`TimelinePresenter` applies the transform to the timeline it exposes, and the redacted
group reuses the existing `GroupHeaderView` and `TimelineItemGroupedEventsRow` with a
count label. Expansion state reuses the existing `rememberSaveable`, so there is no new
state plumbing. Since it runs on the already-resolved timeline item list, it's a thin,
contained change with nothing in the SDK or sync path touched.

## Tests

- `CollapseRedactedRunsTest` (12): the pure transform. Empty list, no redactions, a run
  below the threshold (kept inline), a run at/over the threshold (collapsed), several
  independent runs with distinct group ids, a run at the end of the list, runs broken by a
  real message / a day separator / an existing group, read-receipt aggregation, and events
  stored oldest-first with a stable group id.
- `GroupabilityTest`: redacted-group detection (all redacted, state changes, mixed, empty).
- `TimelinePresenterTest`: a run of redacted events collapses into a single group.
- A preview for the collapsed header, added to the Konsist preview-name exceptions.

## Notes for reviewers

- New string is in `localazy.xml` (EN source); I didn't touch `translations.xml`, since
  Localazy fills those. No towncrier entry and no DCO sign-off, per this repo's
  conventions; the PR title is the release note.
- The change is scoped to `features/messages/impl`. No public API or SDK changes, and no
  new setting.
- iOS: this PR is Android only. I don't work in Swift, so I haven't written the iOS side,
  but the logic is small and self-contained. The porting notes below map it to the iOS
  equivalents.

### Porting to iOS

The Android change is a post-processing pass over the already-built timeline plus a header
label, with no SDK or data changes, so it should mirror cleanly. The shape:

1. Add the equivalent of `collapseRedactedRuns`: walk the assembled timeline items, collect
   maximal runs of consecutive redacted items, and replace any run of 3 or more with the
   existing collapsible group that already backs the "N room changes" group. Leave shorter
   runs and every non-redacted item, day separators included, untouched, so a run never
   crosses a day boundary.
2. When a group is made entirely of redacted items (the equivalent of
   `isRedactedMessagesGroup()`), render the header as a count-only plural, "N removed
   messages", and keep the existing chevron and expand or collapse behaviour.
3. Aggregate the collapsed items' read receipts onto the group, the same way the
   state-change group already does.
4. Add the matching localized plural (the iOS counterpart of
   `screen_room_timeline_redacted_messages`).
5. The same SDK limitation applies: the redaction arrives without a redacter, so the header
   stays count-only on iOS too. If the SDK later exposes it, that header is where "removed
   by X" would go, as on Android.

## Open questions

1. Threshold: 3 felt right to me (2 isn't really a wall). Happy to change the constant or
   expose it.
2. The redacter ("removed by X") is left for a follow-up once the SDK exposes it, as
   agreed on #4417.
