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
import kotlinx.collections.immutable.toImmutableList
import java.text.DateFormat
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
    userId: UserId = UserId("@alice:server.org"),
    key: String = "👍",
    count: Int = 1,
    isHighlighted: Boolean = false,
): AggregatedReaction {
    val timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT, java.util.Locale.US)
    val date = Date(1_689_061_264L)
    val senders = buildList {
        repeat(count) { index ->
            add(
                AggregatedReactionSender(
                    senderId = if (isHighlighted && index == 0) userId else UserId("@user$index:server.org"),
                    timestamp = date,
                    sentTime = timeFormatter.format(date),
                )
            )
        }
    }
    return AggregatedReaction(
        currentUserId = userId,
        key = key,
        senders = senders.toImmutableList()
    )
}
