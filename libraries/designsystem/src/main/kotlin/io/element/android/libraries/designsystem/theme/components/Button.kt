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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.theme.ElementTheme

// Designs: https://www.figma.com/file/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?type=design&mode=design&t=U03tOFZz5FSLVUMa-1

@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: ButtonSize = ButtonSize.Large,
    showProgress: Boolean = false,
    leadingIcon: IconSource? = null,
) = ButtonInternal(
    text = text,
    onClick = onClick,
    style = ButtonStyle.Filled,
    modifier = modifier,
    enabled = enabled,
    size = size,
    showProgress = showProgress,
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
    leadingIcon: IconSource? = null,
) = ButtonInternal(
    text = text,
    onClick = onClick,
    style = ButtonStyle.Outlined,
    modifier = modifier,
    enabled = enabled,
    size = size,
    showProgress = showProgress,
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
    leadingIcon: IconSource? = null,
) = ButtonInternal(
    text = text,
    onClick = onClick,
    style = ButtonStyle.Text,
    modifier = modifier,
    enabled = enabled,
    size = size,
    showProgress = showProgress,
    leadingIcon = leadingIcon
)

@Composable
private fun ButtonInternal(
    text: String,
    onClick: () -> Unit,
    style: ButtonStyle,
    modifier: Modifier = Modifier,
    colors: ButtonColors = style.getColors(),
    enabled: Boolean = true,
    size: ButtonSize = ButtonSize.Large,
    showProgress: Boolean = false,
    leadingIcon: IconSource? = null,
) {
    val minHeight = when (size) {
        ButtonSize.Medium -> 40.dp
        ButtonSize.Large -> 48.dp
    }

    val hasStartDrawable = showProgress || leadingIcon != null

    val contentPadding = when (size) {
        ButtonSize.Medium -> when (style) {
            ButtonStyle.Filled,
            ButtonStyle.Outlined -> if (hasStartDrawable)
                PaddingValues(start = 16.dp, top = 10.dp, end = 24.dp, bottom = 10.dp)
            else
                PaddingValues(start = 24.dp, top = 10.dp, end = 24.dp, bottom = 10.dp)
            ButtonStyle.Text -> if (hasStartDrawable)
                PaddingValues(start = 12.dp, top = 10.dp, end = 16.dp, bottom = 10.dp)
            else
                PaddingValues(start = 12.dp, top = 10.dp, end = 12.dp, bottom = 10.dp)
        }
        ButtonSize.Large -> when (style) {
            ButtonStyle.Filled,
            ButtonStyle.Outlined -> if (hasStartDrawable)
                PaddingValues(start = 24.dp, top = 13.dp, end = 32.dp, bottom = 13.dp)
            else
                PaddingValues(start = 32.dp, top = 13.dp, end = 32.dp, bottom = 13.dp)
            ButtonStyle.Text -> if (hasStartDrawable)
                PaddingValues(start = 12.dp, top = 13.dp, end = 16.dp, bottom = 13.dp)
            else
                PaddingValues(start = 16.dp, top = 13.dp, end = 16.dp, bottom = 13.dp)
        }
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
            color = ElementTheme.colors.borderInteractiveSecondary
        )
        ButtonStyle.Text -> null
    }

    val textStyle = when (size) {
        ButtonSize.Medium -> MaterialTheme.typography.labelLarge
        ButtonSize.Large -> ElementTheme.typography.fontBodyLgMedium
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
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

sealed interface IconSource {
    val contentDescription: String?

    data class Resource(val id: Int, override val contentDescription: String? = null) : IconSource
    data class Vector(val vector: ImageVector, override val contentDescription: String? = null) : IconSource

    @Composable
    fun getPainter(): Painter = when (this) {
        is Resource -> painterResource(id)
        is Vector -> rememberVectorPainter(image = vector)
    }
}

enum class ButtonSize {
    Medium, Large
}

internal enum class ButtonStyle {
    Filled, Outlined, Text;

    @Composable
    fun getColors(): ButtonColors = when (this) {
        Filled -> ButtonDefaults.buttonColors(
            containerColor = ElementTheme.materialColors.primary,
            contentColor = ElementTheme.materialColors.onPrimary,
            disabledContainerColor = ElementTheme.colors.bgActionPrimaryDisabled,
            disabledContentColor = ElementTheme.colors.textOnSolidPrimary
        )
        Outlined -> ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = ElementTheme.materialColors.primary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = ElementTheme.colors.textDisabled,
        )
        Text -> ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = if (LocalContentColor.current.isSpecified) LocalContentColor.current else ElementTheme.materialColors.primary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = ElementTheme.colors.textDisabled,
        )
    }
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
internal fun FilledButtonLargePreview() {
    ButtonCombinationPreview(
        style = ButtonStyle.Filled,
        size = ButtonSize.Large,
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
internal fun OutlinedButtonLargePreview() {
    ButtonCombinationPreview(
        style = ButtonStyle.Outlined,
        size = ButtonSize.Large,
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
internal fun TextButtonLargePreview() {
    ButtonCombinationPreview(
        style = ButtonStyle.Text,
        size = ButtonSize.Large,
    )
}

@Composable
private fun ButtonCombinationPreview(
    style: ButtonStyle,
    size: ButtonSize,
    modifier: Modifier = Modifier,
) {
    ElementThemedPreview {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(16.dp)
                .width(IntrinsicSize.Max),
        ) {
            // Normal
            ButtonRowPreview(
                modifier = Modifier.then(modifier),
                style = style,
                size = size,
            )

            // With icon
            ButtonRowPreview(
                modifier = Modifier.then(modifier),
                leadingIcon = IconSource.Resource(CommonDrawables.ic_compound_share_android),
                style = style,
                size = size,
            )

            // With progress
            ButtonRowPreview(
                modifier = Modifier.then(modifier),
                showProgress = true,
                style = style,
                size = size,
            )
        }
    }
}

@Composable
private fun ButtonRowPreview(
    style: ButtonStyle,
    size: ButtonSize,
    modifier: Modifier = Modifier,
    leadingIcon: IconSource? = null,
    showProgress: Boolean = false,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
        ButtonInternal(
            text = "A button",
            showProgress = showProgress,
            onClick = {},
            style = style,
            size = size,
            leadingIcon = leadingIcon,
            modifier = Modifier.then(modifier),
        )
        ButtonInternal(
            text = "A button",
            showProgress = showProgress,
            enabled = false,
            onClick = {},
            style = style,
            size = size,
            leadingIcon = leadingIcon,
            modifier = Modifier.then(modifier),
        )
    }
}
