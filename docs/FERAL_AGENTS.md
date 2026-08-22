# Feral fork — instructions for AI agents (Claude Code, etc.)

This repository is the **Feral** fork of Element X Android (`jeheja/feral-android`). Upstream's
own agent notes are in `AGENTS.md`; this file adds the Feral rules. It is Feral-owned and absent
upstream.

## 1. Read the brain first, update it while you work
The project knowledge base is the **feralism-brain** Obsidian vault
(`~/Documents/feralism/feralism-brain` on the maintainer's machine, private repo
`jeheja/feralism-brain`). Before touching this repo: pull it, read `Home.md`,
`Concepts/Feral Apps.md`, `Flows/Android Upstream Sync Flow.md`,
`Flows/Feral App Update Flow.md`, `Decisions/Android follows Element release tags.md`,
`Decisions/Android APK without Google.md` and the Android section of
`Project/Gotchas & Landmines.md`. While working, write verified facts, decisions and
landmines back into the right note; commit and push the vault before the session ends.
If the vault is not available (e.g. CI, another machine), say so and record the facts in
`docs/FERAL_MAINTENANCE.md` §12 instead.

## 2. Branch model — never merge upstream
- `main` = the latest upstream stable tag (`vYY.MM.N`) + a thin, linear stack of Feral commits.
  The current base tag is `git describe --tags --abbrev=0 --match 'v[0-9]*' --exclude '*-*' main`
  and is recorded in `docs/FERAL_MAINTENANCE.md` §12.
- Syncs are **rebase-onto-tag** (`.github/workflows/feral-upstream-sync.yml` does it twice a week
  and opens a PR or an issue). Accept a sync with
  `git push --force-with-lease origin feral/sync/<tag>:main` — never GitHub's merge button,
  never `git merge upstream/develop`.
- `develop` is frozen history (tag `archive/develop-before-v26.08.2-sync`). Don't build it.

## 3. Where customisation is allowed
Only in the space upstream designed for it — see the table in `docs/FERAL_MAINTENANCE.md` §4/§12:
`plugins/src/main/kotlin/config/BuildTimeConfig.kt`, `appconfig/*Config.kt`, the
`EnterpriseService` hook (`features/enterprise/impl-foss/.../FeralEnterpriseService.kt`, Metro
`replaces`, delegating), fork-owned modules (`features/appupdate`), Feral-owned resources
(`strings_feral*.xml`, `appicon/element`), `tools/feral/`, `.github/workflows/feral-*.yml`, and
the one thin patch on `OnBoardingView.kt`. Never edit Localazy files (`translations.xml`,
`localazy.xml`). An edit inside any other upstream file needs a `Modified by Feral:` note under
the SPDX header and will be re-presented at every sync — prefer a hook.

## 4. Binding decisions (2026-08-22)
- `element-call-embedded` = the version the upstream tag ships; no standalone bumps.
- Onboarding: no sign-up button; the sign-in button says "Sign in", never the homeserver URL.
- `AuthenticationConfig.MATRIX_ORG_URL = ""`; the 7 upstream login tests this turns red are
  not run by Feral CI and must not be "fixed" by editing upstream tests.
- Members-only lock: `canConnectToAnyHomeserver() == false`, allow-list = `https://feralisme.fr`;
  the guard test `FeralEnterpriseServiceTest` must stay green. At every sync, diff
  `features/enterprise/api/.../EnterpriseService.kt` between tags — delegation inherits
  upstream's permissive default for any new member.
- Builds/releases are the `fdroid` (no-Google) flavor only; release tag `feral-v<versionName>`;
  signing happens only on the maintainer's machine.

## 5. Verify like CI does
```
./gradlew :features:enterprise:impl-foss:testDebugUnitTest :features:appupdate:impl:testDebugUnitTest
./gradlew :app:assembleFdroidDebug     # also validates the Metro DI graph
```
Do not add `sdkmanager` steps to CI (AGP provisions the SDK); a Maven Central 429 on CI is
transient — re-run the job.
