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
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.ReactionSender
import kotlinx.collections.immutable.toPersistentList
import java.util.Date

open class AggregatedReactionProvider : PreviewParameterProvider<AggregatedReaction> {
    override val values: Sequence<AggregatedReaction>
        get() = sequenceOf(false, true).flatMap {
            sequenceOf(
                anAggregatedReaction(isHighlighted = it),
                anAggregatedReaction(isHighlighted = it, count = 88),
            )
        }
}

fun anAggregatedReaction(
    key: String = "👍",
    count: Int = 1,
    isHighlighted: Boolean = false,
): AggregatedReaction {
    val alice = UserId("@alice:server.org")
    val senders = buildList {
        repeat(count) { index ->
            add(
                ReactionSender(
                    senderId = if (isHighlighted && index == 0) alice else UserId("@user$index:server.org"),
                    timestamp = Date()
                )
            )
        }
    }
    return AggregatedReaction(
        currentUserId = alice,
        key = key,
        senders = senders
    )
}
