[![Latest build](https://github.com/element-hq/element-x-android/actions/workflows/build.yml/badge.svg?query=branch%3Adevelop)](https://github.com/element-hq/element-x-android/actions/workflows/build.yml?query=branch%3Adevelop)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=element-x-android&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=element-x-android)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=element-x-android&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=element-x-android)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=element-x-android&metric=bugs)](https://sonarcloud.io/summary/new_code?id=element-x-android)
[![codecov](https://codecov.io/github/element-hq/element-x-android/branch/develop/graph/badge.svg?token=ecwvia7amV)](https://codecov.io/github/element-hq/element-x-android)
[![Element X Android Matrix room #element-x-android:matrix.org](https://img.shields.io/matrix/element-x-android:matrix.org.svg?label=%23element-x-android:matrix.org&logo=matrix&server_fqdn=matrix.org)](https://matrix.to/#/#element-x-android:matrix.org)
[![Localazy](https://img.shields.io/endpoint?url=https%3A%2F%2Fconnect.localazy.com%2Fstatus%2Felement%2Fdata%3Fcontent%3Dall%26title%3Dlocalazy%26logo%3Dtrue)](https://localazy.com/p/element)

# Element X Android

Element X Android is the next-generation [Matrix](https://matrix.org/) client provided by [Element](https://element.io/).

Compared to the previous-generation [Element Classic](https://github.com/element-hq/element-android), the application is a total rewrite, using the [Matrix Rust SDK](https://github.com/matrix-org/matrix-rust-sdk) underneath and targeting devices running Android 7+. The UI layer is written using [Jetpack Compose](https://developer.android.com/jetpack/compose), and the navigation is managed using [Appyx](https://github.com/bumble-tech/appyx).

[<img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" alt="Get it on Google Play" height="80">](https://play.google.com/store/apps/details?id=io.element.android.x)[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/io.element.android.x)

## Fork Changes

This fork ([ceearrbee/element-x-android](https://github.com/ceearrbee/element-x-android)) includes the following changes on top of upstream [element-hq/element-x-android](https://github.com/element-hq/element-x-android):

### Modern (Non-Bubble) Layout Mode

A new timeline layout option that displays messages without chat bubbles, similar to classic IRC/Matrix clients. Includes sender name display, overlay timestamps, proper grouping logic, and accessibility support.

- [`94cb5af8`](https://github.com/ceearrbee/element-x-android/commit/94cb5af8b078f597473eb409731f0cee15d323d4) feat: Add Modern (non-bubble) layout mode for the message timeline
- [`d97edd1a`](https://github.com/ceearrbee/element-x-android/commit/d97edd1a050df74359acc5d8639b4887ff6d3e75) fix: Modern layout review fixes — avatar touch target, dead code, previews
- [`52bcc621`](https://github.com/ceearrbee/element-x-android/commit/52bcc6213af65652f2086c6c091acbb8e9af8619) fix: Modern layout overlay timestamp background and import ordering
- [`a1219bd8`](https://github.com/ceearrbee/element-x-android/commit/a1219bd8af5d6137068fde2ac9188cdcd804bac0) fix: Add middle-of-group own sender preview for modern layout
- [`2786550d`](https://github.com/ceearrbee/element-x-android/commit/2786550df4811be1ffd5e004fc34e47ca981767b) fix: Modern layout production readiness — ripple, tests, previews, docs
- [`7a72c108`](https://github.com/ceearrbee/element-x-android/commit/7a72c108935a91d40ee67ad42401f3818b2b3292) fix: Review round 6 — import ordering, thread preview, localazy compliance

### URL Previews & Image Crop/Rotate Editor

Adds link preview cards for URLs shared in the timeline, plus an image crop and rotate editor for media.

- [`b5947792`](https://github.com/ceearrbee/element-x-android/commit/b59477921baf9a88685f95e39b05b777717c5873) feat: Add URL previews, image crop/rotate editor, and modern layout perf fix

### Material 3 Expressive Redesign

Complete visual overhaul following M3 Expressive guidelines, Google Messages patterns, and Jetchat sample:

- **Theme**: `MotionScheme.expressive()` with spring-physics animations, M3E shape scale (8/12/16/20/28dp)
- **Message bubbles**: Asymmetric Jetchat-style corners (16dp/4dp), primaryContainer color with Material You, animated elevation on press
- **Chat screen**: CenterAlignedTopAppBar with darker `surfaceContainerHigh` layer, rounded-top Surface container wrapping message timeline
- **Chat input**: M3 Surface with animated tonalElevation on focus, FilledTonalIconButton for attachment
- **Room list**: Unified rounded-top container, bold typography + numeric badge for unread rooms, spring animateItem
- **Navigation**: M3 NavigationBar at bottom, ModalNavigationDrawer with profile header
- **Home**: Crossfade tab transitions, FAB scroll-hide with spring animation
- **Profile**: LargeTopAppBar with exitUntilCollapsedScrollBehavior
- **Day separators**: Flanking lines pattern
- **Loading indicators**: ElementLoadingIndicator (polygon morph) + WavyLinearProgressIndicator
- **Onboarding**: Button hierarchy (Filled/Outlined/Text)
- **Settings**: M3E feature flag in Labs, timeline layout always visible

### Thread List

Full thread list feature using the Rust SDK `Room.loadThreadList()` API. Thread button in chat top bar opens a dedicated screen listing all threads with message previews.

### Slash Commands

Full slash command support with suggestion UI:
- `/me`, `/topic`, `/invite`, `/kick`, `/ban`, `/unban`, `/join`, `/part`
- `/plain`, `/shrug`, `/tableflip`, `/unflip`, `/lenny`
- Type `/` to see command suggestions with descriptions

### Additional UX Improvements

- Encryption status shown as composer placeholder text
- Pin button + thread button in chat top bar
- Overflow menu with People/Invite/Settings
- Branded loading splash with Element logo
- Unread count badge on room list items
- Sender names visible in DMs for both participants
- TopAppBar scroll behaviors on RoomDetails, Login, ConfigureRoom

## Table of contents

<!--- TOC -->

* [Fork Changes](#fork-changes)
* [Screenshots](#screenshots)
* [Translations](#translations)
* [Rust SDK](#rust-sdk)
* [Status](#status)
* [Minimum SDK version](#minimum-sdk-version)
* [Contributing](#contributing)
* [Build instructions](#build-instructions)
* [Support](#support)
* [Copyright and License](#copyright-and-license)

<!--- END -->

## Screenshots

Here are some screenshots of the application:

<!--
Commands run before taking the screenshots:
adb shell settings put system time_12_24 24
adb shell am broadcast -a com.android.systemui.demo -e command enter
adb shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm 1337
adb shell am broadcast -a com.android.systemui.demo -e command network -e mobile show -e level 4
adb shell am broadcast -a com.android.systemui.demo -e command network -e wifi show -e level 4
adb shell am broadcast -a com.android.systemui.demo -e command notifications -e visible false
adb shell am broadcast -a com.android.systemui.demo -e command battery -e plugged false -e level 100

And to exit demo mode:
adb shell am broadcast -a com.android.systemui.demo -e command exit
-->

|<img src="./docs/images-lfs/screen_1_light.png" width="280" />|<img src="./docs/images-lfs/screen_2_light.png" width="280" />|<img src="./docs/images-lfs/screen_3_light.png" width="280" />|<img src="./docs/images-lfs/screen_4_light.png" width="280" />|
|-|-|-|-|
|<img src="./docs/images-lfs/screen_1_dark.png" width="280" />|<img src="./docs/images-lfs/screen_2_dark.png" width="280" />|<img src="./docs/images-lfs/screen_3_dark.png" width="280" />|<img src="./docs/images-lfs/screen_4_dark.png" width="280" />|

## Translations

Element X Android supports many languages. You can help us to translate the app in your language by joining our [Localazy project](https://localazy.com/p/element). You can also help us to improve the existing translations.

Note that for now, we keep control on the French and German translations.

Translations can be checked screen per screen using our tool Element X Android Gallery, available at https://element-hq.github.io/element-x-android/. Note that this page is updated every Tuesday.

More instructions about translating the application can be found at [CONTRIBUTING.md](CONTRIBUTING.md#strings).

## Rust SDK

Element X leverages the [Matrix Rust SDK](https://github.com/matrix-org/matrix-rust-sdk) through an FFI layer that the final client can directly import and use.

We're doing this as a way to share code between platforms and while we've seen promising results it's still in the experimental stage and bound to change.

## Status

This project is actively developed and supported. New users are recommended to use Element X instead of the previous-generation app.

## Minimum SDK version

Element X Android requires a minimum SDK version of 24 (Android 7.0, Nougat). We aim to support devices running Android 7.0 and above, which covers a wide range of devices still in use today.

Element Android Enterprise requires a minimum SDK version of 33 (Android 13, Tiramisu). For Element Enterprise, we support only devices that still receive security updates, which means devices running Android 13 and above. Android does not have a documented support policy, but some information can be found at [https://endoflife.date/android](https://endoflife.date/android).

## Contributing

Want to get actively involved in the project? You're more than welcome! A good way to start is to check the issues that are labelled with the [good first issue](https://github.com/element-hq/element-x-android/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22) label. Let us know by commenting the issue that you're starting working on it.

But first make sure to read our [contribution guide](CONTRIBUTING.md) first.

You can also come chat with the community in the Matrix [room](https://matrix.to/#/#element-x-android:matrix.org) dedicated to the project.

## Build instructions

Just clone the project and open it in Android Studio. Make sure to select the
`app` configuration when building (as we also have sample apps in the project).

To build against a local copy of the Rust SDK, see the [Developer
onboarding](docs/_developer_onboarding.md#building-the-sdk-locally) instructions.

## Support

When you are experiencing an issue on Element X Android, please first search in [GitHub issues](https://github.com/element-hq/element-x-android/issues)
and then in [#element-x-android:matrix.org](https://matrix.to/#/#element-x-android:matrix.org).
If after your research you still have a question, ask at [#element-x-android:matrix.org](https://matrix.to/#/#element-x-android:matrix.org). Otherwise feel free to create a GitHub issue if you encounter a bug or a crash, by explaining clearly in detail what happened. You can also perform bug reporting from the application settings. This is especially recommended when you encounter a crash.

## Copyright and License

Copyright (c) 2025 Element Creations Ltd.
Copyright (c) 2022 - 2025 New Vector Ltd.

This software is dual licensed by Element Creations Ltd (Element). It can be used either:

(1) for free under the terms of the GNU Affero General Public License (as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version); OR

(2) under the terms of a paid-for Element Commercial License agreement between you and Element (the terms of which may vary depending on what you and Element have agreed to).

Unless required by applicable law or agreed to in writing, software distributed under the Licenses is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licenses for the specific language governing permissions and limitations under the Licenses.
