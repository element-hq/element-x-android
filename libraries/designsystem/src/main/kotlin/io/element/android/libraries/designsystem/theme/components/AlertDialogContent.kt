/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import kotlin.math.max

// Figma designs: https://www.figma.com/file/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?type=design&node-id=911%3A343492&mode=design&t=jeyd1bXKOOx8y10r-1

@Composable
internal fun SimpleAlertDialogContent(
    content: String,
    submitText: String,
    onSubmitClick: () -> Unit,
    title: String? = null,
    subtitle: @Composable (() -> Unit)? = null,
    destructiveSubmit: Boolean = false,
    cancelText: String? = null,
    onCancelClick: () -> Unit = {},
    thirdButtonText: String? = null,
    onThirdButtonClick: () -> Unit = {},
    applyPaddingToContents: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
) {
    SimpleAlertDialogContent(
        icon = icon,
        title = title,
        subtitle = subtitle,
        content = {
            Text(
                text = content,
                style = ElementTheme.materialTypography.bodyMedium,
            )
        },
        submitText = submitText,
        destructiveSubmit = destructiveSubmit,
        onSubmitClick = onSubmitClick,
        cancelText = cancelText,
        onCancelClick = onCancelClick,
        thirdButtonText = thirdButtonText,
        onThirdButtonClick = onThirdButtonClick,
        applyPaddingToContents = applyPaddingToContents,
    )
}

@Composable
internal fun SimpleAlertDialogContent(
    submitText: String,
    onSubmitClick: () -> Unit,
    title: String? = null,
    subtitle: @Composable (() -> Unit)? = null,
    destructiveSubmit: Boolean = false,
    cancelText: String? = null,
    onCancelClick: () -> Unit = {},
    thirdButtonText: String? = null,
    onThirdButtonClick: () -> Unit = {},
    applyPaddingToContents: Boolean = true,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    AlertDialogContent(
        buttons = {
            AlertDialogFlowRow(
                mainAxisSpacing = ButtonsMainAxisSpacing,
                crossAxisSpacing = ButtonsCrossAxisSpacing
            ) {
                if (thirdButtonText != null) {
                    // If there is a 3rd item it should be at the end of the dialog
                    // Having this 3rd action is discouraged, see https://m3.material.io/components/dialogs/guidelines#e13b68f5-e367-4275-ad6f-c552ee8e358f
                    TextButton(
                        modifier = Modifier.testTag(TestTags.dialogNeutral),
                        text = thirdButtonText,
                        size = ButtonSize.Medium,
                        onClick = onThirdButtonClick,
                    )
                }
                if (cancelText != null) {
                    TextButton(
                        modifier = Modifier.testTag(TestTags.dialogNegative),
                        text = cancelText,
                        size = ButtonSize.Medium,
                        onClick = onCancelClick,
                    )
                    Button(
                        modifier = Modifier.testTag(TestTags.dialogPositive),
                        text = submitText,
                        enabled = enabled,
                        size = ButtonSize.Medium,
                        onClick = onSubmitClick,
                        destructive = destructiveSubmit,
                    )
                } else {
                    TextButton(
                        modifier = Modifier.testTag(TestTags.dialogPositive),
                        text = submitText,
                        enabled = enabled,
                        size = ButtonSize.Medium,
                        onClick = onSubmitClick,
                        destructive = destructiveSubmit,
                    )
                }
            }
        },
        title = title?.let { titleText ->
            @Composable {
                Text(
                    text = titleText,
                    style = ElementTheme.typography.fontHeadingSmMedium,
                    textAlign = if (icon != null) TextAlign.Center else TextAlign.Start,
                )
            }
        },
        subtitle = subtitle,
        content = content,
        shape = DialogContentDefaults.shape,
        containerColor = DialogContentDefaults.containerColor,
        iconContentColor = DialogContentDefaults.iconContentColor,
        titleContentColor = DialogContentDefaults.titleContentColor,
        textContentColor = DialogContentDefaults.textContentColor,
        tonalElevation = 0.dp,
        icon = icon,
        // Note that a button content color is provided here from the dialog's token, but in
        // most cases, TextButtons should be used for dismiss and confirm buttons.
        // TextButtons will not consume this provided content color value, and will used their
        // own defined or default colors.
        buttonContentColor = ElementTheme.colors.textPrimary,
        applyPaddingToContents = applyPaddingToContents,
    )
}

