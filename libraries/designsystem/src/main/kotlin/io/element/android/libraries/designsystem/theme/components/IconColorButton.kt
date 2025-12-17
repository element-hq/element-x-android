/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

/**
 * Button with colored background.
 * Figma: https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=1956-37586
 */
@Composable
fun IconColorButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    buttonSize: ButtonSize = ButtonSize.Large,
    iconColorButtonStyle: IconColorButtonStyle = IconColorButtonStyle.Primary,
) {
    val bgColor = when (iconColorButtonStyle) {
        IconColorButtonStyle.Primary -> ElementTheme.colors.iconPrimary
        IconColorButtonStyle.Secondary -> ElementTheme.colors.iconSecondary
        IconColorButtonStyle.Disabled -> ElementTheme.colors.iconDisabled
    }
    IconButton(
        modifier = modifier.size(48.dp),
        onClick = onClick,
    ) {
        Icon(
            modifier = Modifier
                .clip(CircleShape)
                .size(buttonSize.toContainerSize())
                .background(bgColor)
                .padding(buttonSize.toContainerPadding()),
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = ElementTheme.colors.iconOnSolidPrimary
        )
    }
}

enum class IconColorButtonStyle {
    Primary,
    Secondary,
    Disabled,
}

private fun ButtonSize.toContainerSize() = when (this) {
    ButtonSize.Small -> 20.dp
    ButtonSize.Medium -> 24.dp
    ButtonSize.Large,
    ButtonSize.MediumLowPadding,
    ButtonSize.LargeLowPadding -> 30.dp
}

private fun ButtonSize.toContainerPadding() = when (this) {
    ButtonSize.Small -> 2.dp
    ButtonSize.Medium -> 2.dp
    ButtonSize.Large,
    ButtonSize.MediumLowPadding,
    ButtonSize.LargeLowPadding -> 3.dp
}

@PreviewsDayNight
@Composable
internal fun IconColorButtonPreview() = ElementPreview {
    Column {
        listOf(
            IconColorButtonStyle.Primary,
            IconColorButtonStyle.Secondary,
            IconColorButtonStyle.Disabled,
        ).forEach { style ->
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(ButtonSize.Large, ButtonSize.Medium, ButtonSize.Small).forEach { size ->
                    IconColorButton(
                        onClick = {},
                        imageVector = CompoundIcons.Close(),
                        contentDescription = null,
                        buttonSize = size,
                        iconColorButtonStyle = style,
                    )
                }
            }
        }
    }
}
