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

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEncryptedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemFileContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRedactedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemUnknownContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent

private fun Modifier.defaultContentPadding(): Modifier = padding(
    horizontal = 12.dp, vertical = 6.dp
)

@Composable
fun TimelineItemEventContentView(
    content: TimelineItemEventContent,
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (content) {
        is TimelineItemEncryptedContent -> TimelineItemEncryptedView(
            content = content,
            modifier = modifier.defaultContentPadding()
        )
        is TimelineItemRedactedContent -> TimelineItemRedactedView(
            content = content,
            modifier = modifier.defaultContentPadding()
        )
        is TimelineItemTextBasedContent -> TimelineItemTextView(
            content = content,
            interactionSource = interactionSource,
            modifier = modifier.defaultContentPadding(),
            onTextClicked = onClick,
            onTextLongClicked = onLongClick
        )
        is TimelineItemUnknownContent -> TimelineItemUnknownView(
            content = content,
            modifier = modifier.defaultContentPadding()
        )
        is TimelineItemImageContent -> TimelineItemImageView(
            content = content,
            modifier = modifier,
        )
        is TimelineItemVideoContent -> TimelineItemVideoView(
            content = content,
            modifier = modifier
        )
        is TimelineItemFileContent -> TimelineItemFileView(
            content = content,
            modifier = modifier.defaultContentPadding()
        )
        is TimelineItemStateContent -> TimelineItemStateView(
            content = content,
            modifier = modifier.defaultContentPadding()
        )
    }
}
