# Migration Report: kotlinx.collections.immutable 0.5.0

**Project:** element-x-android (Element X Android — Matrix client)
**Modules audited:** all ~200 Gradle subprojects. Library used in 12 build files (8 `implementation`, 4 `testImplementation`).
**Date:** 2026-05-14
**kotlinx.collections.immutable version:** 0.4.0 → 0.5.0-beta01
**Kotlin version:** unchanged by this migration
**Status:** Completed successfully — version bump only, **zero method renames required**

---

## Summary

element-x-android uses `kotlinx.collections.immutable` as a **read-only consumer**. Across 463 Kotlin files importing the library, every value is typed as the read-only interface (`ImmutableList<T>` / `ImmutableMap<K, V>` / `ImmutableSet<T>`) or constructed and used inline. The deprecated copy-returning methods on `Persistent*` are never visible at any call site, so the Kotlin compiler emits **zero** `kotlinx.collections.immutable` deprecation warnings after the version bump.

The migration therefore reduces to a single one-line change in the version catalog. `MIGRATION_REPORT.md` is added for traceability; can be removed if maintainers prefer.

---

## Pre-Migration State

### Library Usage

The library is declared in the version catalog as a single-line full coordinate string (not a separate `[versions]` + `[libraries]` reference):

```toml
kotlinx_collections_immutable = "org.jetbrains.kotlinx:kotlinx-collections-immutable:0.4.0"
```

Build files depending on it (12 total):
- `features/roomcall/impl/build.gradle.kts:24` — `implementation`
- `features/poll/test/build.gradle.kts:21` — `implementation`
- `libraries/pushproviders/unifiedpush/build.gradle.kts:49` — `testImplementation`
- `libraries/pushproviders/firebase/build.gradle.kts:76` — `testImplementation`
- `libraries/previewutils/build.gradle.kts:21` — `implementation`
- `libraries/usersearch/impl/build.gradle.kts:29` — `implementation`
- `libraries/push/impl/build.gradle.kts:93` — `testImplementation`
- `libraries/recentemojis/impl/build.gradle.kts:26` — `implementation`
- `libraries/recentemojis/test/build.gradle.kts:21` — `implementation`
- `libraries/recentemojis/api/build.gradle.kts:21` — `implementation`
- `libraries/matrix/impl/build.gradle.kts:50` — `implementation`
- `libraries/matrix/test/build.gradle.kts:25` — `implementation`

### Import distribution (463 .kt files)

```
239  import kotlinx.collections.immutable.ImmutableList
209  import kotlinx.collections.immutable.toImmutableList
166  import kotlinx.collections.immutable.persistentListOf
 22  import kotlinx.collections.immutable.persistentMapOf
 14  import kotlinx.collections.immutable.ImmutableMap
 13  import kotlinx.collections.immutable.toImmutableMap
 12  import kotlinx.collections.immutable.toImmutableSet
 10  import kotlinx.collections.immutable.persistentSetOf
  9  import kotlinx.collections.immutable.ImmutableSet
  1  import kotlinx.collections.immutable.toPersistentMap
```

Note the imbalance: `ImmutableList` is imported nearly 3× as often as `persistentListOf`. The codebase consumes the library as factory + read-only interface — never holds a `PersistentList<T>`-typed reference long enough for the renamed methods to be visible at a call site.

### Scope checks (all returned 0 or unrelated)

| Check | Count | Notes |
|---|---|---|
| Declarations typed `: Persistent(List\|Map\|Set\|Collection)<…>` | 0 | nothing references the persistent interfaces by type |
| `.mutate { … }` blocks | 1 | `IncomingVerificationStateMachine.kt:85` — `state.mutate { State.Initial(...) }`. This is a custom `mutate` extension on a state-machine type, **not** the `kotlinx.collections.immutable` `PersistentCollection.mutate { Builder -> Unit }` extension. Unrelated. |
| Classes/objects/interfaces implementing `PersistentList`/`PersistentMap`/`PersistentSet`/`PersistentCollection` | 0 | no third-party implementers |
| Java callers of `PersistentList` / `PersistentMap` methods | 0 | |
| Existing `@Suppress("DEPRECATION")` annotations | 19 | all on Android-framework or library-internal deprecations; none on persistent-collection sites |

### Baseline build

