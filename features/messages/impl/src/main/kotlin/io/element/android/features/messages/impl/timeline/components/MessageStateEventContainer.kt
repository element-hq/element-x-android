/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.modifiers.onKeyboardContextMenuAction
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.ui.strings.CommonStrings

private val CORNER_RADIUS = 8.dp

@Composable
fun MessageStateEventContainer(
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    val backgroundColor = Color.Transparent
    val shape = RoundedCornerShape(CORNER_RADIUS)
    Surface(
        modifier = modifier
            .widthIn(min = 80.dp)
            .clip(shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                onLongClickLabel = stringResource(CommonStrings.action_open_context_menu),
                indication = ripple(),
                interactionSource = interactionSource
            )
            .onKeyboardContextMenuAction(onLongClick),
        color = backgroundColor,
        shape = shape,
        content = content
    )
}

@PreviewsDayNight
@Composable
internal fun MessageStateEventContainerPreview() = ElementPreview {
    MessageStateEventContainer(
        interactionSource = remember { MutableInteractionSource() },
        onClick = {},
        onLongClick = {},
    )
}
