# Dottie

Dottie is a personal fork of [Element X Android](https://github.com/element-hq/element-x-android) — the next-generation [Matrix](https://matrix.org/) client built on the [Matrix Rust SDK](https://github.com/matrix-org/matrix-rust-sdk), Jetpack Compose, and Appyx. It tracks upstream and adds the changes below.

## What this fork adds

- **Dottie branding** — renamed app, placeholder icon, and its own app identity (`io.github.ceearrbee.dottie`) and `dottie://` deep-link scheme, so it installs alongside official Element X.
- **Material 3 Expressive redesign** — `MotionScheme.expressive()` spring animations, M3E shapes/typography, redesigned chat, room list, navigation, profile, loading indicators, and onboarding.
- **Modern (non-bubble) timeline layout** — an IRC/Matrix-style layout option with sender names, overlay timestamps, and grouping.
- **URL previews** — link preview cards for URLs shared in the timeline.
- **Image crop/rotate editor** for media.
- **Thread list** — dedicated screen listing room threads (Rust SDK `Room.loadThreadList()`).
- **Slash commands** — `/me`, `/topic`, `/invite`, `/kick`, `/ban`, `/unban`, `/join`, `/part`, `/plain`, `/shrug`, `/tableflip`, `/unflip`, `/lenny`, with suggestion UI.
- **Room actions** — favourite/mute toggles, kick/ban message actions, DND options.
- **Recent-chats home-screen widget** and **message scheduling**.
- **Misc UX** — unread count badges, encryption status in the composer placeholder, pin/thread buttons in the chat top bar, sender names in DMs.

## Notifications (F-Droid build)

The F-Droid flavor uses **UnifiedPush** (no Google dependency). For real-time background notifications, install a distributor app such as [ntfy](https://ntfy.sh/), select it in **Settings → Notifications**, and verify with the **Troubleshoot notifications** screen.

## Build

Clone and open in Android Studio, selecting the `app` configuration. Common tasks:

```sh
./gradlew assembleFDroidDebug   # build the F-Droid debug APK
./gradlew test                  # unit tests
./gradlew ktlintFormat          # format
```

To build against a local Rust SDK, see [docs/_developer_onboarding.md](docs/_developer_onboarding.md#building-the-sdk-locally). Minimum SDK: 24 (Android 7.0).

## Copyright and License

This is a fork of Element X Android and retains its dual license.

Copyright (c) 2025 Element Creations Ltd.
Copyright (c) 2022 - 2025 New Vector Ltd.

This software is dual licensed by Element Creations Ltd (Element). It can be used either:

(1) for free under the terms of the GNU Affero General Public License (as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version); OR

(2) under the terms of a paid-for Element Commercial License agreement between you and Element (the terms of which may vary depending on what you and Element have agreed to).
