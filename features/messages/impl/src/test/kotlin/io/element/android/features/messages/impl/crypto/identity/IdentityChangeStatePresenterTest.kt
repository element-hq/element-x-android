/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.identity

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.typing.aTypingRoomMember
import io.element.android.libraries.matrix.api.encryption.identity.IdentityState
import io.element.android.libraries.matrix.api.encryption.identity.IdentityStateChange
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class IdentityChangeStatePresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createIdentityChangeStatePresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.roomMemberIdentityStateChanges).isEmpty()
        }
    }

    @Test
    fun `present - when the room emits identity change, the presenter emits new state`() = runTest {
        val room = FakeMatrixRoom()
        val presenter = createIdentityChangeStatePresenter(room)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.roomMemberIdentityStateChanges).isEmpty()
            room.emitIdentityStateChanges(
                listOf(
                    IdentityStateChange(
                        userId = A_USER_ID_2,
                        identityState = IdentityState.PinViolation,
                    ),
                )
            )
            val finalItem = awaitItem()
            assertThat(finalItem.roomMemberIdentityStateChanges).hasSize(1)
            val value = finalItem.roomMemberIdentityStateChanges.first()
            assertThat(value.roomMember.userId).isEqualTo(A_USER_ID_2)
            assertThat(value.identityState).isEqualTo(IdentityState.PinViolation)
        }
    }

    @Test
    fun `present - when the room emits identity change, the presenter emits new state with member details`() =
        runTest {
            val room = FakeMatrixRoom().apply {
                givenRoomMembersState(
                    MatrixRoomMembersState.Ready(
                        listOf(
                            aTypingRoomMember(
                                A_USER_ID_2,
                                displayName = "Alice",
                            ),
                        ).toImmutableList()
                    )
                )
            }
            val presenter = createIdentityChangeStatePresenter(room)
            presenter.test {
                val initialState = awaitItem()
                assertThat(initialState.roomMemberIdentityStateChanges).isEmpty()
                room.emitIdentityStateChanges(
                    listOf(
                        IdentityStateChange(
                            userId = A_USER_ID_2,
                            identityState = IdentityState.PinViolation,
                        ),
                    )
                )
                val finalItem = awaitItem()
                assertThat(finalItem.roomMemberIdentityStateChanges).hasSize(1)
                val value = finalItem.roomMemberIdentityStateChanges.first()
                assertThat(value.roomMember.userId).isEqualTo(A_USER_ID_2)
                assertThat(value.roomMember.displayName).isEqualTo("Alice")
                assertThat(value.identityState).isEqualTo(IdentityState.PinViolation)
            }
        }

    private fun createIdentityChangeStatePresenter(
        room: MatrixRoom = FakeMatrixRoom(),
    ): IdentityChangeStatePresenter {
        return IdentityChangeStatePresenter(
            room = room,
        )
    }
}
