/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup

// Designs: https://www.figma.com/file/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?type=design&mode=design&t=U03tOFZz5FSLVUMa-1

// Horizontal padding for button with low padding
internal val lowHorizontalPaddingValue = 4.dp

@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: ButtonSize = ButtonSize.Large,
    showProgress: Boolean = false,
    destructive: Boolean = false,
    leadingIcon: IconSource? = null,
) = ButtonInternal(
    text = text,
    onClick = onClick,
    style = ButtonStyle.Filled,
    modifier = modifier,
    enabled = enabled,
    size = size,
    showProgress = showProgress,
    destructive = destructive,
    leadingIcon = leadingIcon
)

@Composable
fun OutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: ButtonSize = ButtonSize.Large,
    showProgress: Boolean = false,
    destructive: Boolean = false,
    leadingIcon: IconSource? = null,
) = ButtonInternal(
    text = text,
    onClick = onClick,
    style = ButtonStyle.Outlined,
    modifier = modifier,
    enabled = enabled,
    size = size,
    showProgress = showProgress,
    destructive = destructive,
    leadingIcon = leadingIcon
)

@Composable
fun TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: ButtonSize = ButtonSize.Large,
    showProgress: Boolean = false,
    destructive: Boolean = false,
    leadingIcon: IconSource? = null,
) = ButtonInternal(
    text = text,
    onClick = onClick,
    style = ButtonStyle.Text,
    modifier = modifier,
    enabled = enabled,
    size = size,
    showProgress = showProgress,
    destructive = destructive,
    leadingIcon = leadingIcon
)

@Composable
fun InvisibleButton(
    modifier: Modifier = Modifier,
    size: ButtonSize = ButtonSize.Large,
) {
    Spacer(modifier = modifier.height(size.toMinHeight()))
}

