# Material 3 Expressive Design - Integration Guide

Diese Dokumentation zeigt wie die neuen Material 3 Expressive Komponenten in der bestehenden Codebase verwendet werden.

## Komponenten Übersicht

### 1. MotionTokens (Foundation)
Zentralisierte Motion-Spezifikationen für konsistente Animationen.

```kotlin
import io.element.android.libraries.designsystem.theme.motion.MotionTokens

// Easing Curves
MotionTokens.standardEasing       // Normal Easing (0.2, 0.0, 0.0, 1.0)
MotionTokens.emphasizedEasing     // Emphasized Easing für wichtige Actions
MotionTokens.standardAccelerating // Für Exit-Animationen

// Duration Tokens
MotionTokens.durationExtraShort  // 50ms - sehr schnelle Feedbacks
MotionTokens.durationShort       // 100ms - Buttons, Press-Feedback
MotionTokens.durationMedium      // 200ms - Standard Transitions
MotionTokens.durationLong        // 300ms - Screen Transitions
MotionTokens.durationExtraLong   // 500ms - Komplexe Animationen
MotionTokens.durationDelayed     // 1000ms - Delayed Actions
```

### 2. ExpressiveButton
Buttons mit Spring-basiertem Scale-Feedback auf Press.

**Migration von Standard-Button zu ExpressiveButton:**

```kotlin
// VORHER: Standard Material 3 Button
Button(
    onClick = { /* action */ },
    content = { Text("Click me") }
)

// NACHHER: ExpressiveButton
ExpressiveButton(
    onClick = { /* action */ },
    text = "Click me",
    variant = ButtonVariant.Primary
)
```

**Mit Icon:**
```kotlin
ExpressiveButton(
    onClick = { },
    icon = { Icon(Icons.Default.Add, contentDescription = null) },
    text = "Create",
    variant = ButtonVariant.Secondary
)
```

**Alle Varianten:**
```kotlin
// Primary Button (filled, default action)
ExpressiveButton(
    onClick = { },
    text = "Send",
    variant = ButtonVariant.Primary
)

// Secondary Button (elevated, secondary action)
ExpressiveButton(
    onClick = { },
    text = "Next",
    variant = ButtonVariant.Secondary
)

// Filled Tonal (tertiary action)
ExpressiveButton(
    onClick = { },
    text = "Cancel",
    variant = ButtonVariant.FilledTonal
)

// Outlined (less important action)
ExpressiveButton(
    onClick = { },
    text = "Maybe Later",
    variant = ButtonVariant.Outlined
)

// Text Button (minimal)
ExpressiveButton(
    onClick = { },
    text = "Learn More",
    variant = ButtonVariant.Text
)
```

### 3. ExpressiveCard
Karten mit dynamischer Elevation bei Interaktion.

**Beispiel:**
```kotlin
ExpressiveCard(
    onClick = { /* navigate to room */ },
    defaultElevation = 1.dp,
    hoveredElevation = 4.dp,
    pressedElevation = 2.dp,
    content = {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Room Name")
            Text("Recent message preview")
        }
    }
)
```

**Elevated Variant:**
```kotlin
ExpressiveElevatedCard(
    onClick = { },
    content = { /* Content */ }  // Mehr Elevation (2dp → 8dp)
)
```

### 4. ExpressiveAlertDialog
Dialoge mit Scale + Fade Animation.

**Integration:**
```kotlin
var showDialog by remember { mutableStateOf(false) }

if (showDialog) {
    ExpressiveAlertDialog(
        onDismissRequest = { showDialog = false },
        title = { Text("Bestätigung") },
        text = { Text("Möchten Sie fortfahren?") },
        confirmButton = {
            Button(onClick = { showDialog = false }) {
                Text("Ja")
            }
        },
        dismissButton = {
            TextButton(onClick = { showDialog = false }) {
                Text("Nein")
            }
        }
    )
}
```

### 5. ExpressiveListItem
List Items mit Slide-In Animation und Scale-Feedback.

**Beispiel RoomList:**
```kotlin
ExpressiveListItem(
    onClick = { /* open room */ },
    isSelected = selectedRoomId == room.id,
    headlineContent = {
        Text(room.displayName)
    },
    supportingContent = {
        Text(room.topic ?: "No description")
    },
    leadingContent = {
        Icon(Icons.Default.Home, contentDescription = null)
    },
    trailingContent = {
        Text(room.unreadCount.toString())
    }
)
```

