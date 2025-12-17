/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon

@Composable
internal fun FormattingOption(
    state: FormattingOptionState,
    toggleable: Boolean,
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = when (state) {
        FormattingOptionState.Selected -> ElementTheme.colors.bgAccentSelected
        FormattingOptionState.Default,
        FormattingOptionState.Disabled -> Color.Transparent
    }

    val foregroundColor = when (state) {
        FormattingOptionState.Selected -> ElementTheme.colors.iconAccentPrimary
        FormattingOptionState.Default -> ElementTheme.colors.iconSecondary
        FormattingOptionState.Disabled -> ElementTheme.colors.iconDisabled
    }
    Box(
        modifier = modifier
            .clickable(
                onClick = onClick,
                enabled = state != FormattingOptionState.Disabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    bounded = false,
                    radius = 20.dp,
                ),
            )
            .size(48.dp)
            .then(
                if (toggleable) {
                    Modifier.toggleable(
                        value = state == FormattingOptionState.Selected,
                        enabled = state != FormattingOptionState.Disabled,
                        onValueChange = { onClick() },
                    )
                } else {
                    Modifier
                }
            )
            .clearAndSetSemantics {
                this.contentDescription = contentDescription
            }
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.Center)
                .background(backgroundColor, shape = RoundedCornerShape(8.dp))
        ) {
            Icon(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(20.dp),
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = foregroundColor,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun FormattingOptionPreview() = ElementPreview {
    Row {
        FormattingOption(
            state = FormattingOptionState.Default,
            toggleable = false,
            onClick = { },
            imageVector = CompoundIcons.Bold(),
            contentDescription = "",
        )
        FormattingOption(
            state = FormattingOptionState.Selected,
            toggleable = true,
            onClick = { },
            imageVector = CompoundIcons.Italic(),
            contentDescription = "",
        )
        FormattingOption(
            state = FormattingOptionState.Disabled,
            toggleable = false,
            onClick = { },
            imageVector = CompoundIcons.Underline(),
            contentDescription = "",
        )
    }
}
