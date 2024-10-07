/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

const val MIN_HEIGHT_IN_DP = 100
const val MAX_HEIGHT_IN_DP = 360
const val DEFAULT_ASPECT_RATIO = 1.33f

@Composable
fun TimelineItemAspectRatioBox(
    aspectRatio: Float?,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    minHeight: Int = MIN_HEIGHT_IN_DP,
    maxHeight: Int = MAX_HEIGHT_IN_DP,
    content: @Composable (BoxScope.() -> Unit),
) {
    val safeAspectRatio = aspectRatio ?: DEFAULT_ASPECT_RATIO
    Box(
        modifier = modifier
            .heightIn(min = minHeight.dp, max = maxHeight.dp)
            .aspectRatio(safeAspectRatio, false),
        contentAlignment = contentAlignment,
        content = content
    )
}
