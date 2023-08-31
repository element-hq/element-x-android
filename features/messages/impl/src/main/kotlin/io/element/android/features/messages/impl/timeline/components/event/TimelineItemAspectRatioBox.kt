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

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private const val MAX_HEIGHT_IN_DP = 360f
private const val MIN_ASPECT_RATIO = 0.6f
private const val MAX_ASPECT_RATIO = 4f
private const val DEFAULT_ASPECT_RATIO = 1.33f

@Composable
fun TimelineItemAspectRatioBox(
    aspectRatio: Float?,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable (BoxScope.() -> Unit),
) {
    val safeAspectRatio = (aspectRatio.takeIf { it?.isFinite() == true } ?: DEFAULT_ASPECT_RATIO)
        .coerceIn(MIN_ASPECT_RATIO, MAX_ASPECT_RATIO)
    Box(
        modifier = modifier
            .heightIn(max = MAX_HEIGHT_IN_DP.dp)
            .aspectRatio(safeAspectRatio, true),
        contentAlignment = contentAlignment,
        content = content
    )
}
