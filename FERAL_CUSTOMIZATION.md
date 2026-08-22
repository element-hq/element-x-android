# Feral Android — Customization & Upstream Sync Guide

This document describes all Feral customizations applied to Element X Android and how to keep the fork up to date with upstream.

> **Process authority:** `docs/FERAL_MAINTENANCE.md` (French) is the authoritative runbook for syncing, CI, release and signing. This file describes *what* is customized; where the two disagree, the runbook wins.

## Repository Setup

```bash
# Clone the fork
git clone https://github.com/jeheja/feral-android.git
cd feral-android

# Add upstream remote (Element X Android)
git remote add upstream https://github.com/element-hq/element-x-android.git

# Verify remotes
git remote -v
# origin    https://github.com/jeheja/feral-android.git (fetch/push)
# upstream  https://github.com/element-hq/element-x-android.git (fetch/push)
```

## Syncing with Upstream

We rebase the thin Feral commit stack onto upstream **release tags** (`vYY.MM.N`); we do **not** merge `upstream/develop`. See `docs/FERAL_MAINTENANCE.md` §4 (loop) and §12 (current base tag: `v26.08.2`). The `Feral upstream sync` workflow opens a PR/issue when a new tag appears.

```bash
git fetch upstream --tags
git checkout -b feral/sync/vYY.MM.N develop
git rebase --onto vYY.MM.N <current-base-tag>   # see docs/FERAL_MAINTENANCE.md §12
./gradlew :features:enterprise:impl-foss:testDebugUnitTest :features:appupdate:impl:testDebugUnitTest
./gradlew :app:assembleFdroidDebug
```

### Conflict Resolution Strategy

| File type | Resolution |
|---|---|
| Feral-owned files (`Feral*.kt`, `FeralEnterpriseService*.kt`, `strings_feral*.xml`, `features/appupdate/`, `tools/feral/`, `.github/workflows/feral-*.yml`, `docs/FERAL_MAINTENANCE.md`) | Always keep ours |
| `OnBoardingView.kt`, `LoginPasswordView.kt`, home room-list files (`RoomListPresenter`, `RoomListContentView`, …) | Keep the Feral hook lines, take the new upstream structure |
| `localazy.xml`, `values-*/translations.xml` | **Take upstream verbatim** — Feral strings live in `strings_feral.xml` / `strings_feral_appupdate.xml` |
| `BuildTimeConfig.kt`, `ApplicationConfig.kt`, `AuthenticationConfig.kt`, `OnBoardingConfig.kt`, `AppUpdateConfig.kt` | Keep Feral values, take new upstream fields |
| `app/build.gradle.kts` | Keep the Feral `release` signingConfig (unsigned without `signing.properties`) |
| `gradle/libs.versions.toml`, `plugins/src/main/kotlin/Versions.kt` | Take upstream (Element Call version = the one the tag ships, runbook §6) |
| Everything else | Take upstream |

## What's Customized

### 1. Onboarding Screen (matches iOS exactly)
- **Dark gradient background** — near-black (`#0D0D12` → `#050508` → `#000000`)
- **White Feral logo** — 160dp, tinted white, no container
- **"FERAL" title** — 44sp, Black weight, 6sp letter spacing, white
- **"FOR FERALISTS" subtitle** — 13sp, 4sp letter spacing, 45% white opacity
- **Frosted glass buttons** — `OutlinedButton` with 10% white fill, 25% white border, 14dp corners

### 2. Login Screen
- **Members-only notice** — "Access is reserved for members of the Feralism community." in serif font, replacing the Matrix subtitle (string `screen_login_members_only_notice` in Feral-owned `strings_feral.xml`, per locale)

### 3. App Identity
- App name: "Feral"
- Application ID: `feral.app`
- Default (and only) homeserver: `https://feralisme.fr`
- Native account creation: disabled (`OnBoardingConfig.CAN_CREATE_ACCOUNT = false`); no sign-up button in the app
- Single-provider sign-in button reads plain "Sign in" (no server URL)
- Element services (rageshake, analytics, map tiles…) nulled in `BuildTimeConfig`

### 4. Design System
- **FeralTypography.kt** (login module, onboarding package) — serif font family
- **FeralOnBoardingButtons.kt / FeralOnBoardingOverlay.kt / FeralOnBoardingPage.kt / FeralLogo.kt** — onboarding components
- `libraries/designsystem` `element_logo.png` (day + night) replaced by the Feral mark

### 5. Localization
- Feral strings live in Feral-owned `strings_feral.xml` (per locale, 37 locales) and `strings_feral_appupdate.xml` (en + fr) — never in the Localazy-generated `localazy.xml` / `translations.xml`, which are taken from upstream verbatim
- Locales without a `strings_feral.xml` fall back to English

