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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.theme.ElementTheme

// Designs: https://www.figma.com/file/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?type=design&mode=design&t=U03tOFZz5FSLVUMa-1

@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    buttonSize: ButtonSize = ButtonSize.Large,
    showProgress: Boolean = false,
    leadingIcon: IconSource? = null,
) = ButtonInternal(text, onClick, ButtonStyle.Filled, modifier, enabled, buttonSize, showProgress, leadingIcon)

@Composable
fun OutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    buttonSize: ButtonSize = ButtonSize.Large,
    showProgress: Boolean = false,
    leadingIcon: IconSource? = null,
) = ButtonInternal(text, onClick, ButtonStyle.Outlined, modifier, enabled, buttonSize, showProgress, leadingIcon)

@Composable
fun TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    buttonSize: ButtonSize = ButtonSize.Large,
    showProgress: Boolean = false,
    leadingIcon: IconSource? = null,
) = ButtonInternal(text, onClick, ButtonStyle.Text, modifier, enabled, buttonSize, showProgress, leadingIcon)

@Composable
private fun ButtonInternal(
    text: String,
    onClick: () -> Unit,
    style: ButtonStyle,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: ButtonSize = ButtonSize.Large,
    showProgress: Boolean = false,
    leadingIcon: IconSource? = null,
) {
    val minHeight = when (size) {
        ButtonSize.Medium -> 40.dp
        ButtonSize.Large -> 48.dp
    }

    val contentPadding = when (size) {
        ButtonSize.Medium -> {
            when (style) {
                ButtonStyle.Text -> PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                else -> PaddingValues(horizontal = 16.dp, vertical = 10.dp)
            }
        }
        ButtonSize.Large -> {
            when (style) {
                ButtonStyle.Text -> PaddingValues(horizontal = 16.dp, vertical = 13.dp)
                else -> PaddingValues(horizontal = 24.dp, vertical = 13.dp)
            }
        }
    }

    val shape = when (style) {
        ButtonStyle.Filled, ButtonStyle.Outlined -> RoundedCornerShape(percent = 50)
        ButtonStyle.Text -> RectangleShape
    }

    val colors = when (style) {
        ButtonStyle.Filled -> ButtonDefaults.buttonColors(
            containerColor = ElementTheme.materialColors.primary,
            contentColor = ElementTheme.materialColors.onPrimary,
            disabledContainerColor = ElementTheme.colors.bgActionPrimaryDisabled,
            disabledContentColor = ElementTheme.colors.textOnSolidPrimary
        )
        ButtonStyle.Outlined, ButtonStyle.Text -> ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = ElementTheme.materialColors.primary,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = ElementTheme.colors.textDisabled,
        )
    }

    val border = when (style) {
        ButtonStyle.Filled, ButtonStyle.Text -> null
        ButtonStyle.Outlined -> BorderStroke(
            width = 1.dp,
            color = ElementTheme.colors.borderInteractiveSecondary
        )
    }

    val textStyle = when (size) {
        ButtonSize.Medium -> MaterialTheme.typography.labelLarge
        ButtonSize.Large -> ElementTheme.typography.fontBodyLgMedium
    }

    val internalPadding = when {
        style == ButtonStyle.Text -> if (leadingIcon != null) PaddingValues(start = 8.dp) else PaddingValues(0.dp)
        else -> PaddingValues(horizontal = 8.dp)
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
            }
            leadingIcon != null -> {
                androidx.compose.material.Icon(
                    painter = leadingIcon.getPainter(),
                    contentDescription = null,
                    tint = LocalContentColor.current,
                    modifier = Modifier.size(20.dp),
                )
            }
            else -> Unit
        }
        Text(
            text = text,
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(internalPadding),
        )
    }
}

sealed interface IconSource {
    data class Resource(val id: Int) : IconSource
    data class Vector(val vector: ImageVector) : IconSource

    @Composable
    fun getPainter(): Painter = when (this) {
        is Resource -> painterResource(id)
        is Vector -> rememberVectorPainter(image = vector)
    }
}

enum class ButtonSize {
    Medium, Large
}

private enum class ButtonStyle {
    Filled, Outlined, Text
}

@Preview(group = PreviewGroup.Buttons)
@Composable
internal fun FilledButtonMediumPreview() {
    ButtonCombinationPreview(
        buttonStyle = ButtonStyle.Filled,
        buttonSize = ButtonSize.Medium,
    )
}

@Preview(group = PreviewGroup.Buttons)
@Composable
internal fun FilledButtonLargePreview() {
    ButtonCombinationPreview(
        buttonStyle = ButtonStyle.Filled,
        buttonSize = ButtonSize.Large,
    )
}

@Preview(group = PreviewGroup.Buttons)
@Composable
internal fun OutlinedButtonMediumPreview() {
    ButtonCombinationPreview(
        buttonStyle = ButtonStyle.Outlined,
        buttonSize = ButtonSize.Medium,
    )
}

@Preview(group = PreviewGroup.Buttons)
@Composable
internal fun OutlinedButtonLargePreview() {
    ButtonCombinationPreview(
        buttonStyle = ButtonStyle.Outlined,
        buttonSize = ButtonSize.Large,
    )
}

@Preview(group = PreviewGroup.Buttons)
@Composable
internal fun TextButtonMediumPreview() {
    ButtonCombinationPreview(
        buttonStyle = ButtonStyle.Text,
        buttonSize = ButtonSize.Medium,
    )
}

@Preview(group = PreviewGroup.Buttons)
@Composable
internal fun TextButtonLargePreview() {
    ButtonCombinationPreview(
        buttonStyle = ButtonStyle.Text,
        buttonSize = ButtonSize.Large,
    )
}

@Composable
private fun ButtonCombinationPreview(
    buttonStyle: ButtonStyle,
    buttonSize: ButtonSize,
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
                buttonStyle = buttonStyle,
                buttonSize = buttonSize,
            )

            // With icon
            ButtonRowPreview(
                modifier = Modifier.then(modifier),
                leadingIcon = IconSource.Vector(Icons.Outlined.Share),
                buttonStyle = buttonStyle,
                buttonSize = buttonSize,
            )

            // With progress
            ButtonRowPreview(
                modifier = Modifier.then(modifier),
                showProgress = true,
                buttonStyle = buttonStyle,
                buttonSize = buttonSize,
            )
        }
    }
}

@Composable
private fun ButtonRowPreview(
    buttonStyle: ButtonStyle,
    buttonSize: ButtonSize,
    modifier: Modifier = Modifier,
    leadingIcon: IconSource? = null,
    showProgress: Boolean = false,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
        ButtonInternal(
            text = "A button",
            showProgress = showProgress,
            onClick = {},
            style = buttonStyle,
            size = buttonSize,
            leadingIcon = leadingIcon,
            modifier = Modifier.then(modifier),
        )
        ButtonInternal(
            text = "A button",
            showProgress = showProgress,
            enabled = false,
            onClick = {},
            style = buttonStyle,
            size = buttonSize,
            leadingIcon = leadingIcon,
            modifier = Modifier.then(modifier),
        )
    }
}
