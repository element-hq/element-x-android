# Material 3 Expressive Design Implementation Guide

## Überblick

Material 3 Expressive ist bereits implementiert, muss aber durchgängiger verwendet werden. Das System bietet:

- **Dynamische Animationen**: Responsive Bewegungen bei Interaktionen
- **Stärkere Typographie-Hierarchie**: Expressive Font-Weights und Größen
- **Elevated Interactions**: Mehr visuelle Tiefe durch Elevation und Schatten
- **Kontrastive Farben**: Lebhaftere, aussagekräftigere Farben
- **Raum und Whitespace**: Großzügigeres Spacing für expressiveres Design

## Implementierte Features

### 1. Motion System (`MotionTokens`)
- **Standard Easing** - Für normale Transitions
- **Emphasized Easing** - Für wichtige State-Changes
- **Duration Tokens** - Von 50ms bis 1000ms für verschiedene Animationen

```kotlin
// Beispiel
import io.element.android.libraries.designsystem.theme.motion.MotionTokens

animationSpec = tween(
    durationMillis = MotionTokens.durationMedium,
    easing = MotionTokens.standardEasing
)
```

### 2. Expressive Components

#### ExpressiveAlertDialog
Dialoge mit Scale + Fade Animation für besseres Feedback:
```kotlin
ExpressiveAlertDialog(
    onDismissRequest = { },
    content = { /* Dialog content */ }
)
```

#### ExpressiveCard / ExpressiveElevatedCard
Karten mit dynamischer Elevation bei Interaktionen:
```kotlin
ExpressiveCard(
    onClick = { },
    defaultElevation = 1.dp,
    hoveredElevation = 4.dp,
    pressedElevation = 2.dp,
    content = { /* Card content */ }
)
```

#### ExpressiveInteraction
Scale- und Brightness-Transformationen:
```kotlin
ExpressiveInteraction.expressiveModifier(
    interactionSource = interactionSource,
    baseColor = Color.Blue,
    pressedScale = 0.98f
)
```

## Migration Steps

### Phase 1: Dialogs und Sheets
- [ ] ListDialog → ExpressiveAlertDialog
- [ ] ModalBottomSheet → Expressive Version mit Slide + Fade
- [ ] Confirmation Dialogs → Scale + Fade Animationen

### Phase 2: Card-basierte Komponenten
- [ ] Message Bubbles → ExpressiveElevatedCard
- [ ] Room Items → ExpressiveCard
- [ ] Reaction Buttons → ExpressiveInteraction

### Phase 3: Button Animations
- [ ] Primary Button → Expressive Scale + Color Feedback
- [ ] Secondary Button → Lighter Scale + Opacity
- [ ] Floating Action Button → Combined Animations

### Phase 4: List Items und Layout
- [ ] ListItem Hover → Scale + Elevation
- [ ] Navigation → Slide + Fade mit standardEasing
- [ ] Sheet Dismissal → Custom easing curves

## Typography für Expressive Design

Moderne Hierarchie mit größeren Headings:

```kotlin
// Headlines sollten 700+ FontWeight verwenden
Text(
    text = "Title",
    style = ElementTheme.typography.fontHeadingXlRegular.copy(
        fontWeight = FontWeight.ExtraBold, // oder Bold
        letterSpacing = 0.5.sp // Leicht erhöht für Drama
    )
)

// Body text bleibt Regular (400)
Text(
    text = "Supporting text",
    style = ElementTheme.typography.fontBodyLgRegular
)
```

## Spacing für Expressive Design

Erhöhte Padding/Margin für mehr Whitespace:

```kotlin
// Vorher: 8.dp, 12.dp, 16.dp
// Nachher: 12.dp, 16.dp, 24.dp

Column(
    modifier = Modifier.padding(24.dp), // War 16.dp
    verticalArrangement = Arrangement.spacedBy(16.dp) // War 12.dp
) {
    // Content
}
```

## Best Practices

### Animationen
1. **Verwende immer MotionTokens** für konsistente Timings
2. **Standard Easing** für normale Transitions
3. **Emphasized Easing** für State-Changes und kritische Momente
4. **Keine Overlapping Animations** - einer nach dem anderen

### Farben
1. **Höherer Kontrast** für wichtige Elemente
2. **Container Colors** für zusammenhängende Gruppen
3. **Accent Colors** für CTAs sparsam verwenden
4. **State Colors** für disabled/error/success deutlich unterscheiden

### Größen und Abstände
1. **Größere Touch-Targets**: Minimum 48dp x 48dp für Buttons
2. **Mehr Whitespace** für Sichtbarkeit
3. **Konsistentes Spacing-System**: 4dp, 8dp, 12dp, 16dp, 24dp
4. **Größere Typographie** für Headlines (48+sp für XL)

## Häufige Fehler vermeiden

❌ **Nicht:**
- Zu viele gleichzeitige Animationen
- Zu schnelle Übergänge (< 100ms für sichtbare Effekte)
- Inconsistente Easings in der gleichen Komponente
- Übermäßig große Elevatione bei Hover

✅ **Besser:**
- Maximal 2 Animation-Properties gleichzeitig
- 200-300ms für sichtbare Übergänge
- Einheitliche Easing für gesamte Interaction
- Subtile Elevation-Änderungen (1-4dp)

## Ressourcen

- [Material 3 Motion Guide](https://m3.material.io/styles/motion/overview)
- [Material 3 Color System](https://m3.material.io/styles/color/overview)
- [Material 3 Typography](https://m3.material.io/styles/typography/overview)
- [Compound Android Components](https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components)
