/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Surface

private val CORNER_RADIUS = 8.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageStateEventContainer(
    @Suppress("UNUSED_PARAMETER") isHighlighted: Boolean,
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    // Ignore isHighlighted for now, we need a design decision on it.
    val backgroundColor = Color.Transparent
    val shape = RoundedCornerShape(CORNER_RADIUS)
    Surface(
        modifier = modifier
            .widthIn(min = 80.dp)
            .clip(shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                indication = ripple(),
                interactionSource = interactionSource
            ),
        color = backgroundColor,
        shape = shape,
        content = content
    )
}

@PreviewsDayNight
@Composable
internal fun MessageStateEventContainerPreview() = ElementPreview {
    Column {
        MessageStateEventContainer(
            isHighlighted = false,
            interactionSource = remember { MutableInteractionSource() },
            onClick = {},
            onLongClick = {},
        ) {
            Spacer(modifier = Modifier.size(width = 120.dp, height = 32.dp))
        }
        MessageStateEventContainer(
            isHighlighted = true,
            interactionSource = remember { MutableInteractionSource() },
            onClick = {},
            onLongClick = {},
        ) {
            Spacer(modifier = Modifier.size(width = 120.dp, height = 32.dp))
        }
    }
}
