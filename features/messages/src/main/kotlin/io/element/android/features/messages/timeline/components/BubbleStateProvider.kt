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

package io.element.android.features.messages.timeline.components

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.messages.timeline.model.TimelineItemGroupPosition

open class BubbleStateProvider : PreviewParameterProvider<BubbleState> {
    override val values: Sequence<BubbleState>
        get() = sequenceOf(
            BubbleState(TimelineItemGroupPosition.First, isMine = false, isHighlighted = false),
            BubbleState(TimelineItemGroupPosition.First, isMine = false, isHighlighted = true),
            BubbleState(TimelineItemGroupPosition.First, isMine = false, isHighlighted = false),
            BubbleState(TimelineItemGroupPosition.First, isMine = false, isHighlighted = true),
            BubbleState(TimelineItemGroupPosition.First, isMine = true, isHighlighted = false),
            BubbleState(TimelineItemGroupPosition.First, isMine = true, isHighlighted = true),
            BubbleState(TimelineItemGroupPosition.First, isMine = true, isHighlighted = false),
            BubbleState(TimelineItemGroupPosition.First, isMine = true, isHighlighted = true),

            BubbleState(TimelineItemGroupPosition.Middle, isMine = false, isHighlighted = false),
            BubbleState(TimelineItemGroupPosition.Middle, isMine = false, isHighlighted = true),
            BubbleState(TimelineItemGroupPosition.Middle, isMine = false, isHighlighted = false),
            BubbleState(TimelineItemGroupPosition.Middle, isMine = false, isHighlighted = true),
            BubbleState(TimelineItemGroupPosition.Middle, isMine = true, isHighlighted = false),
            BubbleState(TimelineItemGroupPosition.Middle, isMine = true, isHighlighted = true),
            BubbleState(TimelineItemGroupPosition.Middle, isMine = true, isHighlighted = false),
            BubbleState(TimelineItemGroupPosition.Middle, isMine = true, isHighlighted = true),

            BubbleState(TimelineItemGroupPosition.Last, isMine = false, isHighlighted = false),
            BubbleState(TimelineItemGroupPosition.Last, isMine = false, isHighlighted = true),
            BubbleState(TimelineItemGroupPosition.Last, isMine = false, isHighlighted = false),
            BubbleState(TimelineItemGroupPosition.Last, isMine = false, isHighlighted = true),
            BubbleState(TimelineItemGroupPosition.Last, isMine = true, isHighlighted = false),
            BubbleState(TimelineItemGroupPosition.Last, isMine = true, isHighlighted = true),
            BubbleState(TimelineItemGroupPosition.Last, isMine = true, isHighlighted = false),
            BubbleState(TimelineItemGroupPosition.Last, isMine = true, isHighlighted = true),
        )
}

fun aBubbleState() = BubbleState(
    groupPosition = TimelineItemGroupPosition.First,
    isMine = false,
    isHighlighted = false,
)
