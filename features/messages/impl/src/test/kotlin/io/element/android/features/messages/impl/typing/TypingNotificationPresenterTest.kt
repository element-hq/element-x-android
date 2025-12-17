/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.typing

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.Event
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.RoomMembersState
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID_3
import io.element.android.libraries.matrix.test.A_USER_ID_4
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
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
        val typingMembersFlow = MutableStateFlow<List<UserId>>(emptyList())
        val room = FakeJoinedRoom(roomTypingMembersFlow = typingMembersFlow)
        val sessionPreferencesStore = InMemorySessionPreferencesStore(
            isRenderTypingNotificationsEnabled = false,
        )
        val presenter = createPresenter(
            joinedRoom = room,
            sessionPreferencesStore = sessionPreferencesStore,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.renderTypingNotifications).isFalse()
            assertThat(initialState.typingMembers).isEmpty()
            typingMembersFlow.emit(listOf(A_USER_ID_2))
            expectNoEvents()
            // Preferences changes
            sessionPreferencesStore.setRenderTypingNotifications(true)
            skipItems(1)
            val oneMemberTypingState = awaitItem()
            assertThat(oneMemberTypingState.renderTypingNotifications).isTrue()
            assertThat(oneMemberTypingState.typingMembers.size).isEqualTo(1)
            assertThat(oneMemberTypingState.typingMembers.first()).isEqualTo(
                TypingRoomMember(
                    disambiguatedDisplayName = A_USER_ID_2.value,
                )
            )
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
        val typingMembersFlow = MutableStateFlow<List<UserId>>(emptyList())
        val room = FakeJoinedRoom(roomTypingMembersFlow = typingMembersFlow)
        val presenter = createPresenter(joinedRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.typingMembers).isEmpty()
            typingMembersFlow.emit(listOf(A_USER_ID_2))
            val oneMemberTypingState = awaitItem()
            assertThat(oneMemberTypingState.typingMembers.size).isEqualTo(1)
            assertThat(oneMemberTypingState.typingMembers.first()).isEqualTo(
                TypingRoomMember(
                    disambiguatedDisplayName = A_USER_ID_2.value,
                )
            )
            // User stops typing
            typingMembersFlow.emit(emptyList())
            skipItems(1)
            val finalState = awaitItem()
            assertThat(finalState.typingMembers).isEmpty()
        }
    }

    @Test
    fun `present - state is updated when a member is typing, member is known`() = runTest {
        val aKnownRoomMember = createKnownRoomMember(userId = A_USER_ID_2)
        val typingMembersFlow = MutableStateFlow<List<UserId>>(emptyList())
        val room = FakeJoinedRoom(roomTypingMembersFlow = typingMembersFlow).apply {
            givenRoomMembersState(
                RoomMembersState.Ready(
                    listOf(
                        createKnownRoomMember(A_USER_ID),
                        aKnownRoomMember,
                        createKnownRoomMember(A_USER_ID_3),
                        createKnownRoomMember(A_USER_ID_4),
                    ).toImmutableList()
                )
            )
        }
        val presenter = createPresenter(joinedRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.typingMembers).isEmpty()
            typingMembersFlow.emit(listOf(A_USER_ID_2))
            val oneMemberTypingState = awaitItem()
            assertThat(oneMemberTypingState.typingMembers.size).isEqualTo(1)
            assertThat(oneMemberTypingState.typingMembers.first()).isEqualTo(
                TypingRoomMember(
                    disambiguatedDisplayName = "Alice Doe (@bob:server.org)",
                )
            )
            // User stops typing
            typingMembersFlow.emit(emptyList())
            skipItems(1)
            val finalState = awaitItem()
            assertThat(finalState.typingMembers).isEmpty()
        }
    }

    @Test
    fun `present - state is updated when a member is typing, member is not known, then known`() = runTest {
        val aKnownRoomMember = createKnownRoomMember(A_USER_ID_2)
        val typingMembersFlow = MutableStateFlow<List<UserId>>(emptyList())
        val room = FakeJoinedRoom(roomTypingMembersFlow = typingMembersFlow)
        val presenter = createPresenter(joinedRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.typingMembers).isEmpty()
            typingMembersFlow.emit(listOf(A_USER_ID_2))
            val oneMemberTypingState = awaitItem()
            assertThat(oneMemberTypingState.typingMembers.size).isEqualTo(1)
            assertThat(oneMemberTypingState.typingMembers.first()).isEqualTo(
                TypingRoomMember(
                    disambiguatedDisplayName = A_USER_ID_2.value,
                )
            )
            // User is getting known
            room.givenRoomMembersState(
                RoomMembersState.Ready(
                    listOf(aKnownRoomMember).toImmutableList()
                )
            )
            skipItems(1)
            val finalState = awaitItem()
            assertThat(finalState.typingMembers.first()).isEqualTo(
                TypingRoomMember(
                    disambiguatedDisplayName = "Alice Doe (@bob:server.org)",
                )
            )
        }
    }

    @Test
    fun `present - reserveSpace becomes true once we get the first typing notification with room members`() = runTest {
        val typingMembersFlow = MutableStateFlow<List<UserId>>(emptyList())
        val room = FakeJoinedRoom(roomTypingMembersFlow = typingMembersFlow)
        val presenter = createPresenter(joinedRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.typingMembers).isEmpty()
            typingMembersFlow.emit(listOf(A_USER_ID_2))
            skipItems(1)
            val updatedTypingState = awaitItem()
            assertThat(updatedTypingState.reserveSpace).isTrue()
            // User stops typing
            typingMembersFlow.emit(emptyList())
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
        joinedRoom: JoinedRoom = FakeJoinedRoom().apply {
            givenRoomInfo(aRoomInfo(id = roomId, name = ""))
        },
        sessionPreferencesStore: SessionPreferencesStore = InMemorySessionPreferencesStore(
            isRenderTypingNotificationsEnabled = true
        ),
    ) = TypingNotificationPresenter(
        room = joinedRoom,
        sessionPreferencesStore = sessionPreferencesStore,
    )

    private fun createKnownRoomMember(
        userId: UserId,
    ) = aRoomMember(
        userId = userId,
        displayName = "Alice Doe",
        isNameAmbiguous = true,
    )
}
