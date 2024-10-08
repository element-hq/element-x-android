/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.identity

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.encryption.identity.IdentityState
import io.element.android.libraries.matrix.api.encryption.identity.IdentityStateChange
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
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
            assertThat(value.identityRoomMember.userId).isEqualTo(A_USER_ID_2)
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
                            aRoomMember(
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
                assertThat(value.identityRoomMember.userId).isEqualTo(A_USER_ID_2)
                assertThat(value.identityRoomMember.disambiguatedDisplayName).isEqualTo("Alice")
                assertThat(value.identityRoomMember.avatarData.size).isEqualTo(AvatarSize.ComposerAlert)
                assertThat(value.identityState).isEqualTo(IdentityState.PinViolation)
            }
        }

    @Test
    fun `present - when the user pin the identity, the presenter invokes the encryption service api`() =
        runTest {
            val lambda = lambdaRecorder<UserId, Result<Unit>> { Result.success(Unit) }
            val encryptionService = FakeEncryptionService(
                pinUserIdentityResult = lambda,
            )
            val presenter = createIdentityChangeStatePresenter(encryptionService = encryptionService)
            presenter.test {
                val initialState = awaitItem()
                initialState.eventSink(IdentityChangeEvent.Submit(A_USER_ID))
                lambda.assertions().isCalledOnce().with(value(A_USER_ID))
            }
        }

    private fun createIdentityChangeStatePresenter(
        room: MatrixRoom = FakeMatrixRoom(),
        encryptionService: EncryptionService = FakeEncryptionService(),
    ): IdentityChangeStatePresenter {
        return IdentityChangeStatePresenter(
            room = room,
            encryptionService = encryptionService,
        )
    }
}
