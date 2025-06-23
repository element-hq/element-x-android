/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.securityandprivacy

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.room.history.RoomHistoryVisibility
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.roomdirectory.RoomVisibility
import io.element.android.libraries.matrix.test.A_ROOM_ALIAS
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SecurityAndPrivacyPresenterTest {
    @Test
    fun `present - initial states`() = runTest {
        val presenter = createSecurityAndPrivacyPresenter()
        presenter.test {
            with(awaitItem()) {
                assertThat(editedSettings).isEqualTo(savedSettings)
                assertThat(canBeSaved).isFalse()
                assertThat(showEnableEncryptionConfirmation).isFalse()
                assertThat(saveAction).isEqualTo(AsyncAction.Uninitialized)
                assertThat(showRoomAccessSection).isFalse()
                assertThat(showRoomVisibilitySections).isFalse()
                assertThat(showHistoryVisibilitySection).isFalse()
                assertThat(showEncryptionSection).isFalse()
            }
            with(awaitItem()) {
                assertThat(editedSettings).isEqualTo(savedSettings)
                assertThat(canBeSaved).isFalse()
                assertThat(showEnableEncryptionConfirmation).isFalse()
                assertThat(saveAction).isEqualTo(AsyncAction.Uninitialized)
                assertThat(showRoomAccessSection).isTrue()
                assertThat(showRoomVisibilitySections).isFalse()
                assertThat(showHistoryVisibilitySection).isTrue()
                assertThat(showEncryptionSection).isTrue()
            }
        }
    }

    @Test
    fun `present - room info change updates saved and edited settings`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
            canSendStateResult = { _, _ -> Result.success(true) },
            initialRoomInfo = aRoomInfo(
                joinRule = JoinRule.Public,
                historyVisibility = RoomHistoryVisibility.WorldReadable,
                canonicalAlias = A_ROOM_ALIAS,
            )
        )
        )
        val presenter = createSecurityAndPrivacyPresenter(room = room)
        presenter.test {
            skipItems(1)
            with(awaitItem()) {
                assertThat(editedSettings).isEqualTo(savedSettings)
                assertThat(editedSettings.roomAccess).isEqualTo(SecurityAndPrivacyRoomAccess.Anyone)
                assertThat(editedSettings.historyVisibility).isEqualTo(SecurityAndPrivacyHistoryVisibility.Anyone)
                assertThat(editedSettings.address).isEqualTo(A_ROOM_ALIAS.value)
                assertThat(canBeSaved).isFalse()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - change room access`() = runTest {
        val presenter = createSecurityAndPrivacyPresenter()
        presenter.test {
            skipItems(1)
            with(awaitItem()) {
                assertThat(editedSettings.roomAccess).isEqualTo(SecurityAndPrivacyRoomAccess.InviteOnly)
                assertThat(showRoomVisibilitySections).isFalse()
                eventSink(SecurityAndPrivacyEvents.ChangeRoomAccess(SecurityAndPrivacyRoomAccess.Anyone))
            }
            with(awaitItem()) {
                assertThat(editedSettings.roomAccess).isEqualTo(SecurityAndPrivacyRoomAccess.Anyone)
                assertThat(showRoomVisibilitySections).isTrue()
                assertThat(canBeSaved).isTrue()
                eventSink(SecurityAndPrivacyEvents.ChangeRoomAccess(SecurityAndPrivacyRoomAccess.InviteOnly))
            }
            with(awaitItem()) {
                assertThat(editedSettings.roomAccess).isEqualTo(SecurityAndPrivacyRoomAccess.InviteOnly)
                assertThat(showRoomVisibilitySections).isFalse()
                assertThat(canBeSaved).isFalse()
            }
        }
    }

    @Test
    fun `present - change history visibility`() = runTest {
        val presenter = createSecurityAndPrivacyPresenter()
        presenter.test {
            skipItems(1)
            with(awaitItem()) {
                assertThat(editedSettings.historyVisibility).isEqualTo(SecurityAndPrivacyHistoryVisibility.SinceSelection)
                eventSink(SecurityAndPrivacyEvents.ChangeHistoryVisibility(SecurityAndPrivacyHistoryVisibility.SinceInvite))
            }
            with(awaitItem()) {
                assertThat(editedSettings.historyVisibility).isEqualTo(SecurityAndPrivacyHistoryVisibility.SinceInvite)
                assertThat(canBeSaved).isTrue()
                eventSink(SecurityAndPrivacyEvents.ChangeHistoryVisibility(SecurityAndPrivacyHistoryVisibility.SinceSelection))
            }
            with(awaitItem()) {
                assertThat(editedSettings.historyVisibility).isEqualTo(SecurityAndPrivacyHistoryVisibility.SinceSelection)
                assertThat(canBeSaved).isFalse()
            }
        }
    }

    @Test
    fun `present - enable encryption`() = runTest {
        val presenter = createSecurityAndPrivacyPresenter()
        presenter.test {
            skipItems(1)
            with(awaitItem()) {
                assertThat(editedSettings.isEncrypted).isFalse()
                eventSink(SecurityAndPrivacyEvents.ToggleEncryptionState)
            }
            with(awaitItem()) {
                assertThat(showEnableEncryptionConfirmation).isTrue()
                eventSink(SecurityAndPrivacyEvents.CancelEnableEncryption)
            }
            with(awaitItem()) {
                assertThat(showEnableEncryptionConfirmation).isFalse()
                eventSink(SecurityAndPrivacyEvents.ToggleEncryptionState)
            }
            with(awaitItem()) {
                assertThat(showEnableEncryptionConfirmation).isTrue()
                eventSink(SecurityAndPrivacyEvents.ConfirmEnableEncryption)
            }
            skipItems(1)
            with(awaitItem()) {
                assertThat(editedSettings.isEncrypted).isTrue()
                assertThat(showEnableEncryptionConfirmation).isFalse()
                assertThat(canBeSaved).isTrue()
                eventSink(SecurityAndPrivacyEvents.ToggleEncryptionState)
            }
            skipItems(1)
            with(awaitItem()) {
                assertThat(editedSettings.isEncrypted).isFalse()
                assertThat(canBeSaved).isFalse()
            }
        }
    }

    @Test
    fun `present - room visibility loading and change`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
            canSendStateResult = { _, _ -> Result.success(true) },
            getRoomVisibilityResult = { Result.success(RoomVisibility.Private) },
            initialRoomInfo = aRoomInfo(historyVisibility = RoomHistoryVisibility.Shared)
        )
        )
        val presenter = createSecurityAndPrivacyPresenter(room = room)
        presenter.test {
            skipItems(1)
            with(awaitItem()) {
                assertThat(editedSettings.isVisibleInRoomDirectory).isEqualTo(AsyncData.Loading<Boolean>())
            }
            with(awaitItem()) {
                assertThat(editedSettings.isVisibleInRoomDirectory).isEqualTo(AsyncData.Success(false))
                eventSink(SecurityAndPrivacyEvents.ToggleRoomVisibility)
            }
            with(awaitItem()) {
                assertThat(editedSettings.isVisibleInRoomDirectory).isEqualTo(AsyncData.Success(true))
                assertThat(canBeSaved).isTrue()
                eventSink(SecurityAndPrivacyEvents.ToggleRoomVisibility)
            }
            with(awaitItem()) {
                assertThat(editedSettings.isVisibleInRoomDirectory).isEqualTo(AsyncData.Success(false))
                assertThat(canBeSaved).isFalse()
            }
        }
    }

    @Test
    fun `present - edit room address`() = runTest {
        val openEditRoomAddressLambda = lambdaRecorder<Unit> { }
        val navigator = FakeSecurityAndPrivacyNavigator(openEditRoomAddressLambda)
        val presenter = createSecurityAndPrivacyPresenter(navigator = navigator)
        presenter.test {
            skipItems(1)
            with(awaitItem()) {
                eventSink(SecurityAndPrivacyEvents.EditRoomAddress)
            }
            assert(openEditRoomAddressLambda).isCalledOnce()
        }
    }

    @Test
    fun `present - save success`() = runTest {
        val enableEncryptionLambda = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val updateJoinRuleLambda = lambdaRecorder<JoinRule, Result<Unit>> { Result.success(Unit) }
        val updateRoomVisibilityLambda = lambdaRecorder<RoomVisibility, Result<Unit>> { Result.success(Unit) }
        val updateRoomHistoryVisibilityLambda = lambdaRecorder<RoomHistoryVisibility, Result<Unit>> { Result.success(Unit) }
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
            canSendStateResult = { _, _ -> Result.success(true) },
            getRoomVisibilityResult = { Result.success(RoomVisibility.Private) },
            initialRoomInfo = aRoomInfo(joinRule = JoinRule.Invite, historyVisibility = RoomHistoryVisibility.Shared)
        ),
            enableEncryptionResult = enableEncryptionLambda,
            updateJoinRuleResult = updateJoinRuleLambda,
            updateRoomVisibilityResult = updateRoomVisibilityLambda,
            updateRoomHistoryVisibilityResult = updateRoomHistoryVisibilityLambda,
        )
        val presenter = createSecurityAndPrivacyPresenter(room = room)
        presenter.test {
            skipItems(2)
            with(awaitItem()) {
                assertThat(editedSettings.roomAccess).isEqualTo(SecurityAndPrivacyRoomAccess.InviteOnly)
                eventSink(SecurityAndPrivacyEvents.ChangeRoomAccess(SecurityAndPrivacyRoomAccess.Anyone))
            }
            with(awaitItem()) {
                eventSink(SecurityAndPrivacyEvents.ChangeHistoryVisibility(SecurityAndPrivacyHistoryVisibility.Anyone))
            }
            with(awaitItem()) {
                assertThat(editedSettings.historyVisibility).isEqualTo(SecurityAndPrivacyHistoryVisibility.Anyone)
                eventSink(SecurityAndPrivacyEvents.ConfirmEnableEncryption)
            }
            skipItems(1)
            with(awaitItem()) {
                assertThat(editedSettings.isEncrypted).isTrue()
                eventSink(SecurityAndPrivacyEvents.ToggleRoomVisibility)
            }
            with(awaitItem()) {
                assertThat(editedSettings.isVisibleInRoomDirectory).isEqualTo(AsyncData.Success(true))
                eventSink(SecurityAndPrivacyEvents.Save)
            }
            with(awaitItem()) {
                assertThat(saveAction).isEqualTo(AsyncAction.Loading)
            }

            room.givenRoomInfo(
                aRoomInfo(
                    joinRule = JoinRule.Public,
                    historyVisibility = RoomHistoryVisibility.WorldReadable,
                    isEncrypted = true,
                )
            )
            // Saved settings are updated 3 times to match the edited settings
            skipItems(3)
            with(awaitItem()) {
                assertThat(saveAction).isEqualTo(AsyncAction.Success(Unit))
                assertThat(savedSettings).isEqualTo(editedSettings)
                assertThat(canBeSaved).isFalse()
            }
            assert(enableEncryptionLambda).isCalledOnce()
            assert(updateJoinRuleLambda).isCalledOnce()
            assert(updateRoomVisibilityLambda).isCalledOnce()
            assert(updateRoomHistoryVisibilityLambda).isCalledOnce()
        }
    }

    @Test
    fun `present - save failure`() = runTest {
        val enableEncryptionLambda = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val updateJoinRuleLambda = lambdaRecorder<JoinRule, Result<Unit>> { Result.success(Unit) }
        val updateRoomVisibilityLambda = lambdaRecorder<RoomVisibility, Result<Unit>> {
            Result.failure(Exception("Failed to update room visibility"))
        }
        val updateRoomHistoryVisibilityLambda = lambdaRecorder<RoomHistoryVisibility, Result<Unit>> { Result.success(Unit) }
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
            canSendStateResult = { _, _ -> Result.success(true) },
            getRoomVisibilityResult = { Result.success(RoomVisibility.Private) },
            initialRoomInfo = aRoomInfo(historyVisibility = RoomHistoryVisibility.Shared, joinRule = JoinRule.Private)
        ),
            enableEncryptionResult = enableEncryptionLambda,
            updateJoinRuleResult = updateJoinRuleLambda,
            updateRoomVisibilityResult = updateRoomVisibilityLambda,
            updateRoomHistoryVisibilityResult = updateRoomHistoryVisibilityLambda,
        )
        val presenter = createSecurityAndPrivacyPresenter(room = room)
        presenter.test {
            skipItems(2)
            with(awaitItem()) {
                assertThat(editedSettings.roomAccess).isEqualTo(SecurityAndPrivacyRoomAccess.InviteOnly)
                eventSink(SecurityAndPrivacyEvents.ChangeRoomAccess(SecurityAndPrivacyRoomAccess.Anyone))
            }
            with(awaitItem()) {
                eventSink(SecurityAndPrivacyEvents.ChangeHistoryVisibility(SecurityAndPrivacyHistoryVisibility.Anyone))
            }
            with(awaitItem()) {
                assertThat(editedSettings.historyVisibility).isEqualTo(SecurityAndPrivacyHistoryVisibility.Anyone)
                eventSink(SecurityAndPrivacyEvents.ConfirmEnableEncryption)
            }
            skipItems(1)
            with(awaitItem()) {
                assertThat(editedSettings.isEncrypted).isTrue()
                eventSink(SecurityAndPrivacyEvents.ToggleRoomVisibility)
            }
            with(awaitItem()) {
                assertThat(editedSettings.isVisibleInRoomDirectory).isEqualTo(AsyncData.Success(true))
                eventSink(SecurityAndPrivacyEvents.Save)
            }
            with(awaitItem()) {
                assertThat(saveAction).isEqualTo(AsyncAction.Loading)
            }

            room.givenRoomInfo(
                aRoomInfo(
                    joinRule = JoinRule.Public,
                    historyVisibility = RoomHistoryVisibility.WorldReadable,
                )
            )
            // Saved settings are updated 2 times to match the edited settings
            skipItems(3)
            with(awaitItem()) {
                assertThat(saveAction).isInstanceOf(AsyncAction.Failure::class.java)
                assertThat(savedSettings.isVisibleInRoomDirectory).isNotEqualTo(editedSettings.isVisibleInRoomDirectory)
                assertThat(canBeSaved).isTrue()
            }
            assert(enableEncryptionLambda).isCalledOnce()
            assert(updateJoinRuleLambda).isCalledOnce()
            assert(updateRoomVisibilityLambda).isCalledOnce()
            assert(updateRoomHistoryVisibilityLambda).isCalledOnce()
        }
    }

    private fun createSecurityAndPrivacyPresenter(
        serverName: String = "matrix.org",
        room: FakeJoinedRoom = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canSendStateResult = { _, _ -> Result.success(true) },
                getRoomVisibilityResult = { Result.success(RoomVisibility.Private) },
                initialRoomInfo = aRoomInfo(historyVisibility = RoomHistoryVisibility.Shared, joinRule = JoinRule.Private)
            ),
        ),
        navigator: SecurityAndPrivacyNavigator = FakeSecurityAndPrivacyNavigator(),
    ): SecurityAndPrivacyPresenter {
        return SecurityAndPrivacyPresenter(
            room = room,
            matrixClient = FakeMatrixClient(
                userIdServerNameLambda = { serverName },
            ),
            navigator = navigator
        )
    }
}