**In einer Liste mit Animations-Unterstützung:**
```kotlin
LazyColumn {
    itemsIndexed(
        items = rooms,
        key = { _, room -> room.id }
    ) { index, room ->
        ExpressiveListItem(
            onClick = { viewModel.selectRoom(room.id) },
            isVisible = true,  // Control visibility for entrance animation
            headlineContent = { Text(room.name) }
        )
    }
}
```

### 6. ExpressiveBottomSheet
Bottom Sheets mit Slide-Up Animation.

**Beispiel:**
```kotlin
var showSheet by remember { mutableStateOf(false) }

if (showSheet) {
    ExpressiveModalBottomSheet(
        onDismissRequest = { showSheet = false }
    ) {
        Text("Choose an option", modifier = Modifier.padding(16.dp))
        
        ExpressiveListItem(
            onClick = { 
                viewModel.performAction1()
                showSheet = false
            },
            headlineContent = { Text("Option 1") }
        )
        
        ExpressiveListItem(
            onClick = { 
                viewModel.performAction2()
                showSheet = false
            },
            headlineContent = { Text("Option 2") }
        )
    }
}

Button(onClick = { showSheet = true }) {
    Text("Show Options")
}
```

### 7. ExpressiveFAB
Floating Action Button mit erweiterten Animationen.

**Standard FAB:**
```kotlin
ExpressiveFAB(
    onClick = { /* create new room */ },
    isVisible = !isScrolling,  // Hide while scrolling
    size = FABSize.Medium,
    content = {
        Icon(Icons.Default.Add, contentDescription = "Create room")
    }
)
```

**Extended FAB:**
```kotlin
ExpressiveExtendedFAB(
    onClick = { /* action */ },
    text = "New Chat",
    icon = { Icon(Icons.Default.Add, contentDescription = null) }
)
```

**Alle Größen:**
```kotlin
// Small FAB
ExpressiveFAB(
    onClick = { },
    size = FABSize.Small,
    content = { Icon(Icons.Default.Add, contentDescription = null) }
)

// Medium FAB (default)
ExpressiveFAB(
    onClick = { },
    size = FABSize.Medium,
    content = { Icon(Icons.Default.Add, contentDescription = null) }
)

// Large FAB
ExpressiveFAB(
    onClick = { },
    size = FABSize.Large,
    content = { Icon(Icons.Default.Add, contentDescription = null) }
)
```

### 8. ExpressiveInteraction
Reusable Interaction-Animationen für Custom Komponenten.

```kotlin
import io.element.android.libraries.designsystem.theme.motion.ExpressiveInteraction

// Scale auf Press
val scaleValue = ExpressiveInteraction.scaleOnPress(
    isPressed = isPressed,
    pressedScale = 0.98f,
    durationMillis = MotionTokens.durationShort
)

// Brightness auf Press
val brightnessColor = ExpressiveInteraction.brightnessOnPress(
    isPressed = isPressed,
    durationMillis = MotionTokens.durationShort
)

// Apply to Custom Component
Box(
    modifier = Modifier
        .scale(scaleValue)
        .graphicsLayer { alpha = brightnessColor.alpha }
) {
    // Content
}
```

## Migration Strategie

### Phase 1: Kritische UI-Elemente (High Impact)
1. **Buttons in der App**
   - Alle CTA Buttons → ExpressiveButton
   - Send Button in Messages → ExpressiveButton.Primary
   - Approve/Cancel Dialogs → ExpressiveButton Varianten

2. **Liste & Items**
   - RoomList → ExpressiveListItem
   - UserList → ExpressiveListItem
   - Message List → Adaptive (komplex)

3. **Navigation**
   - Bottom Sheet Dialogs → ExpressiveModalBottomSheet
   - Room Selection → ExpressiveListItem

**Files to Update (Phase 1):**
- `features/home/src/main/kotlin/io/element/android/features/home/RoomListView.kt`
- `features/roommembermoderation/src/main/kotlin/.../RoomMemberListView.kt`
- `features/createroom/src/main/kotlin/.../CreateRoomView.kt`
- `app/src/main/kotlin/.../AppScaffold.kt` (FAB)

