# AGENTS.md — Element X Android

**Repo:** `element-hq/element-x-android` — Android Matrix client (Compose UI + `matrix-rust-sdk`).
**Language:** Kotlin only. No Java. **Build:** Gradle with Kotlin DSL.

---

## Build / Lint / Test Commands

```bash
# Build
./gradlew assembleDebug                     # Debug APK (default: gplay flavor)
./gradlew assembleGplayDebug                # Google Play debug
./gradlew assembleFdroidDebug               # F-Droid debug
./gradlew assembleRelease                   # Release APK

# Run all checks (what CI runs)
./gradlew check -PallWarningsAsErrors=true
./tools/quality/check.sh                    # Full quality gate: code quality + runQualityChecks + check

# Quality checks (can run individually)
./gradlew detekt                            # Static analysis (detekt + compose rules)
./gradlew ktlintCheck --continue            # Lint formatting
./gradlew ktlintFormat                      # Auto-fix formatting issues
./gradlew runQualityChecks                  # Konsist architecture tests + lint + detekt + ktlint

# Unit tests
./gradlew test                              # All unit tests
./gradlew testDebugUnitTest                 # Debug unit tests only

# Run a single test class
./gradlew :features:login:impl:testDebugUnitTest --tests "io.element.android.features.login.impl.screens.onboarding.OnBoardingPresenterTest"
# Run a single test method
./gradlew :features:login:impl:testDebugUnitTest --tests "io.element.android.features.login.impl.screens.onboarding.OnBoardingPresenterTest.present - initial state"

# Screenshot tests (Paparazzi)
./gradlew verifyPaparazziDebug              # Verify screenshots against golden images
./gradlew recordPaparazziDebug              # Record new golden screenshots (deletes old ones first)

# Code coverage (Kover)
./gradlew :app:koverHtmlReport              # Generate HTML coverage report
./gradlew :app:koverVerify                  # Verify coverage thresholds

# Lint
./gradlew lint                              # Android lint (all modules)
./gradlew :app:lintGplayDebug              # App-specific lint

# Docs
./gradlew generateDocsToc                   # Update markdown table of contents
./gradlew checkDocs                         # Verify TOC is up to date

# Clean
./gradlew clean
```

**Important notes:**
- Screenshot tests are excluded from normal `./gradlew test` runs. They only run during `verifyPaparazzi*` tasks.
- The `runQualityChecks` task includes Konsist tests (`:tests:konsist:testDebugUnitTest`), app lint, detekt, and ktlint.

---

## Project Structure

```
root
├── app/                         # Main Android application (single Activity)
├── appnav/                      # Navigation layer (RootFlowNode, LoggedInFlowNode, NotLoggedInFlowNode)
├── appconfig/                   # App configuration constants
├── appicon/element|enterprise/  # App icon variants
├── annotations/                 # Custom annotations (@ContributesNode)
├── codegen/                     # Code generation (Compound icons, colors)
├── features/
│   └── <feature>/
│       ├── api/                 # Public interfaces, FeatureEntryPoint, data classes
│       ├── impl/                # Implementation: Node, Presenter, View, State, Event, StateProvider
│       └── test/                # Test fakes implementing API interfaces (for other features to use)
├── libraries/
│   ├── architecture/            # BaseFlowNode, Presenter, FeatureEntryPoint, Appyx helpers
│   ├── compound/                # Design system: colors, typography, icons (ElementTheme, CompoundIcons)
│   ├── designsystem/            # UI components (Button, TextField, TopAppBar, Dialogs, etc.)
│   ├── di/                      # DI scopes: AppScope, SessionScope, RoomScope
│   ├── matrix/
│   │   ├── api/                 # Pure Kotlin interfaces (MatrixClient, JoinedRoom, Timeline, etc.)
│   │   ├── impl/                # Rust SDK wrappers (RustMatrixClient, JoinedRustRoom, etc.)
│   │   └── test/                # Fakes (FakeMatrixClient, FakeJoinedRoom, TestData constants)
│   ├── rustsdk/                 # matrix-rust-sdk.aar binding
│   ├── ui-strings/              # Shared string resources (localazy.xml — DO NOT EDIT)
│   └── <other>/                 # 50+ library modules (core, media, push, network, etc.)
├── services/                    # Analytics, app nav state, toolbox
├── tests/
│   ├── konsist/                 # Architecture rule enforcement tests
│   ├── uitests/                 # Paparazzi screenshot tests
│   ├── testutils/               # Shared test utilities
│   └── detekt-rules/            # Custom detekt rules
├── plugins/                     # Precompiled Gradle plugins (convention plugins)
├── enterprise/                  # Enterprise build variant (placeholder)
├── tools/
│   ├── detekt/detekt.yml        # Detekt configuration
│   ├── check/                   # Code quality scripts and forbidden strings lists
│   └── quality/check.sh         # Full quality gate script
└── docs/                        # Developer documentation
```

