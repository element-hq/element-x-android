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
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.utils.CommonDrawables

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
        resourceId = CommonDrawables.ic_reply,
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