### Phase 2: Dialog & Sheet Komponenten
1. Alle AlertDialogs → ExpressiveAlertDialog
2. Alle ModalBottomSheets → ExpressiveModalBottomSheet
3. Color Picker → Expressive Variant

**Files to Update (Phase 2):**
- `features/login/src/main/kotlin/.../LoginView.kt`
- `features/rageshake/src/main/kotlin/.../RageShakeView.kt`
- `features/invite/src/main/kotlin/.../InviteView.kt`

### Phase 3: Cards & Complex Layouts
1. Room Card → ExpressiveCard
2. Message Bubbles → Adaptive Elevation
3. User Profile → Enhanced Interactions

**Files to Update (Phase 3):**
- `features/messages/src/main/kotlin/.../MessageView.kt`
- `features/roomdetails/src/main/kotlin/.../RoomDetailsView.kt`
- `features/home/src/main/kotlin/.../RoomList.kt`

### Phase 4: Edge Cases & Refinements
1. Complex animations synchronization
2. Performance optimization
3. Accessibility considerations (motion reduction)
4. Dark mode adaptations

## Performance Considerations

### Memory Usage
- **Scale Animations**: Minimal overhead (single float value)
- **Color Animations**: Minimal overhead (color interpolation)
- **Multiple animations**: Memoize with `remember { }` to prevent recreation

```kotlin
// ✅ GOOD - Memoized
val interactionSource = remember { MutableInteractionSource() }

// ❌ BAD - Recreated every recomposition
val interactionSource = MutableInteractionSource()
```

### Recomposition Optimization
```kotlin
// ✅ GOOD - animateFloatAsState caches internally
val scaleValue = animateFloatAsState(targetValue)

// ❌ BAD - Creates new animation spec every time
val scaleValue = animateFloatAsState(
    targetValue = scale,
    animationSpec = tween(300)  // Don't do this inline
)
```

### Correct Pattern:
```kotlin
val animationSpec = remember { 
    spring(dampingRatio = 0.6f, stiffness = 500f) 
}

val scaleValue = animateFloatAsState(
    targetValue = scale,
    animationSpec = animationSpec
)
```

## Testing Expressive Design

### Unit Tests
```kotlin
// Test animation values
@Test
fun testButtonScaleOnPress() {
    val scaleAnimationState = ExpressiveInteraction.scaleOnPress(
        isPressed = true,
        pressedScale = 0.98f
    )
    assertEquals(0.98f, scaleAnimationState)
}
```

### Compose Tests
```kotlin
@Test
fun testExpressiveButtonAnimation() {
    composeTestRule.setContent {
        ExpressiveButton(
            onClick = { },
            text = "Test"
        )
    }
    
    composeTestRule.onRoot().performClick()
    // Verify animation was triggered
}
```

### Visual Tests
1. Manual testing on device
2. Screenshot tests for animations (advanced)
3. Performance profiling with Compose Compiler Metrics

## Accessibility

### Motion Reduction
Material 3 Expressive respects system motion preferences:

```kotlin
import androidx.compose.material3.LocalContentColor

// The animation system automatically respects
// android.accessibilityAnimatorDurationScale setting
```

### WCAG Compliance
- All animations are 300ms or less (WCAG AAA)
- No flashy animations that could trigger seizures
- Clear focus indicators on interactive elements

## Troubleshooting

### Problem: Animations feel laggy
**Solution:** 
- Check if mutable state is being recreated
- Verify `remember { }` is used for interactionSource
- Profile with Android Studio Profiler

### Problem: FAB doesn't animate
**Solution:**
- Ensure `isVisible` parameter is properly managed
- Check if parent Composable is recomposing excessively
- Verify AnimatedVisibility is used correctly

### Problem: Colors look different in dark mode
**Solution:**
- Use `ElementTheme.colors` instead of hardcoded colors
- Test with `darkColorScheme` parameter in preview
- Adjust if needed with `.copy(alpha = ...)`

## Resources

- [Material 3 Motion](https://m3.material.io/styles/motion/overview)
- [Compose Animation Docs](https://developer.android.com/jetpack/compose/animation)
- [Spring Animation Tuning](https://developer.android.com/jetpack/compose/animation#spring)
- [Compose Performance](https://developer.android.com/jetpack/compose/performance)