@Composable
private fun ButtonInternal(
    text: String,
    onClick: () -> Unit,
    style: ButtonStyle,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
    colors: ButtonColors = style.getColors(destructive),
    enabled: Boolean = true,
    size: ButtonSize = ButtonSize.Large,
    showProgress: Boolean = false,
    leadingIcon: IconSource? = null,
) {
    val minHeight = size.toMinHeight()
    val hasStartDrawable = showProgress || leadingIcon != null

    val contentPadding = when (size) {
        ButtonSize.Small -> {
            if (hasStartDrawable) {
                PaddingValues(start = 8.dp, top = 5.dp, end = 16.dp, bottom = 5.dp)
            } else {
                PaddingValues(start = 16.dp, top = 5.dp, end = 16.dp, bottom = 5.dp)
            }
        }
        ButtonSize.Medium -> when (style) {
            ButtonStyle.Filled,
            ButtonStyle.Outlined -> if (hasStartDrawable) {
                PaddingValues(start = 16.dp, top = 10.dp, end = 24.dp, bottom = 10.dp)
            } else {
                PaddingValues(start = 24.dp, top = 10.dp, end = 24.dp, bottom = 10.dp)
            }
            ButtonStyle.Text -> if (hasStartDrawable) {
                PaddingValues(start = 12.dp, top = 10.dp, end = 16.dp, bottom = 10.dp)
            } else {
                PaddingValues(start = 12.dp, top = 10.dp, end = 12.dp, bottom = 10.dp)
            }
        }
        ButtonSize.MediumLowPadding -> PaddingValues(horizontal = lowHorizontalPaddingValue, vertical = 10.dp)
        ButtonSize.Large -> when (style) {
            ButtonStyle.Filled,
            ButtonStyle.Outlined -> if (hasStartDrawable) {
                PaddingValues(start = 24.dp, top = 13.dp, end = 32.dp, bottom = 13.dp)
            } else {
                PaddingValues(start = 32.dp, top = 13.dp, end = 32.dp, bottom = 13.dp)
            }
            ButtonStyle.Text -> if (hasStartDrawable) {
                PaddingValues(start = 12.dp, top = 13.dp, end = 16.dp, bottom = 13.dp)
            } else {
                PaddingValues(start = 16.dp, top = 13.dp, end = 16.dp, bottom = 13.dp)
            }
        }
        ButtonSize.LargeLowPadding -> PaddingValues(horizontal = lowHorizontalPaddingValue, vertical = 13.dp)
    }

    val shape = when (style) {
        ButtonStyle.Filled,
        ButtonStyle.Outlined -> RoundedCornerShape(percent = 50)
        ButtonStyle.Text -> RectangleShape
    }

    val border = when (style) {
        ButtonStyle.Filled -> null
        ButtonStyle.Outlined -> BorderStroke(
            width = 1.dp,
            color = if (destructive) {
                ElementTheme.colors.borderCriticalPrimary.copy(
                    alpha = if (enabled) 1f else 0.5f
                )
            } else {
                ElementTheme.colors.borderInteractiveSecondary
            }
        )
        ButtonStyle.Text -> null
    }

    androidx.compose.material3.Button(
        onClick = {
            if (!showProgress) {
                onClick()
            }
        },
        modifier = modifier.heightIn(min = minHeight),
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = null,
        border = border,
        contentPadding = contentPadding,
        interactionSource = remember { MutableInteractionSource() },
    ) {
        when {
            showProgress -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .progressSemantics()
                        .size(20.dp),
                    color = LocalContentColor.current,
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            leadingIcon != null -> {
                androidx.compose.material.Icon(
                    painter = leadingIcon.getPainter(),
                    contentDescription = null,
                    tint = LocalContentColor.current,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        Text(
            text = text,
            style = ElementTheme.typography.fontBodyLgMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun ButtonSize.toMinHeight() = when (this) {
    ButtonSize.Small -> 32.dp
    ButtonSize.Medium,
    ButtonSize.MediumLowPadding -> 40.dp
    ButtonSize.Large,
    ButtonSize.LargeLowPadding -> 48.dp
}

@Immutable
sealed interface IconSource {
    val contentDescription: String?

    data class Resource(val id: Int, override val contentDescription: String? = null) : IconSource
    data class Vector(val vector: ImageVector, override val contentDescription: String? = null) : IconSource

    @Composable
    fun getPainter(): Painter = when (this) {
        is Resource -> rememberVectorPainter(image = ImageVector.vectorResource(id))
        is Vector -> rememberVectorPainter(image = vector)
    }
}

enum class ButtonSize {
    Small,
    Medium,

    /**
     * Like [Medium] but with minimal horizontal padding, so that large texts have less risk to get truncated.
     * To be used for instance for button with weight which ensures a maximal width.
     */
    MediumLowPadding,
    Large,

    /**
     * Like [Large] but with minimal horizontal padding, so that large texts have less risk to get truncated.
     * To be used for instance for button with weight which ensures a maximal width.
     */
    LargeLowPadding,
}

internal enum class ButtonStyle {
    Filled,
    Outlined,
    Text;

    @Composable
    fun getColors(destructive: Boolean): ButtonColors = when (this) {
        Filled -> ButtonDefaults.buttonColors(
            containerColor = getPrimaryColor(destructive),
            contentColor = ElementTheme.materialColors.onPrimary,
            disabledContainerColor = if (destructive) {
                ElementTheme.colors.bgCriticalPrimary.copy(alpha = 0.5f)
            } else {
                ElementTheme.colors.bgActionPrimaryDisabled
            },
            disabledContentColor = ElementTheme.colors.textOnSolidPrimary
        )
        Outlined -> ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = getPrimaryColor(destructive),
            disabledContainerColor = Color.Transparent,
            disabledContentColor = getDisabledContentColor(destructive),
        )
        Text -> ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = if (destructive) {
                ElementTheme.colors.textCriticalPrimary
            } else {
                if (LocalContentColor.current.isSpecified) LocalContentColor.current else ElementTheme.colors.textPrimary
            },
            disabledContainerColor = Color.Transparent,
            disabledContentColor = getDisabledContentColor(destructive),
        )
    }

    @Composable
    private fun getPrimaryColor(destructive: Boolean): Color {
        return if (destructive) {
            ElementTheme.colors.bgCriticalPrimary
        } else {
            ElementTheme.materialColors.primary
        }
    }

    @Composable
    private fun getDisabledContentColor(destructive: Boolean): Color {
        return if (destructive) {
            ElementTheme.colors.textCriticalPrimary.copy(alpha = 0.5f)
        } else {
            ElementTheme.colors.textDisabled
        }
    }
}

@Preview(group = PreviewGroup.Buttons)
@Composable
internal fun FilledButtonSmallPreview() {
    ButtonCombinationPreview(
        style = ButtonStyle.Filled,
        size = ButtonSize.Small,
    )
}

@Preview(group = PreviewGroup.Buttons)
@Composable
internal fun FilledButtonMediumPreview() {
    ButtonCombinationPreview(
        style = ButtonStyle.Filled,
        size = ButtonSize.Medium,
    )
}

@Preview(group = PreviewGroup.Buttons)
@Composable
internal fun FilledButtonMediumLowPaddingPreview() {
    ButtonCombinationPreview(
        style = ButtonStyle.Filled,
        size = ButtonSize.MediumLowPadding,
    )
}

@Preview(group = PreviewGroup.Buttons)
@Composable
internal fun FilledButtonLargePreview() {
    ButtonCombinationPreview(
        style = ButtonStyle.Filled,
        size = ButtonSize.Large,
    )
}

@Preview(group = PreviewGroup.Buttons)
@Composable
internal fun FilledButtonLargeLowPaddingPreview() {
    ButtonCombinationPreview(
        style = ButtonStyle.Filled,
        size = ButtonSize.LargeLowPadding,
    )
}

@Preview(group = PreviewGroup.Buttons)
@Composable
internal fun OutlinedButtonSmallPreview() {
    ButtonCombinationPreview(
        style = ButtonStyle.Outlined,
        size = ButtonSize.Small,
    )
}

@Preview(group = PreviewGroup.Buttons)
@Composable
internal fun OutlinedButtonMediumPreview() {
    ButtonCombinationPreview(
        style = ButtonStyle.Outlined,
        size = ButtonSize.Medium,
    )
}

@Preview(group = PreviewGroup.Buttons)
@Composable
internal fun OutlinedButtonMediumLowPaddingPreview() {
    ButtonCombinationPreview(
        style = ButtonStyle.Outlined,
        size = ButtonSize.MediumLowPadding,
    )
}

@Preview(group = PreviewGroup.Buttons)
@Composable
internal fun OutlinedButtonLargePreview() {
    ButtonCombinationPreview(
        style = ButtonStyle.Outlined,
        size = ButtonSize.Large,
    )
}

@Preview(group = PreviewGroup.Buttons)
@Composable
internal fun OutlinedButtonLargeLowPaddingPreview() {
    ButtonCombinationPreview(
        style = ButtonStyle.Outlined,
        size = ButtonSize.LargeLowPadding,
    )
}

@Preview(group = PreviewGroup.Buttons)
@Composable
internal fun TextButtonSmallPreview() {
    ButtonCombinationPreview(
        style = ButtonStyle.Text,
        size = ButtonSize.Small,
    )
}

@Preview(group = PreviewGroup.Buttons)
@Composable
internal fun TextButtonMediumPreview() {
    ButtonCombinationPreview(
        style = ButtonStyle.Text,
        size = ButtonSize.Medium,
    )
}

@Preview(group = PreviewGroup.Buttons)
@Composable
internal fun TextButtonMediumLowPaddingPreview() {
    ButtonCombinationPreview(
        style = ButtonStyle.Text,
        size = ButtonSize.MediumLowPadding,
    )
}

@Preview(group = PreviewGroup.Buttons)
@Composable
internal fun TextButtonLargePreview() {
    ButtonCombinationPreview(
        style = ButtonStyle.Text,
        size = ButtonSize.Large,
    )
}

@Preview(group = PreviewGroup.Buttons)
@Composable
internal fun TextButtonLargeLowPaddingPreview() {
    ButtonCombinationPreview(
        style = ButtonStyle.Text,
        size = ButtonSize.LargeLowPadding,
    )
}

@Composable
private fun ButtonCombinationPreview(
    style: ButtonStyle,
    size: ButtonSize,
) {
    ElementThemedPreview {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(16.dp)
                .width(IntrinsicSize.Max),
        ) {
            ButtonMatrixPreview(style = style, size = size, destructive = false)
            ButtonMatrixPreview(style = style, size = size, destructive = true)
        }
    }
}

@Composable
private fun ColumnScope.ButtonMatrixPreview(
    style: ButtonStyle,
    size: ButtonSize,
    destructive: Boolean,
) {
    // Normal
    ButtonRowPreview(
        style = style,
        size = size,
        destructive = destructive,
    )
    // With icon
    ButtonRowPreview(
        leadingIcon = IconSource.Vector(CompoundIcons.ShareAndroid()),
        style = style,
        size = size,
        destructive = destructive,
    )
    // With progress
    ButtonRowPreview(
        showProgress = true,
        style = style,
        size = size,
        destructive = destructive,
    )
}

@Composable
private fun ButtonRowPreview(
    style: ButtonStyle,
    size: ButtonSize,
    leadingIcon: IconSource? = null,
    showProgress: Boolean = false,
    destructive: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        ButtonInternal(
            text = "A button",
            showProgress = showProgress,
            destructive = destructive,
            onClick = {},
            style = style,
            size = size,
            leadingIcon = leadingIcon,
        )
        ButtonInternal(
            text = "A button",
            showProgress = showProgress,
            destructive = destructive,
            enabled = false,
            onClick = {},
            style = style,
            size = size,
            leadingIcon = leadingIcon,
        )
    }
}