/**
 * Copy of M3's `AlertDialogContent` so we can use it for previews.
 */
@Suppress("ContentTrailingLambda")
@Composable
internal fun AlertDialogContent(
    buttons: @Composable () -> Unit,
    icon: (@Composable () -> Unit)?,
    title: (@Composable () -> Unit)?,
    subtitle: @Composable (() -> Unit)?,
    content: @Composable (() -> Unit)?,
    shape: Shape,
    containerColor: Color,
    tonalElevation: Dp,
    buttonContentColor: Color,
    iconContentColor: Color,
    titleContentColor: Color,
    textContentColor: Color,
    applyPaddingToContents: Boolean = true,
) {
    Surface(
        shape = shape,
        color = containerColor,
        tonalElevation = tonalElevation,
    ) {
        Column(
            modifier = Modifier.padding(
                if (applyPaddingToContents) {
                    // We can just apply the same padding to the whole dialog contents
                    DialogContentDefaults.externalPadding
                } else {
                    // We should only apply vertical padding in this case, every component will apply the horizontal content individually
                    DialogContentDefaults.externalVerticalPadding
                }
            )
        ) {
            icon?.let {
                CompositionLocalProvider(LocalContentColor provides iconContentColor) {
                    Box(
                        Modifier
                            .then(if (applyPaddingToContents) Modifier else Modifier.padding(DialogContentDefaults.externalHorizontalPadding))
                            .padding(DialogContentDefaults.iconPadding)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        icon()
                    }
                }
            }
            title?.let {
                CompositionLocalProvider(LocalContentColor provides titleContentColor) {
                    val textStyle = MaterialTheme.typography.headlineSmall
                    ProvideTextStyle(textStyle) {
                        Box(
                            // Align the title to the center when an icon is present.
                            Modifier
                                .then(
                                    if (applyPaddingToContents) {
                                        Modifier
                                    } else {
                                        Modifier.padding(DialogContentDefaults.externalHorizontalPadding)
                                    }
                                )
                                .padding(DialogContentDefaults.titlePadding)
                                .align(
                                    if (icon == null) {
                                        Alignment.Start
                                    } else {
                                        Alignment.CenterHorizontally
                                    }
                                )
                        ) {
                            title()
                        }
                    }
                }
            }
            subtitle?.invoke()
            content?.let {
                CompositionLocalProvider(LocalContentColor provides textContentColor) {
                    val textStyle = MaterialTheme.typography.bodyMedium
                    ProvideTextStyle(textStyle) {
                        Box(
                            Modifier
                                .weight(weight = 1f, fill = false)
                                // We don't apply padding here if it wasn't applied to the root component, this allows us to have a full width content
                                .padding(DialogContentDefaults.textPadding)
                                .align(Alignment.Start)
                        ) {
                            content()
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .then(if (applyPaddingToContents) Modifier else Modifier.padding(DialogContentDefaults.externalHorizontalPadding))
                    .align(Alignment.End)
            ) {
                CompositionLocalProvider(LocalContentColor provides buttonContentColor) {
                    val textStyle =
                        MaterialTheme.typography.labelLarge
                    ProvideTextStyle(value = textStyle, content = buttons)
                }
            }
        }
    }
}

/**
 * Simple clone of FlowRow that arranges its children in a horizontal flow with limited
 * customization.
 */
@Composable
private fun AlertDialogFlowRow(
    mainAxisSpacing: Dp,
    crossAxisSpacing: Dp,
    content: @Composable () -> Unit
) {
    Layout(content) { measurables, constraints ->
        val sequences = mutableListOf<List<Placeable>>()
        val crossAxisSizes = mutableListOf<Int>()
        val crossAxisPositions = mutableListOf<Int>()

        var mainAxisSpace = 0
        var crossAxisSpace = 0

        val currentSequence = mutableListOf<Placeable>()
        var currentMainAxisSize = 0
        var currentCrossAxisSize = 0

        // Return whether the placeable can be added to the current sequence.
        fun canAddToCurrentSequence(placeable: Placeable) =
            currentSequence.isEmpty() || currentMainAxisSize + mainAxisSpacing.roundToPx() +
                placeable.width <= constraints.maxWidth

        // Store current sequence information and start a new sequence.
        fun startNewSequence() {
            if (sequences.isNotEmpty()) {
                crossAxisSpace += crossAxisSpacing.roundToPx()
            }
            // Ensures that confirming actions appear above dismissive actions.
            sequences.add(0, currentSequence.toList())
            crossAxisSizes += currentCrossAxisSize
            crossAxisPositions += crossAxisSpace

            crossAxisSpace += currentCrossAxisSize
            mainAxisSpace = max(mainAxisSpace, currentMainAxisSize)

            currentSequence.clear()
            currentMainAxisSize = 0
            currentCrossAxisSize = 0
        }

        for (measurable in measurables) {
            // Ask the child for its preferred size.
            val placeable = measurable.measure(constraints)

            // Start a new sequence if there is not enough space.
            if (!canAddToCurrentSequence(placeable)) startNewSequence()

            // Add the child to the current sequence.
            if (currentSequence.isNotEmpty()) {
                currentMainAxisSize += mainAxisSpacing.roundToPx()
            }
            currentSequence.add(placeable)
            currentMainAxisSize += placeable.width
            currentCrossAxisSize = max(currentCrossAxisSize, placeable.height)
        }

        if (currentSequence.isNotEmpty()) startNewSequence()

        val mainAxisLayoutSize = max(mainAxisSpace, constraints.minWidth)

        val crossAxisLayoutSize = max(crossAxisSpace, constraints.minHeight)

        val layoutWidth = mainAxisLayoutSize

        val layoutHeight = crossAxisLayoutSize

        layout(layoutWidth, layoutHeight) {
            sequences.forEachIndexed { i, placeables ->
                val childrenMainAxisSizes = IntArray(placeables.size) { j ->
                    placeables[j].width +
                        if (j < placeables.lastIndex) mainAxisSpacing.roundToPx() else 0
                }
                val arrangement = Arrangement.End
                val mainAxisPositions = IntArray(childrenMainAxisSizes.size) { 0 }
                with(arrangement) {
                    arrange(
                        mainAxisLayoutSize,
                        childrenMainAxisSizes,
                        layoutDirection,
                        mainAxisPositions
                    )
                }
                placeables.forEachIndexed { j, placeable ->
                    placeable.place(
                        x = mainAxisPositions[j],
                        y = crossAxisPositions[i]
                    )
                }
            }
        }
    }
}

@Composable
internal fun DialogPreview(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .background(ElementTheme.materialColors.onSurfaceVariant)
            .sizeIn(minWidth = DialogMinWidth, maxWidth = DialogMaxWidth)
            .padding(20.dp),
        propagateMinConstraints = true
    ) {
        content()
    }
}

internal object DialogContentDefaults {
    private val externalPaddingDp = 24.dp
    val shape = RoundedCornerShape(12.dp)
    val externalPadding = PaddingValues(all = externalPaddingDp)
    val externalHorizontalPadding = PaddingValues(horizontal = externalPaddingDp)
    val externalVerticalPadding = PaddingValues(vertical = externalPaddingDp)
    val titlePadding = PaddingValues(bottom = 16.dp)
    val iconPadding = PaddingValues(bottom = 8.dp)
    val textPadding = PaddingValues(bottom = 16.dp)

    val containerColor: Color
        @Composable
        @ReadOnlyComposable
        get() = ElementTheme.colors.bgCanvasDefault

    val textContentColor: Color
        @Composable
        @ReadOnlyComposable
        get() = ElementTheme.materialColors.onSurfaceVariant

    val titleContentColor: Color
        @Composable
        @ReadOnlyComposable
        get() = ElementTheme.materialColors.onSurface

    val iconContentColor: Color
        @Composable
        @ReadOnlyComposable
        get() = ElementTheme.materialColors.primary
}

// Paddings for each of the dialog's parts. Taken from M3 source code.
internal val ButtonsMainAxisSpacing = 8.dp
internal val ButtonsCrossAxisSpacing = 12.dp

internal val DialogMinWidth = 280.dp
internal val DialogMaxWidth = 560.dp

@Preview(group = PreviewGroup.Dialogs, name = "Dialog with title, icon and ok button")
@Composable
@Suppress("MaxLineLength")
internal fun DialogWithTitleIconAndOkButtonPreview() {
    ElementThemedPreview(showBackground = false) {
        DialogPreview {
            SimpleAlertDialogContent(
                icon = {
                    Icon(
                        imageVector = CompoundIcons.NotificationsSolid(),
                        contentDescription = null
                    )
                },
                title = "Dialog Title",
                content = "A dialog is a type of modal window that appears in front of app content to provide critical information," +
                    " or prompt for a decision to be made. Learn more",
                submitText = "OK",
                onSubmitClick = {},
            )
        }
    }
}

@Preview(group = PreviewGroup.Dialogs, name = "Dialog with title and ok button")
@Composable
@Suppress("MaxLineLength")
internal fun DialogWithTitleAndOkButtonPreview() {
    ElementThemedPreview(showBackground = false) {
        DialogPreview {
            SimpleAlertDialogContent(
                title = "Dialog Title",
                content = "A dialog is a type of modal window that appears in front of app content to provide critical information," +
                    " or prompt for a decision to be made. Learn more",
                submitText = "OK",
                onSubmitClick = {},
            )
        }
    }
}

@Preview(group = PreviewGroup.Dialogs, name = "Dialog with only message and ok button")
@Composable
@Suppress("MaxLineLength")
internal fun DialogWithOnlyMessageAndOkButtonPreview() {
    ElementThemedPreview(showBackground = false) {
        DialogPreview {
            SimpleAlertDialogContent(
                content = "A dialog is a type of modal window that appears in front of app content to provide critical information," +
                    " or prompt for a decision to be made. Learn more",
                submitText = "OK",
                onSubmitClick = {},
            )
        }
    }
}

@Preview(group = PreviewGroup.Dialogs, name = "Dialog with destructive button")
@Composable
internal fun DialogWithDestructiveButtonPreview() {
    ElementThemedPreview(showBackground = false) {
        DialogPreview {
            SimpleAlertDialogContent(
                title = "Dialog Title",
                content = "A dialog with a destructive action",
                cancelText = "Cancel",
                submitText = "Delete",
                destructiveSubmit = true,
                onSubmitClick = {},
            )
        }
    }
}

@Preview(group = PreviewGroup.Dialogs, name = "Dialog with third button")
@Composable
internal fun DialogWithThirdButtonPreview() {
    ElementThemedPreview(showBackground = false) {
        DialogPreview {
            SimpleAlertDialogContent(
                title = "Dialog Title",
                content = "A dialog with a third button",
                cancelText = "Cancel",
                submitText = "Delete",
                thirdButtonText = "Other",
                onSubmitClick = {},
            )
        }
    }
}

@Preview(group = PreviewGroup.Dialogs, name = "Dialog with a very long title")
@Composable
@Suppress("MaxLineLength")
internal fun DialogWithVeryLongTitlePreview() {
    ElementThemedPreview(showBackground = false) {
        DialogPreview {
            SimpleAlertDialogContent(
                title = "Dialog Title that takes more than one line",
                content = "A dialog is a type of modal window that appears in front of app content to provide critical information," +
                    " or prompt for a decision to be made. Learn more",
                submitText = "OK",
                onSubmitClick = {},
            )
        }
    }
}

@Preview(group = PreviewGroup.Dialogs, name = "Dialog with a very long title and icon")
@Composable
@Suppress("MaxLineLength")
internal fun DialogWithVeryLongTitleAndIconPreview() {
    ElementThemedPreview(showBackground = false) {
        DialogPreview {
            SimpleAlertDialogContent(
                icon = {
                    Icon(
                        imageVector = CompoundIcons.NotificationsSolid(),
                        contentDescription = null
                    )
                },
                title = "Dialog Title that takes more than one line",
                content = "A dialog is a type of modal window that appears in front of app content to provide critical information," +
                    " or prompt for a decision to be made. Learn more",
                submitText = "OK",
                onSubmitClick = {},
            )
        }
    }
}
