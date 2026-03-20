# M3 Expressive — Design Decisions

## Feature Flag
- `FeatureFlags.M3Expressive` added with `key = "feature.m3_expressive"`
- Default enabled for non-release builds (`buildType != BuildType.RELEASE`)
- Not yet wired to gate runtime behavior — MotionScheme is applied unconditionally at theme level since it's the foundation. Feature flag is available for future per-component gating.

## MotionScheme Strategy
- `MotionScheme.expressive()` applied globally in `ElementTheme()` via the `MaterialTheme(motionScheme = ...)` parameter
- This makes `MaterialTheme.motionScheme` available to all composables in the tree
- New composables in `ExpressiveTransitions.kt` and `M3Motion.kt` read `MaterialTheme.motionScheme.defaultSpatialSpec()` / `.defaultEffectsSpec()` for spring-physics transitions
- Appyx transition handlers (non-composable context) use `spring()` with `Spring.DampingRatioLowBouncy` / `Spring.StiffnessMediumLow` to match the expressive feel

## Motion Migration
- All hardcoded `tween()` calls in navigation transition handlers replaced with `spring()` specs
- `M3Motion.kt` fully rewritten: tween-based constants → MotionScheme-powered composable properties
- `ExpressiveTransitions.kt` created as reusable transition library with reduce-motion compliance (`snap()` fallback)
- Micro-interaction tweens (100ms scale, delayed alpha) left as-is where spring physics would be inappropriate (delay-based staggering)

## Loading Indicators
- `ElementLoadingIndicator` wraps M3E `LoadingIndicator` with polygon shape morphing animation
- `ElementFullScreenLoading` — fullscreen centered variant
- `ElementPaginationLoading` — list footer variant (32dp, within 120dp container)
- Preview fallback: `CircularProgressIndicator` in `LocalInspectionMode` since `LoadingIndicator` animation doesn't render in previews
- **13 files** updated to use `ElementLoadingIndicator`
- **18 files** kept with `CircularProgressIndicator` — these are tiny inline indicators (12-24dp with custom strokeWidth) where the M3E loading indicator is too large

## Shape Morphing (Send Button)
- `MaterialShapes.morph()` requires `androidx.graphics:graphics-shapes` dependency which is not included
- Implemented corner-radius animation instead: `CircleShape` (18dp radius) → `RoundedCornerShape(10.dp)` on press
- Uses `interactionSource.collectIsPressedAsState()` with spring animation
- Achieves the M3E "squish" visual feedback without adding a new dependency

## WavyProgressIndicator
- `WavyLinearProgressIndicator` wrapper created for both determinate and indeterminate variants
- Uses M3E `LinearWavyProgressIndicator` at runtime, falls back to `LinearProgressIndicator` in previews
- Available for file transfer progress bars (not yet wired — existing usages use `LinearProgressIndicator` with specific styling)

## WideNavigationRail
- `ElementWideNavigationRail` wrapper created around M3E `WideNavigationRail`
- Uses `ElementTheme.colors.bgSubtlePrimary` as container color (consistent with `NavigationBar`)
- Available for medium-width adaptive layouts (not yet wired to `HomeView` — requires window size class integration)

## Decisions NOT to Implement
- **ButtonGroup for filter chips**: `ButtonGroup` is for connected/segmented controls, not scrollable filter lists. Current `FilterChip` + `LazyRow` with spring animations is the correct pattern.
- **TwoRowsTopAppBar**: Current `LargeTopAppBar` already provides the collapsing behavior needed. Replacing would add complexity without clear UX benefit.
- **Avatar MaterialShapes morphing**: Requires `graphics-shapes` dependency not in the project. Current avatar shape system works well.
- **MaterialExpressiveTheme / expressiveLightColorScheme()**: These APIs don't exist. `MaterialTheme(motionScheme = MotionScheme.expressive())` is the correct approach (confirmed by compilation).

## Typography & FloatingToolbar
- `HorizontalFloatingToolbar` already uses `@ExperimentalMaterial3ExpressiveApi` — no changes needed
- Typography emphasis unchanged — Compound typography tokens are brand-calibrated and shouldn't be overridden by M3E defaults
- `ElementShapes` already uses M3 Expressive shape scale (8/12/16/20/28dp) — no changes needed