---

## Architecture: Appyx + Molecule (Circuit-inspired)

### Pattern per Screen (`Foo`)

Each screen follows a strict file convention. All files go in the feature's `impl` module.

| File | Purpose |
| :--- | :--- |
| `FooNode.kt` | Appyx `Node`. Wires Presenter → View. Defines `Callback` interface and `Params` data class. Annotated `@ContributesNode(Scope::class)`. |
| `FooPresenter.kt` | `@Composable fun present(): FooState` — Molecule presenter. Implements `Presenter<FooState>`. |
| `FooView.kt` | Stateless `@Composable` rendering UI from `FooState`. Includes `@PreviewsDayNight` preview. |
| `FooState.kt` | `data class` with `val eventSink: (FooEvents) -> Unit`. **No default values** (Konsist-enforced). |
| `FooEvent.kt` | `sealed interface` (not `sealed class` — Konsist-enforced). |
| `FooStateProvider.kt` | `PreviewParameterProvider<FooState>` + `anFooState()` factory function. |
| `FooPresenterTest.kt` | Unit tests using Turbine + Molecule. |

### Node Pattern

```kotlin
@ContributesNode(AppScope::class)
@AssistedInject
class FooNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: FooPresenter.Factory,
) : Node(buildContext = buildContext, plugins = plugins) {
    interface Callback : Plugin { fun onDone() }
    data class Params(val id: String) : NodeInputs
    private val callback: Callback = callback()
    private val params = inputs<Params>()
    @Composable override fun View(modifier: Modifier) {
        val state = presenter.present()
        FooView(state = state, modifier = modifier)
    }
}
```

### Presenter Pattern

```kotlin
@AssistedInject
class FooPresenter(
    @Assisted private val params: FooNode.Params,
    private val someService: SomeService,
) : Presenter<FooState> {
    @AssistedFactory
    interface Factory {
        fun create(params: FooNode.Params): FooPresenter
    }
    @Composable
    override fun present(): FooState {
        // Use Compose state (remember, derivedStateOf, etc.)
        return FooState(eventSink = ::handleEvent, ...)
    }
}
```

### State Pattern

```kotlin
data class FooState(
    val isLoading: Boolean,
    val data: String,
    val eventSink: (FooEvents) -> Unit,
)
```

### State Provider Pattern

```kotlin
open class FooStateProvider : PreviewParameterProvider<FooState> {
    override val values: Sequence<FooState>
        get() = sequenceOf(aFooState(), aFooState(isLoading = true))
}
fun aFooState(isLoading: Boolean = false) = FooState(
    isLoading = isLoading,
    data = "",
    eventSink = {},
)
```

---

## Dependency Injection (Metro)