### 6. Members-only lock
- Only `https://feralisme.fr` is accepted — `features/enterprise/impl-foss/.../FeralEnterpriseService.kt` replaces `DefaultEnterpriseService` via Metro `@ContributesBinding(replaces = ...)`; `FeralEnterpriseServiceTest` guards `canConnectToAnyHomeserver() == false` in CI

### 7. In-app updater
- `features/appupdate/{api,impl}` + `appconfig/.../AppUpdateConfig.kt` (channel URL, pinned signing certificate, interval) + home banner; release/publish flow in runbook §11

## Key File Locations

```
# Feral-specific files (100% custom — always keep ours)
features/enterprise/impl-foss/src/main/kotlin/.../FeralEnterpriseService.kt
features/enterprise/impl-foss/src/test/kotlin/.../FeralEnterpriseServiceTest.kt
features/appupdate/api/, features/appupdate/impl/
features/login/impl/src/main/kotlin/.../onboarding/FeralLogo.kt
features/login/impl/src/main/kotlin/.../onboarding/FeralOnBoardingOverlay.kt
features/login/impl/src/main/kotlin/.../onboarding/FeralOnBoardingPage.kt
features/login/impl/src/main/kotlin/.../onboarding/FeralOnBoardingButtons.kt
features/login/impl/src/main/kotlin/.../onboarding/FeralTypography.kt
features/login/impl/src/main/res/values*/strings_feral.xml
features/home/impl/src/main/res/values*/strings_feral_appupdate.xml
tools/feral/sign-release.sh, tools/feral/publish-release.sh
.github/workflows/feral-ci.yml, feral-release.yml, feral-upstream-sync.yml
docs/FERAL_MAINTENANCE.md

# Modified upstream files (rebase carefully — marked "Modified by Feral" under the SPDX header)
features/login/impl/src/main/kotlin/.../onboarding/OnBoardingView.kt
features/login/impl/src/main/kotlin/.../loginpassword/LoginPasswordView.kt
features/home/impl/src/main/kotlin/.../roomlist/{RoomListPresenter,RoomListContentView,...}.kt
app/build.gradle.kts  (release signingConfig from signing.properties)

# Config files (keep our values)
plugins/src/main/kotlin/config/BuildTimeConfig.kt
appconfig/src/main/kotlin/.../ApplicationConfig.kt
appconfig/src/main/kotlin/.../AuthenticationConfig.kt
appconfig/src/main/kotlin/.../OnBoardingConfig.kt
appconfig/src/main/kotlin/.../AppUpdateConfig.kt

# App icons and assets
app/src/main/res/mipmap-*/ic_launcher.webp
app/src/main/res/mipmap-*/ic_launcher_round.webp
app/src/main/res/drawable/splash_logo.xml
```

## Building

```bash
# Debug build (what Feral CI builds; validates the Metro DI graph)
./gradlew :app:assembleFdroidDebug

# Release build — unsigned unless signing.properties exists (signing happens off-box, runbook §11)
./gradlew :app:assembleFdroidRelease
```

> `gplay` (Firebase push) is not built anymore: Feral ships the `fdroid` (no-Google) variant only — see `docs/FERAL_MAINTENANCE.md` §11.

> **Note**: You need Android Studio or the Android SDK on your machine. On macOS, install via `brew install --cask android-studio` or download from https://developer.android.com/studio.

## Testing Checklist

After any upstream sync, verify:

- [ ] `FeralEnterpriseServiceTest` passes (members-only lock) — `./gradlew :features:enterprise:impl-foss:testDebugUnitTest`
- [ ] Home banner offers an update when `update.json` advertises a higher versionCode

- [ ] Onboarding screen shows dark gradient background (not Element blue)
- [ ] White Feral logo displayed (no container/background shape)
- [ ] "FERAL" title with letter spacing visible
- [ ] "FOR FERALISTS" subtitle in faded white
- [ ] Frosted glass buttons (semi-transparent white)
- [ ] Login screen shows members-only notice in serif font
- [ ] App name shows "Feral" (not "Element")
- [ ] No Element branding visible anywhere
- [ ] Members-only notice is translated in non-English locales (`strings_feral.xml`)
- [ ] QR code sign-in works (if available)
- [ ] Sign-in flow completes successfully
- [ ] App compiles without errors

## Notes

- We rebase onto upstream release tags, never `upstream/develop` (see runbook §4)
- The fork was originally ~9 months behind upstream (2,916 commits) — successfully merged in March 2025
- iOS and Android should always match visually — check `feral-ios` repo for reference
- No patch system: all customizations are direct commits on the Feral branch (runbook §7); `scripts/remove-feral-patches.sh` was dropped at the v26.08.2 sync — stock Element is `git checkout <base-tag>`
