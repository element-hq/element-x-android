/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.theme.ElementTheme
import kotlin.math.max

@Composable
internal fun SimpleAlertDialogContent(
    content: String,
    cancelText: String,
    onCancelClicked: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    submitText: String? = null,
    onSubmitClicked: () -> Unit = {},
    thirdButtonText: String? = null,
    onThirdButtonClicked: () -> Unit = {},
    icon: @Composable (() -> Unit)? = null,
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
                        text = thirdButtonText,
                        buttonSize = ButtonSize.Medium,
                        onClick = onThirdButtonClicked,
                    )
                }
                TextButton(
                    text = cancelText,
                    buttonSize = ButtonSize.Medium,
                    onClick = onCancelClicked,
                )
                if (submitText != null) {
                    Button(
                        text = submitText,
                        buttonSize = ButtonSize.Medium,
                        onClick = onSubmitClicked,
                    )
                }
            }
        },
        modifier = modifier,
        title = title?.let { titleText ->
            @Composable {
                Text(
                    text = titleText,
                    style = ElementTheme.typography.fontHeadingSmMedium,
                )
            }
        },
        text = {
            Text(
                text = content,
                style = ElementTheme.materialTypography.bodyMedium,
            )
        },
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
        buttonContentColor = MaterialTheme.colorScheme.primary,
    )
}

/**
 * Copy of M3's `AlertDialogContent` so we can use it for previews.
 */
@Composable
internal fun AlertDialogContent(
    buttons: @Composable () -> Unit,
    icon: (@Composable () -> Unit)?,
    title: (@Composable () -> Unit)?,
    text: @Composable (() -> Unit)?,
    shape: Shape,
    containerColor: Color,
    tonalElevation: Dp,
    buttonContentColor: Color,
    iconContentColor: Color,
    titleContentColor: Color,
    textContentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = containerColor,
        tonalElevation = tonalElevation,
    ) {
        Column(
            modifier = Modifier.padding(DialogContentDefaults.externalPadding)
        ) {
            icon?.let {
                CompositionLocalProvider(LocalContentColor provides iconContentColor) {
                    Box(
                        Modifier
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
            text?.let {
                CompositionLocalProvider(LocalContentColor provides textContentColor) {
                    val textStyle =
                        MaterialTheme.typography.bodyMedium
                    ProvideTextStyle(textStyle) {
                        Box(
                            Modifier
                                .weight(weight = 1f, fill = false)
                                .padding(DialogContentDefaults.textPadding)
                                .align(Alignment.Start)
                        ) {
                            text()
                        }
                    }
                }
            }
            Box(modifier = Modifier.align(Alignment.End)) {
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
                    arrange(mainAxisLayoutSize, childrenMainAxisSizes,
                        layoutDirection, mainAxisPositions)
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
            .sizeIn(minWidth = DialogMinWidth, maxWidth = DialogMaxWidth)
            .padding(20.dp),
        propagateMinConstraints = true
    ) {
        content()
    }
}

internal object DialogContentDefaults {
    val shape = RoundedCornerShape(12.dp)
    val externalPadding = PaddingValues(all = 24.dp)
    val titlePadding = PaddingValues(bottom = 16.dp)
    val iconPadding = PaddingValues(bottom = 8.dp)
    val textPadding = PaddingValues(bottom = 16.dp)

    val containerColor: Color
        @Composable
        @ReadOnlyComposable
        get()= ElementTheme.colors.bgCanvasDefault

    val textContentColor: Color
        @Composable
        @ReadOnlyComposable
        get()= ElementTheme.materialColors.onSurfaceVariant

    val titleContentColor: Color
        @Composable
        @ReadOnlyComposable
        get()= ElementTheme.materialColors.onSurface

    val iconContentColor: Color
        @Composable
        @ReadOnlyComposable
        get()= ElementTheme.materialColors.primary
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
                    Icon(imageVector = Icons.Default.Notifications, contentDescription = null)
                },
                title = "Dialog Title",
                content = "A dialog is a type of modal window that appears in front of app content to provide critical information, or prompt for a decision to be made. Learn more",
                cancelText = "OK",
                onCancelClicked = {},
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
                content = "A dialog is a type of modal window that appears in front of app content to provide critical information, or prompt for a decision to be made. Learn more",
                cancelText = "OK",
                onCancelClicked = {},
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
                content = "A dialog is a type of modal window that appears in front of app content to provide critical information, or prompt for a decision to be made. Learn more",
                cancelText = "OK",
                onCancelClicked = {},
            )
        }
    }
}