We use [Metro](https://zacsweers.github.io/metro/).

### Key Annotations

| Annotation | Usage |
| :--- | :--- |
| `@Inject` | Simple constructor injection (no `@Assisted` params). |
| `@AssistedInject` + `@AssistedFactory` | When constructor needs runtime args (e.g., `Params`, `BuildContext`). |
| `@ContributesBinding(AppScope::class)` | Binds implementation to its interface as singleton. |
| `@ContributesTo(SessionScope::class)` | Contributes a Metro module (with `@Provides` functions). |
| `@ContributesNode(AppScope::class)` | Appyx Nodes that need DI graph lookup. |
| `@BindingContainer` | Marks a Metro module object. |

### DI Rules (Konsist-enforced)
- `@Inject` classes must NOT have `@Assisted` parameters → use `@AssistedInject` instead.
- `@ContributesBinding`, `@ContributesIntoSet`, `@ContributesIntoMap` classes do NOT need `@Inject` annotation.

### Scopes
- `AppScope` — app-wide singletons.
- `SessionScope` — per-session (logged-in user). Defined in `libraries/di/`.
- `RoomScope` — per-room. Defined in `libraries/di/`.

### Naming Convention
- Interface: `FooService`
- Default impl: `DefaultFooService` (with `@ContributesBinding`)
- Test fake: `FakeFooService`

---

## Navigation (Appyx)

- **Single Activity** architecture. `MainActivity` holds the `RootFlowNode`.
- `RootFlowNode` switches between `NotLoggedInFlowNode` and `LoggedInFlowNode`.
- Navigation targets are `sealed interface NavTarget : Parcelable` with `@Parcelize`.
- `resolve(navTarget, buildContext): Node` creates child nodes.
- Features expose `FeatureEntryPoint` interfaces with `createNode()` methods.
- DI graph factories (`SessionGraphFactory`, `RoomGraphFactory`) create scoped sub-graphs.

---

## Code Style Guidelines

### Formatting
- **Max line length:** 160 characters (enforced by ktlint + detekt + forbidden strings check).
- **Indentation:** 4 spaces (no tabs).
- **Kotlin official code style** (`kotlin.code.style=official` in `gradle.properties`).
- Import ordering: `*, java.**, javax.**, kotlin.**, ^` (no star imports except `kotlinx.android.synthetic.**`).

### Naming Conventions
- **Presenter classes:** must end with `Presenter`.
- **Node classes:** must end with `Node`. `BaseFlowNode` subclasses must end with `FlowNode`.
- **State data classes:** named `FooState`. Must NOT have default parameter values.
- **Event sealed interfaces:** named `FooEvents`. Must be `sealed interface` (not `sealed class`).
- **State Providers:** must end with `Provider` and contain the provided type name (e.g., `FooStateProvider`).
- **Fakes:** named `Fake<InterfaceName>` (e.g., `FakeMatrixClient`). Must implement the interface.
- **Default implementations:** named `Default<InterfaceName>` (not `*Impl` suffix).
- **Top-level `@Composable` functions** starting with uppercase: filename must match the function name.
- **Function parameters** must NOT end with `Press` → use `Click` instead (e.g., `onBackClick` not `onBackPress`).
- **No Hungarian notation:** no `m` prefix on fields.
- **Kotlin naming:** `userId` not `userID`, `roomId` not `roomId`.

### Compose Conventions
- Every `@Composable` View must have an `@PreviewsDayNight` preview function.
- Preview functions must: end with `Preview`, be `internal`, use `ElementPreview { ... }` as root.
- Preview function name must match `<ViewUnderPreview>Preview`.
- Never use `@PreviewLightDark` — use `@PreviewsDayNight` instead.
- Sealed interfaces used as Composable parameters must be annotated `@Immutable` or `@Stable`.
- Presenters must NOT depend on other Presenters.
- `@Composable` function naming ignores the `Composable` annotation (ktlint rule).
- Always use `Modifier` parameter with default value in public Composables.
- Flows collected as state must be remembered.
- Use `toImmutableList()` / `toImmutableSet()` / `toImmutableMap()` instead of `toPersistent*()`.

### Logging (Timber)
- Use **Timber** exclusively — never `android.util.Log`.
- Prefer `Timber.tag(loggerTag.value).d("message")` for tagged logging.
- **Never log private info: secrets, passwords, keys, or user content** (e.g., message bodies).
- Matrix IDs (User IDs, Room IDs, Event IDs) are safe to log.
- Provide `Throwable` to Timber error functions when available.

### Strings & Localization
- Strings are managed via [Localazy](https://localazy.com/p/element) and shared with Element X iOS.
- **Never edit `localazy.xml`** — it is auto-generated and overwritten.
- New English strings go in `temporary.xml` files for the core team to import.
- Key naming:
  - Cross-screen verbs: `action_` prefix (e.g., `action_copy`)
  - Common nouns: `common_` prefix (e.g., `common_error`)
  - Accessibility: `a11y_` prefix
  - Screen-specific: `screen_<name>_<key>` (e.g., `screen_onboarding_welcome_title`)
  - Errors: `error_` prefix
  - Placeholders: numbered `%1$s`, `%2$d`
- Use `CommonStrings.<stringKey>` (import from `io.element.android.libraries.ui.strings.CommonStrings`), never `R.string.*`.

### Imports
- Use `androidx.annotation.VisibleForTesting` (not `org.jetbrains.annotations.VisibleForTesting`).
- Use custom `TextField` from `io.element.android.libraries.designsystem.theme.components` (not `material3.OutlinedTextField`).
- Use custom `TopAppBar` from `io.element.android.libraries.designsystem.theme.components` (not `material3.TopAppBar`).

### License Headers
- All `.kt` files must have the AGPL-3.0 license header (see any existing file for template).
- Enterprise files use a separate proprietary license header.
- Each file must have exactly ONE license header.

---

## Testing

### Unit Tests (Presenter Tests)
- Use **Turbine** for Flow testing + **Molecule** for Compose state.
- Test framework: JUnit 4 with Google Truth assertions.
- **Prefer Fakes over Mocks.** Only mock Android framework classes if needed.
- Test function naming: backtick-quoted descriptive names (e.g., `` `present - initial state` ``).
- Factory function to create presenters under test: `createFooPresenter(...)`.
- Assertion style (Konsist-enforced):
  - `assertThat(x).isTrue()` not `assertThat(x).isEqualTo(true)`
  - `assertThat(x).isFalse()` not `assertThat(x).isEqualTo(false)`
  - `assertThat(x).isNull()` not `assertThat(x).isEqualTo(null)`
  - `assertThat(x).isEmpty()` not `assertThat(x).isEqualTo(empty...)`
- Import assertion methods directly (e.g., `import com.google.common.truth.Truth.assertThat`), never use `Truth.assertThat(...)` inline.

```kotlin
class FooPresenterTest {
    @get:Rule val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createFooPresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.isLoading).isFalse()
            // Send events via state.eventSink(...)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

### Screenshot Tests (Paparazzi)
- All `@PreviewsDayNight` Composables are automatically included in screenshot tests via ComposablePreviewScanner.
- Tests are in `tests/uitests/src/test/kotlin/`.
- Tests run in 4 shards for parallelism.
- Recorded golden images stored in `tests/uitests/src/test/snapshots/` (Git LFS).
- Record locally: `./gradlew recordPaparazziDebug` (do NOT commit local recordings).
- Verify: `./gradlew verifyPaparazziDebug`.
- GitHub Action "Record screenshots" should be triggered for PRs with UI changes.

### Konsist Architecture Tests
Located in `tests/konsist/`. Enforces naming, structure, and import rules across the codebase. Key rules enforced:
- State classes have no default values
- Events are sealed interfaces
- Presenters don't depend on other Presenters
- Fake naming conventions
- Class implementation naming (Default* not *Impl)
- Preview naming conventions
- Import restrictions (no material3 OutlinedTextField/TopAppBar)
- Flow remembering rules
- Assertion method style

### Test Fakes
- Feature `test` modules provide fakes implementing API interfaces for other features to use in tests.
- Fake pattern: `class FakeFooService(private val result: () -> Unit = { lambdaError() }) : FooService`
- Matrix fakes are in `libraries/matrix/test/` (e.g., `FakeMatrixClient`, `FakeJoinedRoom`).

---

## Build System Details

### Convention Plugins
Located in `plugins/src/`. Applied via `plugins { id("io.element.android-compose-library") }` etc.
- `io.element.android-root` — root project config
- `io.element.android-compose-application` — app module
- `io.element-android-compose-library` — library modules
- `io.element.android-library` — non-compose library modules

---

## Design System (Compound)

Always prefer Compound components from `libraries/compound/`:

- **Colours:** `ElementTheme.colors.textPrimary`, `ElementTheme.colors.bgCanvasDefault`
- **Typography:** `ElementTheme.typography.fontBodyMdRegular`
- **Icons:** `CompoundIcons.HomeSolid()`, `CompoundIcons.UserProfileSolid()`, etc.
- **Components:** Use designsystem wrappers (`libraries/designsystem/`) over raw Material3

---

## The Rust SDK Layer

The `matrix-rust-sdk` is wrapped to isolate the UI from the underlying SDK.
- API module (`libraries/matrix/api/`): pure Kotlin interfaces — `MatrixClient`, `JoinedRoom`, `Timeline`, etc.
- Impl module (`libraries/matrix/impl/`): Rust SDK wrappers — `RustMatrixClient`, `JoinedRustRoom`, etc.
- Rust types are imported as `org.matrix.rustcomponents.sdk.*` and mapped to Kotlin API types.
- Naming: SDK `Room` → `JoinedRoom` or `RoomInfo`.
- Test module (`libraries/matrix/test/`): fakes like `FakeMatrixClient`, `FakeMatrixAuthenticationService`, `TestData` constants.

---

## PR Guidelines

- Use sentence-style commit/PR messages (no conventional commits).
- PR title = changelog entry — make it descriptive.
- Include screenshots/screen recordings for UI changes.
- Keep PRs focused; split changes over 1000 lines.
- Run `./tools/quality/check.sh` before submitting.
