# Ravel — Features & Divergence from Element X

This file tracks all intentional divergences from the upstream [Element X Android](https://github.com/element-hq/element-x-android) codebase.

When syncing upstream changes, review this file to understand what needs to be preserved or rebased.

---

## Vision

Ravel is a Matrix client for people, not power users. Built on Element X Android's foundation (Matrix Rust SDK + Jetpack Compose), Ravel's differentiators are:

- **Phone contacts as first-class citizens** — tap a contact, start a bridged chat. No Matrix IDs required.
- **Self-hosted bridge support** — designed around mautrix bridges (WhatsApp, Signal, iMessage, Telegram). Bridge management happens in bridge bot rooms, not custom UI.
- **Custom UI/UX** — clean, opinionated design distinct from Element's enterprise aesthetic.

---

## Upstream Sync Strategy

- Remote `upstream` tracks `element-hq/element-x-android`
- A **weekly GitHub Actions workflow** (`.github/workflows/upstream-sync.yml`) automatically opens a sync PR every Monday at 8:00 UTC targeting `develop`
- Customizations should be isolated to their own modules where possible to minimize merge conflicts
- See [`docs/upstream-sync.md`](docs/upstream-sync.md) for the full sync guide, conflict resolution tips, and manual sync instructions

To fetch upstream changes manually:
```bash
git fetch upstream
git merge upstream/develop
```

---

## Active Divergences

### 1. MSC4171 — Bridge Service Members / isDM() Fix
**Status:** Planned  
**Upstream issue:** [#4034](https://github.com/element-hq/element-x-android/issues/4034)  
**What:** Patch `isDM()` to use `service_members` from Rust SDK so bridged DM rooms (2 users + bridge bot) correctly appear as DMs, not group rooms.  
**Why upstream hasn't done it:** MSC4171 isn't fully stable; Rust SDK FFI doesn't expose service_members yet.

---

### 2. Phone Contacts Integration
**Status:** Planned  
**What:** Request Android contacts permission. Resolve phone numbers against configured mautrix bridge provisioning APIs. Present contacts in a native contact-picker UX. Tapping a contact opens or creates the bridged DM room.  
**Key API:** `/_matrix/provision/v1/` (per-bridge)

---

### 3. Custom UI / Branding
**Status:** In progress  
**What:** Ravel visual identity replacing Element X branding. Theme, colors, typography, app icon.

---

## Planned Features (Not in Upstream)

- [ ] Phone contact picker with bridge-aware resolution
- [ ] MSC4171 isDM() fix for bridged rooms
- [ ] Bridge status indicator in room list (connected/disconnected)
- [ ] Unified "People" tab showing contacts across all bridges

## Removed / Disabled from Upstream

_(nothing yet)_

---

## Links

- Upstream: https://github.com/element-hq/element-x-android
- Matrix Rust SDK: https://github.com/matrix-org/matrix-rust-sdk
- mautrix bridges: https://docs.mau.fi/bridges/
- MSC4171: https://github.com/matrix-org/matrix-spec-proposals/blob/tulir/service-members/proposals/4171-service-members.md
- SchildiChat Next (fork reference): https://github.com/SchildiChat/schildichat-android-next
