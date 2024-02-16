/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.messages.impl.typing

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.Event
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.preferences.api.store.SessionPreferencesStore
import io.element.android.libraries.featureflag.test.InMemorySessionPreferencesStore
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID_3
import io.element.android.libraries.matrix.test.A_USER_ID_4
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@Suppress("LargeClass")
class TypingNotificationPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.renderTypingNotifications).isTrue()
            assertThat(initialState.typingMembers).isEmpty()
            assertThat(initialState.reserveSpace).isFalse()
        }
    }

    @Test
    fun `present - typing notification disabled`() = runTest {
        val aDefaultRoomMember = createDefaultRoomMember(A_USER_ID_2)
        val room = FakeMatrixRoom()
        val sessionPreferencesStore = InMemorySessionPreferencesStore(
            isRenderTypingNotificationsEnabled = false
        )
        val presenter = createPresenter(
            matrixRoom = room,
            sessionPreferencesStore = sessionPreferencesStore,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.renderTypingNotifications).isFalse()
            assertThat(initialState.typingMembers).isEmpty()
            room.givenRoomTypingMembers(listOf(A_USER_ID_2))
            expectNoEvents()
            // Preferences changes
            sessionPreferencesStore.setRenderTypingNotifications(true)
            skipItems(1)
            val oneMemberTypingState = awaitItem()
            assertThat(oneMemberTypingState.renderTypingNotifications).isTrue()
            assertThat(oneMemberTypingState.typingMembers.size).isEqualTo(1)
            assertThat(oneMemberTypingState.typingMembers.first()).isEqualTo(aDefaultRoomMember)
            // Preferences changes again
            sessionPreferencesStore.setRenderTypingNotifications(false)
            skipItems(2)
            val finalState = awaitItem()
            assertThat(finalState.renderTypingNotifications).isFalse()
            assertThat(finalState.typingMembers).isEmpty()
        }
    }

    @Test
    fun `present - state is updated when a member is typing, member is not known`() = runTest {
        val aDefaultRoomMember = createDefaultRoomMember(A_USER_ID_2)
        val room = FakeMatrixRoom()
        val presenter = createPresenter(matrixRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.typingMembers).isEmpty()
            room.givenRoomTypingMembers(listOf(A_USER_ID_2))
            val oneMemberTypingState = awaitItem()
            assertThat(oneMemberTypingState.typingMembers.size).isEqualTo(1)
            assertThat(oneMemberTypingState.typingMembers.first()).isEqualTo(aDefaultRoomMember)
            // User stops typing
            room.givenRoomTypingMembers(emptyList())
            skipItems(1)
            val finalState = awaitItem()
            assertThat(finalState.typingMembers).isEmpty()
        }
    }

    @Test
    fun `present - state is updated when a member is typing, member is known`() = runTest {
        val aKnownRoomMember = createKnownRoomMember(userId = A_USER_ID_2)
        val room = FakeMatrixRoom().apply {
            givenRoomMembersState(
                MatrixRoomMembersState.Ready(
                    listOf(
                        createKnownRoomMember(A_USER_ID),
                        aKnownRoomMember,
                        createKnownRoomMember(A_USER_ID_3),
                        createKnownRoomMember(A_USER_ID_4),
                    ).toImmutableList()
                )
            )
        }
        val presenter = createPresenter(matrixRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.typingMembers).isEmpty()
            room.givenRoomTypingMembers(listOf(A_USER_ID_2))
            val oneMemberTypingState = awaitItem()
            assertThat(oneMemberTypingState.typingMembers.size).isEqualTo(1)
            assertThat(oneMemberTypingState.typingMembers.first()).isEqualTo(aKnownRoomMember)
            // User stops typing
            room.givenRoomTypingMembers(emptyList())
            skipItems(1)
            val finalState = awaitItem()
            assertThat(finalState.typingMembers).isEmpty()
        }
    }

    @Test
    fun `present - state is updated when a member is typing, member is not known, then known`() = runTest {
        val aDefaultRoomMember = createDefaultRoomMember(A_USER_ID_2)
        val aKnownRoomMember = createKnownRoomMember(A_USER_ID_2)
        val room = FakeMatrixRoom()
        val presenter = createPresenter(matrixRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.typingMembers).isEmpty()
            room.givenRoomTypingMembers(listOf(A_USER_ID_2))
            val oneMemberTypingState = awaitItem()
            assertThat(oneMemberTypingState.typingMembers.size).isEqualTo(1)
            assertThat(oneMemberTypingState.typingMembers.first()).isEqualTo(aDefaultRoomMember)
            // User is getting known
            room.givenRoomMembersState(
                MatrixRoomMembersState.Ready(
                    listOf(aKnownRoomMember).toImmutableList()
                )
            )
            skipItems(1)
            val finalState = awaitItem()
            assertThat(finalState.typingMembers.first()).isEqualTo(aKnownRoomMember)
        }
    }

    @Test
    fun `present - reserveSpace becomes true once we get the first typing notification with room members`() = runTest {
        val aDefaultRoomMember = createDefaultRoomMember(A_USER_ID_2)
        val room = FakeMatrixRoom()
        val presenter = createPresenter(matrixRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.typingMembers).isEmpty()
            room.givenRoomTypingMembers(listOf(A_USER_ID_2))
            skipItems(1)
            val updatedTypingState = awaitItem()
            assertThat(updatedTypingState.reserveSpace).isTrue()
            // User stops typing
            room.givenRoomTypingMembers(emptyList())
            // Is still true for all future events
            val futureEvents = cancelAndConsumeRemainingEvents()
            for (event in futureEvents) {
                if (event is Event.Item) {
                    assertThat(event.value.reserveSpace).isTrue()
                }
            }
        }
    }

    private fun createPresenter(
        matrixRoom: MatrixRoom = FakeMatrixRoom().apply {
            givenRoomInfo(aRoomInfo(id = roomId.value, name = ""))
        },
        sessionPreferencesStore: SessionPreferencesStore = InMemorySessionPreferencesStore(
            isRenderTypingNotificationsEnabled = true
        ),
    ) = TypingNotificationPresenter(
        room = matrixRoom,
        sessionPreferencesStore = sessionPreferencesStore,
    )

    private fun createDefaultRoomMember(
        userId: UserId,
    ) = RoomMember(
        userId = userId,
        displayName = null,
        avatarUrl = null,
        membership = RoomMembershipState.JOIN,
        isNameAmbiguous = false,
        powerLevel = 0,
        normalizedPowerLevel = 0,
        isIgnored = false,
    )

    private fun createKnownRoomMember(
        userId: UserId,
    ) = RoomMember(
        userId = userId,
        displayName = "Alice Doe",
        avatarUrl = "an_avatar_url",
        membership = RoomMembershipState.JOIN,
        isNameAmbiguous = true,
        powerLevel = 0,
        normalizedPowerLevel = 0,
        isIgnored = false,
    )
}
