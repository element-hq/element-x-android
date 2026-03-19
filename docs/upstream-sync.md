# Upstream Sync Guide

Ravel is a fork of [Element X Android](https://github.com/element-hq/element-x-android). This document explains how we keep in sync with upstream and how to handle the merge process.

## Why weekly sync PRs?

We merge upstream changes weekly via automated PR rather than continuously rebasing or cherry-picking because:

- **Batched review** — a single weekly PR collects all upstream changes into one reviewable unit, making it easier to spot conflicts with Ravel customizations.
- **Clean history** — merge commits preserve upstream history without rewriting Ravel's branch, which avoids force-push issues for other contributors.
- **Controlled cadence** — weekly is frequent enough to avoid massive conflict backlog, but infrequent enough to keep review burden manageable.

## Automated workflow

The GitHub Actions workflow at `.github/workflows/upstream-sync.yml` runs every Monday at 8:00 UTC. It:

1. Fetches `upstream/develop` (element-hq/element-x-android)
2. Checks if there are new commits since the last merge base
3. Creates a `sync/upstream-YYYY-MM-DD` branch from `origin/develop`
4. Merges `upstream/develop` into the branch
5. Pushes the branch and opens a PR targeting `develop` with the `upstream-sync` label

If the merge has conflicts, the workflow commits the conflict markers so the branch is pushable and the PR body lists the conflicted files.

### Running the workflow manually

1. Go to **Actions** → **Upstream Sync** in the GitHub repo
2. Click **Run workflow** → select the `develop` branch → **Run workflow**
3. The PR will appear within a couple of minutes

## Manual sync process

When you need to sync outside of the automation (e.g., to pull a specific upstream fix immediately):

```bash
# 1. Fetch upstream
git fetch upstream

# 2. Create a sync branch
git checkout -b sync/upstream-$(date +%Y-%m-%d) develop

# 3. Merge upstream develop
git merge upstream/develop --no-edit

# 4. If there are conflicts, resolve them (see sections below), then:
git add -A
git commit

# 5. Push and open a PR
git push -u origin HEAD
gh pr create --base develop --label upstream-sync \
  --title "chore: upstream sync $(date +%Y-%m-%d)" \
  --body "Manual upstream sync. See FEATURES.md for divergences to review."
```

## Files to review after every sync

These are the areas where Ravel intentionally diverges from upstream. After every sync, check whether upstream made changes to these files and ensure Ravel customizations are preserved:

| File / Area | What to check | FEATURES.md section |
|---|---|---|
| `FEATURES.md` | Should not be overwritten by upstream | — |
| `app/src/main/res/values/colors.xml` | Ravel brand palette | §3 Custom UI / Branding |
| Package name (`app.ravel.android`) | Any new files referencing `io.element.android` | §3 Custom UI / Branding |
| `plugins/src/main/kotlin/config/BuildTimeConfig.kt` | App ID, name, deep link host | §3 Custom UI / Branding |
| `plugins/src/main/kotlin/Versions.kt` | Dependency version bumps — verify compatibility | — |
| Bridge-related modules (`features/home/impl/.../bridge/`) | Ravel-only code, should not exist upstream | §1 MSC4171, §2 Phone Contacts |
| App icons (`app/src/main/res/mipmap-*`) | Ravel icons must not be replaced | §3 Custom UI / Branding |

## Common conflict hotspots

### `colors.xml`
Upstream may add new color entries or reorder existing ones. Ravel overrides specific brand colors. **Resolution:** Keep Ravel's brand colors, add any new upstream colors that don't conflict.

### `BuildTimeConfig.kt` / `Versions.kt`
Upstream version bumps to the Matrix Rust SDK or Compose libraries may conflict with Ravel's build config. **Resolution:** Take upstream's version bumps unless they break Ravel-specific features — test the build after merging.

### Package name references
New upstream files will use `io.element.android`. If they're under `app/src/`, they may need the `app.ravel.android` package name. **Resolution:** Check if the file is in `app/src/main/` — if so, update the package declaration. Files in library modules keep upstream's package.

### New resource files
Upstream may add new drawables, strings, or themes that reference Element branding. **Resolution:** Check if they're user-visible. If so, update branding. If they're internal/technical, leave as-is.

## Tips

- Always run `./gradlew assembleFdroidDebug` after resolving conflicts to verify the build compiles.
- Use `git log --oneline upstream/develop..develop` to see what Ravel has on top of upstream (useful for understanding the diff in the other direction).
- If a sync PR is too large to review comfortably, consider splitting it: merge upstream up to a specific commit, then do a second PR for the rest.
