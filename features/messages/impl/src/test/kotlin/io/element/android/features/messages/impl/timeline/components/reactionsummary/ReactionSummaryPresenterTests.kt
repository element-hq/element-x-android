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
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ReactionSummaryPresenterTests {

    @get:Rule
    val warmUpRule = WarmUpRule()

    private val aggregatedReaction = anAggregatedReaction(userId = A_USER_ID, key = "👍", isHighlighted = true)
    private val roomMember = aRoomMember(userId = A_USER_ID,  avatarUrl = AN_AVATAR_URL, displayName = A_USER_NAME)
    private val summaryEvent = ReactionSummaryEvents.ShowReactionSummary(AN_EVENT_ID, listOf(aggregatedReaction), aggregatedReaction.key)
    private val room = FakeMatrixRoom().apply {
        givenRoomMembersState(MatrixRoomMembersState.Ready(persistentListOf(roomMember)))
    }
    private val presenter = ReactionSummaryPresenter(room)

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
