/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon

/**
 * A swipe indicator that appears when swiping to reply to a message.
 *
 * @param swipeProgress the progress of the swipe, between 0 and X. When swipeProgress >= 1 the swipe will be detected.
 * @param modifier the modifier to apply to this Composable root.
 */
@Composable
fun RowScope.ReplySwipeIndicator(
    swipeProgress: () -> Float,
    modifier: Modifier = Modifier,
) {
    Icon(
        modifier = modifier
            .align(Alignment.CenterVertically)
            .graphicsLayer {
                translationX = 36.dp.toPx() * swipeProgress().coerceAtMost(1f)
                alpha = swipeProgress()
            },
        contentDescription = null,
        imageVector = CompoundIcons.Reply(),
    )
}

@PreviewsDayNight
@Composable
internal fun ReplySwipeIndicatorPreview() = ElementPreview {
    Column(modifier = Modifier.fillMaxWidth()) {
        for (i in 0..8) {
            Row { ReplySwipeIndicator(swipeProgress = { i / 8f }) }
        }
        Row { ReplySwipeIndicator(swipeProgress = { 1.5f }) }
        Row { ReplySwipeIndicator(swipeProgress = { 2f }) }
        Row { ReplySwipeIndicator(swipeProgress = { 3f }) }
    }
}
