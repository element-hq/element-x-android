/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