- **Command:** `./gradlew :app:compileGplayDebugKotlin :app:compileGplayDebugUnitTestKotlin :app:compileFdroidDebugKotlin :app:compileFdroidDebugUnitTestKotlin compileDebugUnitTestKotlin --continue`
- **Result:** `BUILD SUCCESSFUL in 4m 58s` (4939 actionable tasks; 3064 executed). Zero `kotlinx.collections.immutable` warnings (and zero total `w:` warnings across the compile). Build downloaded Gradle 9.2.1 on first run.

### Pre-requisite environment setup

- **Android SDK:** required a one-line `local.properties` (`sdk.dir=/Users/dmitry.nekrasov/Library/Android/sdk`). Gitignored, so not in the commit.
- **No private Maven repos requiring auth** — unlike bitwarden-android, all dependencies resolved from public repos without credentials.

---

## Migration Steps

### Phase 3: Version Bump

- **File:** `gradle/libs.versions.toml`
- **Change:** `kotlinx_collections_immutable = "org.jetbrains.kotlinx:kotlinx-collections-immutable:0.4.0"` → `"…:0.5.0-beta01"` (line 191)

### Phase 4: Compiler-Driven Renames

Total call sites renamed: **0**.

After bumping the version, the compile loop produced zero `kotlinx.collections.immutable` deprecation warnings across both `:app` flavors (`Gplay`, `Fdroid`) plus all module-level `compileDebugUnitTestKotlin` tasks. Confirmed via `grep -E 'kotlinx.collections.immutable|Persistent(List|Map|Set|Collection)|Use [a-z]+ing\(\) instead' /tmp/element-x-iter1.log` → 0 hits.

### Phase 5: Compiler-Blind Passes

- **5.1 Operator-syntax indexed assignment:** none on `PersistentList` receivers.
- **5.2 Method / callable references:** none on persistent receivers.
- **5.3 Java callers:** zero `.java` files reference the library.

### Phase 6: Interface Implementers

None. element-x-android consumes the library; no class implements its interfaces.

### Phase 7: `@Suppress("DEPRECATION")` Cleanup

None redundant. The post-bump recompile emitted no `'@Suppress("DEPRECATION")' annotation has no effect` warnings.

### Phase 8: Verification

- **Compile:** as above.
- **Tests:** `./gradlew :libraries:recentemojis:impl:testDebugUnitTest :libraries:usersearch:impl:testDebugUnitTest :features:roomcall:impl:testDebugUnitTest --continue` → `BUILD SUCCESSFUL in 18s`. Three modules using the library run their unit tests cleanly. (Full test sweep across ~200 modules is impractical mid-migration; the compile already validates type safety across the codebase, and the version bump is binary-compatible.)

---

## Errors Encountered

None.

---

## Non-Trivial Decisions

- **No textual rewrites applied.** The skill is compiler-driven, and the compiler had nothing to flag. A naive find-and-replace would have damaged 463 import-site files that *look* relevant but aren't (factory calls + read-only interface typing leave no `Persistent*` receiver to migrate).

- **The single `.mutate { }` block is not a kotlinx call.** `state.mutate { State.Initial(...) }` in `IncomingVerificationStateMachine.kt:85` is a state-machine helper, not the `PersistentCollection.mutate { Builder.() -> Unit }` extension. It returns `State.Initial`, not a persistent collection. Left untouched.

- **`:app` product flavors required flavored task names.** The skill's compile examples use `compileDebugKotlin`; for the `:app` module of element-x-android (which has `Gplay` and `Fdroid` flavors), the actual tasks are `compileGplayDebugKotlin` / `compileFdroidDebugKotlin` (+ their `UnitTest` and `AndroidTest` variants). Same situation as bitwarden-android.

## Files Changed

### Gradle Files
- `gradle/libs.versions.toml` — version bump 0.4.0 → 0.5.0-beta01 (line 191).

### Kotlin Sources
- None.

### Java Sources
- None.

### Created
- `MIGRATION_REPORT.md` — this file.

### Not Modified (deliberately)
- All `persistentListOf` / `persistentMapOf` / `persistentSetOf` call sites (~200+) — factory functions, names unchanged in 0.5.0.
- All `.toImmutableList()` / `.toImmutableMap()` / `.toImmutableSet()` / `.toPersistentMap()` conversions — unchanged in 0.5.0.
- `IncomingVerificationStateMachine.kt:85` `state.mutate { … }` — unrelated to kotlinx.collections.immutable.
- 19 existing `@Suppress("DEPRECATION")` annotations — all cover unrelated Android/library deprecations.
