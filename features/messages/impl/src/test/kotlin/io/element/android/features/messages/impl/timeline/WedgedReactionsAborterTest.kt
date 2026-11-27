/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.SendTarget
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import io.element.android.libraries.matrix.api.timeline.item.event.EventReaction
import io.element.android.libraries.matrix.api.timeline.item.event.ReactionSender
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.test.runTest
import org.junit.Test

class WedgedReactionsAborterTest {
    private val abortCalls = mutableListOf<Pair<EventOrTransactionId, SendTarget>>()

    private fun createAborter(result: Result<Boolean> = Result.success(true)) = WedgedReactionsAborter(
        abortSend = { eventOrTransactionId, sendTarget ->
            abortCalls.add(eventOrTransactionId to sendTarget)
            result
        }
    )

    @Test
    fun `a wedged reaction is aborted`() = runTest {
        val aborter = createAborter()
        aborter.onEachMatrixTimelineItem(aTimeline(aReaction("👍", isSendWedged = true), aReaction("🎉", isSendWedged = false)))
        assertThat(abortCalls).containsExactly(EventOrTransactionId.Event(AN_EVENT_ID) to SendTarget.Reaction("👍"))
    }

    @Test
    fun `reactions which are not wedged are not aborted`() = runTest {
        val aborter = createAborter()
        aborter.onEachMatrixTimelineItem(aTimeline(aReaction("👍", isSendWedged = false)))
        assertThat(abortCalls).isEmpty()
    }

    @Test
    fun `a wedged reaction is aborted only once`() = runTest {
        val aborter = createAborter()
        val timeline = aTimeline(aReaction("👍", isSendWedged = true))
        aborter.onEachMatrixTimelineItem(timeline)
        aborter.onEachMatrixTimelineItem(timeline)
        assertThat(abortCalls).hasSize(1)
    }

    @Test
    fun `aborting is tried again on the next update if it failed`() = runTest {
        val aborter = createAborter(result = Result.failure(IllegalStateException("error")))
        val timeline = aTimeline(aReaction("👍", isSendWedged = true))
        aborter.onEachMatrixTimelineItem(timeline)
        aborter.onEachMatrixTimelineItem(timeline)
        assertThat(abortCalls).hasSize(2)
    }

    private fun aReaction(key: String, isSendWedged: Boolean) = EventReaction(
        key = key,
        senders = persistentListOf(
            ReactionSender(senderId = A_USER_ID_2, timestamp = 0L),
            ReactionSender(senderId = A_USER_ID, timestamp = 1L, isSendWedged = isSendWedged),
        ),
    )

    private fun aTimeline(vararg reactions: EventReaction) = listOf(
        MatrixTimelineItem.Event(
            uniqueId = UniqueId("0"),
            event = anEventTimelineItem(
                eventId = AN_EVENT_ID,
                reactions = reactions.toList().toImmutableList(),
            ),
        ),
    )
}
