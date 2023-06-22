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

package io.element.android.features.messages.impl.timeline.model

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class AggregatedReactionProvider : PreviewParameterProvider<AggregatedReaction> {
    override val values: Sequence<AggregatedReaction>
        get() = sequenceOf(false, true).flatMap {
            sequenceOf(
                anAggregatedReaction(isOnMyMessage = it),
                anAggregatedReaction(isOnMyMessage = it, count = "88"),
                anAggregatedReaction(isOnMyMessage = it, isHighlighted = true),
                anAggregatedReaction(isOnMyMessage = it, count = "88", isHighlighted = true),
            )
        }
}

fun anAggregatedReaction(
    key: String = "👍",
    count: String = "1", // TODO Why is it a String?
    isOnMyMessage: Boolean = false,
    isHighlighted: Boolean = false,
) = AggregatedReaction(
    key = key,
    count = count,
    isOnMyMessage = isOnMyMessage,
    isHighlighted = isHighlighted,
)
