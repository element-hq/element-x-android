# Feral Android — Quick Reference

> Authoritative process doc: `docs/FERAL_MAINTENANCE.md` (FR). This page is only a TL;DR.

## Upstream Sync (TL;DR)

We follow upstream **release tags** (`vYY.MM.N`) with *rebase-onto-tag*, never `merge upstream/develop`:

```bash
git fetch upstream --tags
git checkout -b feral/sync/vYY.MM.N develop
git rebase --onto vYY.MM.N <current-base-tag>   # current base tag: docs/FERAL_MAINTENANCE.md §12 (v26.08.2)
./gradlew :features:enterprise:impl-foss:testDebugUnitTest :features:appupdate:impl:testDebugUnitTest
./gradlew :app:assembleFdroidDebug
```

Conflict rule of thumb: Feral-owned files (`Feral*.kt`, `strings_feral*.xml`, `features/appupdate/`, `tools/feral/`, `.github/workflows/feral-*.yml`) keep ours; Localazy `localazy.xml` / `translations.xml` and everything else take upstream verbatim.

## What's Changed from Element X

| Area | Element X | Feral |
|------|-----------|-------|
| App name | Element X | Feral |
| App ID | `io.element.android.x` | `feral.app` |
| Homeservers | any | **members-only**: only `https://feralisme.fr` (`FeralEnterpriseService`, guard test) |
| Onboarding background | Element blue/gradient | Dark near-black gradient |
| Onboarding logo | Element logo in container | White Feral logo, no container |
| Onboarding title | "Element X" | "FERAL" (44sp, letter-spaced) |
| Onboarding subtitle | Element tagline | "FOR FERALISTS" (faded white) |
| Buttons | Standard Material | Frosted glass (10% white fill) |
| Sign-in button | "Sign in to <server>" | plain "Sign in" (no server URL) |
| Login screen | Standard | + Members-only serif notice (`strings_feral.xml`) |
| Registration | In-app | None in-app (`OnBoardingConfig.CAN_CREATE_ACCOUNT = false`); members sign up on feralisme.fr |
| Typography | Default | Serif-based (onboarding / notice) |
| Updates | Play Store / F-Droid | In-app updater (`features/appupdate`, pinned signing cert) |
| Push (release build) | gplay = Firebase | `fdroid` flavour = UnifiedPush / background sync, no Google code |
| Element services (rageshake, analytics…) | Element | nulled in `BuildTimeConfig` |

## Custom Files (Feral-only)

- `features/enterprise/impl-foss/.../FeralEnterpriseService.kt` (+ `FeralEnterpriseServiceTest.kt`) — members-only lock
- `features/appupdate/{api,impl}` + `appconfig/.../AppUpdateConfig.kt` — in-app updater
- `features/login/impl/.../onboarding/Feral*.kt` (logo, overlay, page, buttons, typography) — branding
- `features/login/impl/src/main/res/values*/strings_feral.xml`, `features/home/impl/src/main/res/values*/strings_feral_appupdate.xml` — Feral strings (outside Localazy)
- `.github/workflows/feral-ci.yml`, `feral-release.yml`, `feral-upstream-sync.yml`, `tools/feral/*.sh` — CI / release
- `docs/FERAL_MAINTENANCE.md` — runbook (FR)

## Building

```bash
./gradlew :app:assembleFdroidDebug     # Debug (what Feral CI builds)
./gradlew :app:assembleFdroidRelease   # Release, unsigned unless signing.properties exists (see docs/FERAL_MAINTENANCE.md §11)
```
