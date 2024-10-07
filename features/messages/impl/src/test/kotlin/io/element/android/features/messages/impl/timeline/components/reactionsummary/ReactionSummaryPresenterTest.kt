/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.reactionsummary

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.timeline.model.anAggregatedReaction
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ReactionSummaryPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val aggregatedReaction = anAggregatedReaction(userId = A_USER_ID, key = "👍", isHighlighted = true)
    private val roomMember = aRoomMember(userId = A_USER_ID, avatarUrl = AN_AVATAR_URL, displayName = A_USER_NAME)
    private val summaryEvent = ReactionSummaryEvents.ShowReactionSummary(AN_EVENT_ID, listOf(aggregatedReaction), aggregatedReaction.key)
    private val buildMeta = aBuildMeta()
    private val room = FakeMatrixRoom().apply {
        givenRoomMembersState(MatrixRoomMembersState.Ready(persistentListOf(roomMember)))
    }
    private val presenter = ReactionSummaryPresenter(buildMeta, room)

    @Test
    fun `present - handle showing and hiding the reaction summary`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.target).isNull()

            initialState.eventSink(summaryEvent)
            assertThat(awaitItem().target).isNotNull()

            initialState.eventSink(ReactionSummaryEvents.Clear)
            assertThat(awaitItem().target).isNull()
        }
    }

    @Test
    fun `present - handle reaction summary content and avatars populated`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.target).isNull()

            initialState.eventSink(summaryEvent)
            val reactions = awaitItem().target?.reactions
            assertThat(reactions?.count()).isEqualTo(1)
            assertThat(reactions?.first()?.key).isEqualTo("👍")
            assertThat(reactions?.first()?.senders?.first()?.senderId).isEqualTo(A_USER_ID)
            assertThat(reactions?.first()?.senders?.first()?.user?.userId).isEqualTo(A_USER_ID)
            assertThat(reactions?.first()?.senders?.first()?.user?.avatarUrl).isEqualTo(AN_AVATAR_URL)
            assertThat(reactions?.first()?.senders?.first()?.user?.displayName).isEqualTo(A_USER_NAME)
        }
    }
}
