/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.root

import com.google.common.truth.Truth.assertThat
import io.element.android.features.securityandprivacy.impl.FakeSecurityAndPrivacyNavigator
import io.element.android.features.securityandprivacy.impl.SecurityAndPrivacyNavigator
import io.element.android.features.securityandprivacy.impl.manageauthorizedspaces.SpaceSelectionStateHolder
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.api.room.history.RoomHistoryVisibility
import io.element.android.libraries.matrix.api.room.join.AllowRule
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPermissions
import io.element.android.libraries.matrix.api.roomdirectory.RoomVisibility
import io.element.android.libraries.matrix.test.A_ROOM_ALIAS
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.matrix.test.room.powerlevels.FakeRoomPermissions
import io.element.android.libraries.matrix.test.spaces.FakeSpaceService
import io.element.android.libraries.previewutils.room.aSpaceRoom
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

@Suppress("LargeClass")
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
                roomPermissions = roomPermissions(),
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
                assertThat(editedSettings.historyVisibility).isEqualTo(SecurityAndPrivacyHistoryVisibility.WorldReadable)
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
                eventSink(SecurityAndPrivacyEvent.ChangeRoomAccess(SecurityAndPrivacyRoomAccess.Anyone))
            }
            with(awaitItem()) {
                assertThat(editedSettings.roomAccess).isEqualTo(SecurityAndPrivacyRoomAccess.Anyone)
                assertThat(showRoomVisibilitySections).isTrue()
                assertThat(canBeSaved).isTrue()
                eventSink(SecurityAndPrivacyEvent.ChangeRoomAccess(SecurityAndPrivacyRoomAccess.InviteOnly))
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
                assertThat(editedSettings.historyVisibility).isEqualTo(SecurityAndPrivacyHistoryVisibility.Shared)
                eventSink(SecurityAndPrivacyEvent.ChangeHistoryVisibility(SecurityAndPrivacyHistoryVisibility.Invited))
            }
            with(awaitItem()) {
                assertThat(editedSettings.historyVisibility).isEqualTo(SecurityAndPrivacyHistoryVisibility.Invited)
                assertThat(canBeSaved).isTrue()
                eventSink(SecurityAndPrivacyEvent.ChangeHistoryVisibility(SecurityAndPrivacyHistoryVisibility.Shared))
            }
            with(awaitItem()) {
                assertThat(editedSettings.historyVisibility).isEqualTo(SecurityAndPrivacyHistoryVisibility.Shared)
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
                eventSink(SecurityAndPrivacyEvent.ToggleEncryptionState)
            }
            with(awaitItem()) {
                assertThat(showEnableEncryptionConfirmation).isTrue()
                eventSink(SecurityAndPrivacyEvent.CancelEnableEncryption)
            }
            with(awaitItem()) {
                assertThat(showEnableEncryptionConfirmation).isFalse()
                eventSink(SecurityAndPrivacyEvent.ToggleEncryptionState)
            }
            with(awaitItem()) {
                assertThat(showEnableEncryptionConfirmation).isTrue()
                eventSink(SecurityAndPrivacyEvent.ConfirmEnableEncryption)
            }
            skipItems(1)
            with(awaitItem()) {
                assertThat(editedSettings.isEncrypted).isTrue()
                assertThat(showEnableEncryptionConfirmation).isFalse()
                assertThat(canBeSaved).isTrue()
                eventSink(SecurityAndPrivacyEvent.ToggleEncryptionState)
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
                roomPermissions = roomPermissions(),
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
                eventSink(SecurityAndPrivacyEvent.ToggleRoomVisibility)
            }
            with(awaitItem()) {
                assertThat(editedSettings.isVisibleInRoomDirectory).isEqualTo(AsyncData.Success(true))
                assertThat(canBeSaved).isTrue()
                eventSink(SecurityAndPrivacyEvent.ToggleRoomVisibility)
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
        val navigator =
            FakeSecurityAndPrivacyNavigator(openEditRoomAddressLambda = openEditRoomAddressLambda)
        val presenter = createSecurityAndPrivacyPresenter(navigator = navigator)
        presenter.test {
            skipItems(1)
            with(awaitItem()) {
                eventSink(SecurityAndPrivacyEvent.EditRoomAddress)
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
                roomPermissions = roomPermissions(),
                getRoomVisibilityResult = { Result.success(RoomVisibility.Private) },
                initialRoomInfo = aRoomInfo(joinRule = JoinRule.Invite, historyVisibility = RoomHistoryVisibility.Shared)
            ),
            enableEncryptionResult = enableEncryptionLambda,
            updateJoinRuleResult = updateJoinRuleLambda,
            updateRoomVisibilityResult = updateRoomVisibilityLambda,
            updateRoomHistoryVisibilityResult = updateRoomHistoryVisibilityLambda,
        )
        val onDoneLambda = lambdaRecorder<Unit> { }
        val navigator = FakeSecurityAndPrivacyNavigator(
            onDoneLambda = onDoneLambda,
        )
        val presenter = createSecurityAndPrivacyPresenter(
            room = room,
            navigator = navigator,
        )
        presenter.test {
            skipItems(1)
            with(awaitItem()) {
                assertThat(editedSettings.roomAccess).isEqualTo(SecurityAndPrivacyRoomAccess.InviteOnly)
                eventSink(SecurityAndPrivacyEvent.ChangeRoomAccess(SecurityAndPrivacyRoomAccess.Anyone))
            }
            with(awaitItem()) {
                eventSink(SecurityAndPrivacyEvent.ChangeHistoryVisibility(SecurityAndPrivacyHistoryVisibility.WorldReadable))
            }
            with(awaitItem()) {
                assertThat(editedSettings.historyVisibility).isEqualTo(SecurityAndPrivacyHistoryVisibility.WorldReadable)
                eventSink(SecurityAndPrivacyEvent.ConfirmEnableEncryption)
            }
            skipItems(1)
            with(awaitItem()) {
                assertThat(editedSettings.isEncrypted).isTrue()
                eventSink(SecurityAndPrivacyEvent.ToggleRoomVisibility)
            }
            with(awaitItem()) {
                assertThat(editedSettings.isVisibleInRoomDirectory).isEqualTo(AsyncData.Success(true))
                eventSink(SecurityAndPrivacyEvent.Save)
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
            // Saved settings are updated 2 times to match the edited settings
            skipItems(2)
            with(awaitItem()) {
                assertThat(saveAction).isEqualTo(AsyncAction.Success(Unit))
                assertThat(savedSettings).isEqualTo(editedSettings)
                assertThat(canBeSaved).isFalse()
            }
            assert(enableEncryptionLambda).isCalledOnce()
            assert(updateJoinRuleLambda).isCalledOnce()
            assert(updateRoomVisibilityLambda).isCalledOnce()
            assert(updateRoomHistoryVisibilityLambda).isCalledOnce()
            onDoneLambda.assertions().isCalledOnce()
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
                roomPermissions = roomPermissions(),
                getRoomVisibilityResult = { Result.success(RoomVisibility.Private) },
                initialRoomInfo = aRoomInfo(historyVisibility = RoomHistoryVisibility.Shared, joinRule = JoinRule.Invite)
            ),
            enableEncryptionResult = enableEncryptionLambda,
            updateJoinRuleResult = updateJoinRuleLambda,
            updateRoomVisibilityResult = updateRoomVisibilityLambda,
            updateRoomHistoryVisibilityResult = updateRoomHistoryVisibilityLambda,
        )
        val presenter = createSecurityAndPrivacyPresenter(room = room)
        presenter.test {
            skipItems(1)
            with(awaitItem()) {
                assertThat(editedSettings.roomAccess).isEqualTo(SecurityAndPrivacyRoomAccess.InviteOnly)
                eventSink(SecurityAndPrivacyEvent.ChangeRoomAccess(SecurityAndPrivacyRoomAccess.Anyone))
            }
            with(awaitItem()) {
                eventSink(SecurityAndPrivacyEvent.ChangeHistoryVisibility(SecurityAndPrivacyHistoryVisibility.WorldReadable))
            }
            with(awaitItem()) {
                assertThat(editedSettings.historyVisibility).isEqualTo(SecurityAndPrivacyHistoryVisibility.WorldReadable)
                eventSink(SecurityAndPrivacyEvent.ConfirmEnableEncryption)
            }
            skipItems(1)
            with(awaitItem()) {
                assertThat(editedSettings.isEncrypted).isTrue()
                eventSink(SecurityAndPrivacyEvent.ToggleRoomVisibility)
            }
            with(awaitItem()) {
                assertThat(editedSettings.isVisibleInRoomDirectory).isEqualTo(AsyncData.Success(true))
                eventSink(SecurityAndPrivacyEvent.Save)
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
            skipItems(2)
            val state = awaitItem()
            with(state) {
                assertThat(saveAction).isInstanceOf(AsyncAction.Failure::class.java)
                assertThat(savedSettings.isVisibleInRoomDirectory).isNotEqualTo(editedSettings.isVisibleInRoomDirectory)
                assertThat(canBeSaved).isTrue()
            }
            assert(enableEncryptionLambda).isCalledOnce()
            assert(updateJoinRuleLambda).isCalledOnce()
            assert(updateRoomVisibilityLambda).isCalledOnce()
            assert(updateRoomHistoryVisibilityLambda).isCalledOnce()
            // Clear error
            state.eventSink(SecurityAndPrivacyEvent.DismissSaveError)
            with(awaitItem()) {
                assertThat(saveAction).isEqualTo(AsyncAction.Uninitialized)
            }
        }
    }

    @Test
    fun `present - Restricted join rule maps to SpaceMember`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                roomPermissions = roomPermissions(),
                initialRoomInfo = aRoomInfo(
                    joinRule = JoinRule.Restricted(
                        rules = persistentListOf(AllowRule.RoomMembership(A_ROOM_ID))
                    ),
                    historyVisibility = RoomHistoryVisibility.Shared,
                )
            )
        )
        val presenter = createSecurityAndPrivacyPresenter(room = room)
        presenter.test {
            skipItems(1)
            with(awaitItem()) {
                assertThat(editedSettings.roomAccess).isInstanceOf(SecurityAndPrivacyRoomAccess.SpaceMember::class.java)
                val access = editedSettings.roomAccess as SecurityAndPrivacyRoomAccess.SpaceMember
                assertThat(access.spaceIds).containsExactly(A_ROOM_ID)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - SelectSpaceMemberAccess with single space auto-selects`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                roomPermissions = roomPermissions(),
                initialRoomInfo = aRoomInfo(
                    historyVisibility = RoomHistoryVisibility.Shared,
                    joinRule = JoinRule.Invite
                )
            )
        )
        val client = FakeMatrixClient(
            userIdServerNameLambda = { "matrix.org" },
            spaceService = FakeSpaceService(
                joinedParentsResult = { _ ->
                    Result.success(listOf(aSpaceRoom(roomId = A_ROOM_ID)))
                }
            )
        )
        val presenter = createSecurityAndPrivacyPresenter(
            room = room,
            matrixClient = client,
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(
                    FeatureFlags.SpaceSettings.key to true,
                )
            )
        )
        presenter.test {
            skipItems(1)
            val state = awaitItem()
            assertThat(state.isSpaceMemberSelectable).isTrue()
            state.eventSink(SecurityAndPrivacyEvent.SelectSpaceMemberAccess)
            with(awaitItem()) {
                assertThat(editedSettings.roomAccess).isInstanceOf(SecurityAndPrivacyRoomAccess.SpaceMember::class.java)
                val access = editedSettings.roomAccess as SecurityAndPrivacyRoomAccess.SpaceMember
                assertThat(access.spaceIds).containsExactly(A_ROOM_ID)
            }
        }
    }

    @Test
    fun `present - SelectSpaceMemberAccess with multiple spaces opens ManageAuthorizedSpaces`() = runTest {
        val openManageAuthorizedSpacesLambda = lambdaRecorder<Unit> { }
        val navigator =
            FakeSecurityAndPrivacyNavigator(openManageAuthorizedSpacesLambda = openManageAuthorizedSpacesLambda)
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                roomPermissions = roomPermissions(),
                initialRoomInfo = aRoomInfo(
                    historyVisibility = RoomHistoryVisibility.Shared,
                    joinRule = JoinRule.Invite
                )
            )
        )
        val client = FakeMatrixClient(
            userIdServerNameLambda = { "matrix.org" },
            spaceService = FakeSpaceService(
                joinedParentsResult = { _ ->
                    Result.success(listOf(aSpaceRoom(roomId = A_ROOM_ID), aSpaceRoom(roomId = RoomId("!space2:matrix.org"))))
                }
            )
        )
        val presenter = createSecurityAndPrivacyPresenter(
            room = room,
            navigator = navigator,
            matrixClient = client,
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(
                    FeatureFlags.SpaceSettings.key to true,
                )
            )
        )
        presenter.test {
            skipItems(1)
            val state = awaitItem()
            assertThat(state.isSpaceMemberSelectable).isTrue()
            state.eventSink(SecurityAndPrivacyEvent.SelectSpaceMemberAccess)
            assert(openManageAuthorizedSpacesLambda).isCalledOnce()
        }
    }

    @Test
    fun `present - SpaceMember saves as Restricted join rule`() = runTest {
        val updateJoinRuleLambda = lambdaRecorder<JoinRule, Result<Unit>> { Result.success(Unit) }
        val updateRoomVisibilityLambda = lambdaRecorder<RoomVisibility, Result<Unit>> { Result.success(Unit) }
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                roomPermissions = roomPermissions(),
                getRoomVisibilityResult = { Result.success(RoomVisibility.Private) },
                initialRoomInfo = aRoomInfo(
                    historyVisibility = RoomHistoryVisibility.Shared,
                    joinRule = JoinRule.Invite
                )
            ),
            updateJoinRuleResult = updateJoinRuleLambda,
            updateRoomVisibilityResult = updateRoomVisibilityLambda,
        )
        val onDoneLambda = lambdaRecorder<Unit> { }
        val navigator = FakeSecurityAndPrivacyNavigator(onDoneLambda = onDoneLambda)
        val presenter = createSecurityAndPrivacyPresenter(room = room, navigator = navigator)
        presenter.test {
            skipItems(1)
            with(awaitItem()) {
                assertThat(editedSettings.roomAccess).isEqualTo(SecurityAndPrivacyRoomAccess.InviteOnly)
                val spaceMemberAccess = SecurityAndPrivacyRoomAccess.SpaceMember(
                    spaceIds = persistentListOf(A_ROOM_ID)
                )
                eventSink(SecurityAndPrivacyEvent.ChangeRoomAccess(spaceMemberAccess))
            }
            with(awaitItem()) {
                assertThat(editedSettings.roomAccess).isInstanceOf(SecurityAndPrivacyRoomAccess.SpaceMember::class.java)
                assertThat(canBeSaved).isTrue()
                eventSink(SecurityAndPrivacyEvent.Save)
            }
            with(awaitItem()) {
                assertThat(saveAction).isEqualTo(AsyncAction.Loading)
            }
            room.givenRoomInfo(
                aRoomInfo(
                    joinRule = JoinRule.Restricted(
                        rules = persistentListOf(AllowRule.RoomMembership(A_ROOM_ID))
                    ),
                    historyVisibility = RoomHistoryVisibility.Shared,
                )
            )
            skipItems(2)
            with(awaitItem()) {
                assertThat(saveAction).isEqualTo(AsyncAction.Success(Unit))
            }
            assert(updateJoinRuleLambda).isCalledOnce().with(
                value(JoinRule.Restricted(rules = persistentListOf(AllowRule.RoomMembership(A_ROOM_ID))))
            )
            onDoneLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - room visibility is NOT configurable for SpaceMember`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                roomPermissions = roomPermissions(),
                getRoomVisibilityResult = { Result.success(RoomVisibility.Private) },
                initialRoomInfo = aRoomInfo(
                    historyVisibility = RoomHistoryVisibility.Shared,
                    joinRule = JoinRule.Restricted(
                        rules = persistentListOf(AllowRule.RoomMembership(A_ROOM_ID))
                    )
                )
            )
        )
        val presenter = createSecurityAndPrivacyPresenter(room = room)
        presenter.test {
            skipItems(1)
            with(awaitItem()) {
                assertThat(editedSettings.roomAccess).isInstanceOf(SecurityAndPrivacyRoomAccess.SpaceMember::class.java)
                assertThat(showRoomVisibilitySections).isFalse()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - KnockRestricted join rule maps to AskToJoinWithSpaceMembers`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                roomPermissions = roomPermissions(),
                initialRoomInfo = aRoomInfo(
                    joinRule = JoinRule.KnockRestricted(
                        rules = persistentListOf(AllowRule.RoomMembership(A_ROOM_ID))
                    ),
                    historyVisibility = RoomHistoryVisibility.Shared,
                )
            )
        )
        val presenter = createSecurityAndPrivacyPresenter(room = room)
        presenter.test {
            skipItems(1)
            with(awaitItem()) {
                assertThat(editedSettings.roomAccess).isInstanceOf(SecurityAndPrivacyRoomAccess.AskToJoinWithSpaceMember::class.java)
                val access = editedSettings.roomAccess as SecurityAndPrivacyRoomAccess.AskToJoinWithSpaceMember
                assertThat(access.spaceIds).containsExactly(A_ROOM_ID)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - showAskToJoinWithSpaceMembersOption is true when both FFs enabled and spaces available`() = runTest {
        val presenter = createSecurityAndPrivacyPresenter(
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(
                    FeatureFlags.Knock.key to true,
                    FeatureFlags.SpaceSettings.key to true,
                )
            )
        )
        presenter.test {
            skipItems(1)
            // Without spaces available, AskToJoinWithSpaceMembers should not be selectable
            with(awaitItem()) {
                assertThat(isAskToJoinWithSpaceMembersSelectable).isFalse()
                assertThat(showAskToJoinWithSpaceMemberOption).isFalse()
                // AskToJoin should be shown instead
                assertThat(showAskToJoinOption).isTrue()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - SelectAskToJoinWithSpaceMembersAccess with multiple spaces opens ManageAuthorizedSpaces`() = runTest {
        val openManageAuthorizedSpacesLambda = lambdaRecorder<Unit> { }
        val navigator =
            FakeSecurityAndPrivacyNavigator(openManageAuthorizedSpacesLambda = openManageAuthorizedSpacesLambda)
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                roomPermissions = roomPermissions(),
                initialRoomInfo = aRoomInfo(
                    historyVisibility = RoomHistoryVisibility.Shared,
                    joinRule = JoinRule.Invite
                )
            )
        )
        val client = FakeMatrixClient(
            userIdServerNameLambda = { "matrix.org" },
            spaceService = FakeSpaceService(
                joinedParentsResult = { _ ->
                    Result.success(listOf(aSpaceRoom(roomId = A_ROOM_ID), aSpaceRoom(roomId = RoomId("!space2:matrix.org"))))
                }
            )
        )
        val presenter = createSecurityAndPrivacyPresenter(
            room = room,
            navigator = navigator,
            matrixClient = client,
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(
                    FeatureFlags.Knock.key to true,
                    FeatureFlags.SpaceSettings.key to true,
                )
            )
        )
        presenter.test {
            skipItems(1)
            // Wait for space selection mode to be set
            val state = awaitItem()
            assertThat(state.isAskToJoinWithSpaceMembersSelectable).isTrue()
            state.eventSink(SecurityAndPrivacyEvent.SelectAskToJoinWithSpaceMembersAccess)
            assert(openManageAuthorizedSpacesLambda).isCalledOnce()
        }
    }

    @Test
    fun `present - AskToJoinWithSpaceMember saves as KnockRestricted join rule`() = runTest {
        val updateJoinRuleLambda = lambdaRecorder<JoinRule, Result<Unit>> { Result.success(Unit) }
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                roomPermissions = roomPermissions(),
                getRoomVisibilityResult = { Result.success(RoomVisibility.Private) },
                initialRoomInfo = aRoomInfo(
                    historyVisibility = RoomHistoryVisibility.Shared,
                    joinRule = JoinRule.Invite
                )
            ),
            updateJoinRuleResult = updateJoinRuleLambda,
        )
        val onDoneLambda = lambdaRecorder<Unit> { }
        val navigator = FakeSecurityAndPrivacyNavigator(onDoneLambda = onDoneLambda)
        val presenter = createSecurityAndPrivacyPresenter(room = room, navigator = navigator)
        presenter.test {
            skipItems(1)
            with(awaitItem()) {
                assertThat(editedSettings.roomAccess).isEqualTo(SecurityAndPrivacyRoomAccess.InviteOnly)
                val askToJoinAccess = SecurityAndPrivacyRoomAccess.AskToJoinWithSpaceMember(
                    spaceIds = persistentListOf(A_ROOM_ID)
                )
                eventSink(SecurityAndPrivacyEvent.ChangeRoomAccess(askToJoinAccess))
            }
            with(awaitItem()) {
                assertThat(editedSettings.roomAccess).isInstanceOf(SecurityAndPrivacyRoomAccess.AskToJoinWithSpaceMember::class.java)
                assertThat(canBeSaved).isTrue()
                eventSink(SecurityAndPrivacyEvent.Save)
            }
            with(awaitItem()) {
                assertThat(saveAction).isEqualTo(AsyncAction.Loading)
            }
            room.givenRoomInfo(
                aRoomInfo(
                    joinRule = JoinRule.KnockRestricted(
                        rules = persistentListOf(AllowRule.RoomMembership(A_ROOM_ID))
                    ),
                    historyVisibility = RoomHistoryVisibility.Shared,
                )
            )
            // Saved settings are updated multiple times to match the edited settings
            skipItems(2)
            with(awaitItem()) {
                assertThat(saveAction).isEqualTo(AsyncAction.Success(Unit))
            }
            assert(updateJoinRuleLambda).isCalledOnce().with(
                value(JoinRule.KnockRestricted(rules = persistentListOf(AllowRule.RoomMembership(A_ROOM_ID))))
            )
            onDoneLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - room visibility is configurable for AskToJoinWithSpaceMember`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                roomPermissions = roomPermissions(),
                getRoomVisibilityResult = { Result.success(RoomVisibility.Private) },
                initialRoomInfo = aRoomInfo(
                    historyVisibility = RoomHistoryVisibility.Shared,
                    joinRule = JoinRule.KnockRestricted(
                        rules = persistentListOf(AllowRule.RoomMembership(A_ROOM_ID))
                    )
                )
            )
        )
        val presenter = createSecurityAndPrivacyPresenter(room = room)
        presenter.test {
            skipItems(1)
            with(awaitItem()) {
                assertThat(editedSettings.roomAccess).isInstanceOf(SecurityAndPrivacyRoomAccess.AskToJoinWithSpaceMember::class.java)
                assertThat(showRoomVisibilitySections).isTrue()
            }
            with(awaitItem()) {
                assertThat(editedSettings.isVisibleInRoomDirectory).isEqualTo(AsyncData.Success(false))
                eventSink(SecurityAndPrivacyEvent.ToggleRoomVisibility)
            }
            with(awaitItem()) {
                assertThat(editedSettings.isVisibleInRoomDirectory).isEqualTo(AsyncData.Success(true))
                assertThat(canBeSaved).isTrue()
            }
        }
    }

    @Test
    fun `present - availableHistoryVisibilities includes WorldReadable for Anyone without encryption`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                roomPermissions = roomPermissions(),
                initialRoomInfo = aRoomInfo(
                    joinRule = JoinRule.Public,
                    historyVisibility = RoomHistoryVisibility.Shared,
                )
            )
        )
        val presenter = createSecurityAndPrivacyPresenter(room = room)
        presenter.test {
            skipItems(1)
            with(awaitItem()) {
                assertThat(editedSettings.roomAccess).isEqualTo(SecurityAndPrivacyRoomAccess.Anyone)
                assertThat(editedSettings.isEncrypted).isFalse()
                assertThat(availableHistoryVisibilities).contains(SecurityAndPrivacyHistoryVisibility.WorldReadable)
                assertThat(availableHistoryVisibilities).doesNotContain(SecurityAndPrivacyHistoryVisibility.Invited)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - availableHistoryVisibilities includes Invited for InviteOnly access`() = runTest {
        val presenter = createSecurityAndPrivacyPresenter()
        presenter.test {
            skipItems(1)
            with(awaitItem()) {
                assertThat(editedSettings.roomAccess).isEqualTo(SecurityAndPrivacyRoomAccess.InviteOnly)
                assertThat(availableHistoryVisibilities).contains(SecurityAndPrivacyHistoryVisibility.Invited)
                assertThat(availableHistoryVisibilities).doesNotContain(SecurityAndPrivacyHistoryVisibility.WorldReadable)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - availableHistoryVisibilities excludes WorldReadable when encrypted`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                roomPermissions = roomPermissions(),
                initialRoomInfo = aRoomInfo(
                    joinRule = JoinRule.Public,
                    historyVisibility = RoomHistoryVisibility.Shared,
                    isEncrypted = true,
                )
            )
        )
        val presenter = createSecurityAndPrivacyPresenter(room = room)
        presenter.test {
            skipItems(1)
            with(awaitItem()) {
                assertThat(editedSettings.roomAccess).isEqualTo(SecurityAndPrivacyRoomAccess.Anyone)
                assertThat(editedSettings.isEncrypted).isTrue()
                assertThat(availableHistoryVisibilities).contains(SecurityAndPrivacyHistoryVisibility.Invited)
                assertThat(availableHistoryVisibilities).doesNotContain(SecurityAndPrivacyHistoryVisibility.WorldReadable)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - showSpaceMemberOption is true when savedSettings has SpaceMember`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                roomPermissions = roomPermissions(),
                initialRoomInfo = aRoomInfo(
                    joinRule = JoinRule.Restricted(
                        rules = persistentListOf(AllowRule.RoomMembership(A_ROOM_ID))
                    ),
                    historyVisibility = RoomHistoryVisibility.Shared,
                )
            )
        )
        // No spaces available, so isSpaceMemberSelectable should be false
        val presenter = createSecurityAndPrivacyPresenter(room = room)
        presenter.test {
            skipItems(1)
            with(awaitItem()) {
                assertThat(savedSettings.roomAccess).isInstanceOf(SecurityAndPrivacyRoomAccess.SpaceMember::class.java)
                assertThat(isSpaceMemberSelectable).isFalse()
                // showSpaceMemberOption should still be true because savedSettings has SpaceMember
                assertThat(showSpaceMemberOption).isTrue()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - showSpaceMemberOption is false when not selectable and savedSettings is not SpaceMember`() = runTest {
        // No spaces available, default InviteOnly join rule
        val presenter = createSecurityAndPrivacyPresenter()
        presenter.test {
            skipItems(1)
            with(awaitItem()) {
                assertThat(savedSettings.roomAccess).isEqualTo(SecurityAndPrivacyRoomAccess.InviteOnly)
                assertThat(isSpaceMemberSelectable).isFalse()
                assertThat(showSpaceMemberOption).isFalse()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - showManageSpaceFooter is true when Multiple mode and SpaceMember access`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                roomPermissions = roomPermissions(),
                initialRoomInfo = aRoomInfo(
                    historyVisibility = RoomHistoryVisibility.Shared,
                    joinRule = JoinRule.Invite
                )
            )
        )
        val client = FakeMatrixClient(
            userIdServerNameLambda = { "matrix.org" },
            spaceService = FakeSpaceService(
                joinedParentsResult = { _ ->
                    Result.success(listOf(aSpaceRoom(roomId = A_ROOM_ID), aSpaceRoom(roomId = RoomId("!space2:matrix.org"))))
                }
            )
        )
        val presenter = createSecurityAndPrivacyPresenter(
            room = room,
            matrixClient = client,
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.SpaceSettings.key to true)
            )
        )
        presenter.test {
            skipItems(1)
            val state = awaitItem()
            // Change to SpaceMember access
            val spaceMemberAccess = SecurityAndPrivacyRoomAccess.SpaceMember(
                spaceIds = persistentListOf(A_ROOM_ID)
            )
            state.eventSink(SecurityAndPrivacyEvent.ChangeRoomAccess(spaceMemberAccess))
            with(awaitItem()) {
                assertThat(editedSettings.roomAccess).isInstanceOf(SecurityAndPrivacyRoomAccess.SpaceMember::class.java)
                assertThat(showManageSpaceFooter).isTrue()
            }
        }
    }

    @Test
    fun `present - showManageSpaceFooter is false when Single mode`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                roomPermissions = roomPermissions(),
                initialRoomInfo = aRoomInfo(
                    historyVisibility = RoomHistoryVisibility.Shared,
                    joinRule = JoinRule.Invite
                )
            )
        )
        // Single space available
        val client = FakeMatrixClient(
            userIdServerNameLambda = { "matrix.org" },
            spaceService = FakeSpaceService(
                joinedParentsResult = { _ ->
                    Result.success(listOf(aSpaceRoom(roomId = A_ROOM_ID)))
                }
            )
        )
        val presenter = createSecurityAndPrivacyPresenter(
            room = room,
            matrixClient = client,
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.SpaceSettings.key to true)
            )
        )
        presenter.test {
            skipItems(1)
            val state = awaitItem()
            // Select SpaceMember access (single space auto-selects)
            state.eventSink(SecurityAndPrivacyEvent.SelectSpaceMemberAccess)
            with(awaitItem()) {
                assertThat(editedSettings.roomAccess).isInstanceOf(SecurityAndPrivacyRoomAccess.SpaceMember::class.java)
                // Single mode, so no footer
                assertThat(showManageSpaceFooter).isFalse()
            }
        }
    }

    @Test
    fun `present - isAskToJoinSelectable is true when Knock FF enabled`() = runTest {
        val presenter = createSecurityAndPrivacyPresenter(
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.Knock.key to true)
            )
        )
        presenter.test {
            skipItems(1)
            with(awaitItem()) {
                assertThat(isAskToJoinSelectable).isTrue()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - isAskToJoinSelectable is false when Knock FF disabled`() = runTest {
        val presenter = createSecurityAndPrivacyPresenter(
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.Knock.key to false)
            )
        )
        presenter.test {
            skipItems(1)
            with(awaitItem()) {
                assertThat(isAskToJoinSelectable).isFalse()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - SelectAskToJoinWithSpaceMembersAccess with single space auto-selects`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                roomPermissions = roomPermissions(),
                initialRoomInfo = aRoomInfo(
                    historyVisibility = RoomHistoryVisibility.Shared,
                    joinRule = JoinRule.Invite
                )
            )
        )
        val client = FakeMatrixClient(
            userIdServerNameLambda = { "matrix.org" },
            spaceService = FakeSpaceService(
                joinedParentsResult = { _ ->
                    Result.success(listOf(aSpaceRoom(roomId = A_ROOM_ID)))
                }
            )
        )
        val presenter = createSecurityAndPrivacyPresenter(
            room = room,
            matrixClient = client,
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(
                    FeatureFlags.Knock.key to true,
                    FeatureFlags.SpaceSettings.key to true,
                )
            )
        )
        presenter.test {
            skipItems(1)
            val state = awaitItem()
            assertThat(state.isAskToJoinWithSpaceMembersSelectable).isTrue()
            state.eventSink(SecurityAndPrivacyEvent.SelectAskToJoinWithSpaceMembersAccess)
            with(awaitItem()) {
                assertThat(editedSettings.roomAccess).isInstanceOf(SecurityAndPrivacyRoomAccess.AskToJoinWithSpaceMember::class.java)
                val access = editedSettings.roomAccess as SecurityAndPrivacyRoomAccess.AskToJoinWithSpaceMember
                assertThat(access.spaceIds).containsExactly(A_ROOM_ID)
            }
        }
    }

    @Test
    fun `present - showAskToJoinOption is true when savedSettings is AskToJoin`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                roomPermissions = roomPermissions(),
                initialRoomInfo = aRoomInfo(
                    joinRule = JoinRule.Knock,
                    historyVisibility = RoomHistoryVisibility.Shared,
                )
            )
        )
        // Knock FF disabled, but showAskToJoinOption should still be true because savedSettings has AskToJoin
        val presenter = createSecurityAndPrivacyPresenter(
            room = room,
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.Knock.key to false)
            )
        )
        presenter.test {
            skipItems(1)
            with(awaitItem()) {
                assertThat(savedSettings.roomAccess).isEqualTo(SecurityAndPrivacyRoomAccess.AskToJoin)
                assertThat(isAskToJoinSelectable).isFalse()
                assertThat(showAskToJoinOption).isTrue()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - showHistoryVisibilitySection is false for space`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                roomPermissions = roomPermissions(),
                initialRoomInfo = aRoomInfo(
                    historyVisibility = RoomHistoryVisibility.Shared,
                    joinRule = JoinRule.Invite,
                    isSpace = true,
                )
            )
        )
        val presenter = createSecurityAndPrivacyPresenter(room = room)
        presenter.test {
            skipItems(1)
            with(awaitItem()) {
                assertThat(showHistoryVisibilitySection).isFalse()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - showEncryptionSection is false for space`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                roomPermissions = roomPermissions(),
                initialRoomInfo = aRoomInfo(
                    historyVisibility = RoomHistoryVisibility.Shared,
                    joinRule = JoinRule.Invite,
                    isSpace = true,
                )
            )
        )
        val presenter = createSecurityAndPrivacyPresenter(room = room)
        presenter.test {
            skipItems(1)
            with(awaitItem()) {
                assertThat(showEncryptionSection).isFalse()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun roomPermissions(
        canChangeRoomAccess: Boolean = true,
        canChangeHistoryVisibility: Boolean = true,
        canChangeEncryption: Boolean = true,
        canChangeRoomVisibility: Boolean = true,
    ): RoomPermissions {
        return FakeRoomPermissions(
            canSendState = { eventType ->
                when (eventType) {
                    StateEventType.RoomJoinRules -> canChangeRoomAccess
                    StateEventType.RoomHistoryVisibility -> canChangeHistoryVisibility
                    StateEventType.RoomEncryption -> canChangeEncryption
                    StateEventType.RoomCanonicalAlias -> canChangeRoomVisibility
                    else -> lambdaError()
                }
            }
        )
    }

    private fun createSecurityAndPrivacyPresenter(
        serverName: String = "matrix.org",
        room: FakeJoinedRoom = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                roomPermissions = roomPermissions(),
                getRoomVisibilityResult = { Result.success(RoomVisibility.Private) },
                initialRoomInfo = aRoomInfo(historyVisibility = RoomHistoryVisibility.Shared, joinRule = JoinRule.Invite)
            ),
        ),
        navigator: SecurityAndPrivacyNavigator = FakeSecurityAndPrivacyNavigator(),
        featureFlagService: FeatureFlagService = FakeFeatureFlagService(),
        matrixClient: MatrixClient = FakeMatrixClient(
            userIdServerNameLambda = { serverName },
            spaceService = FakeSpaceService(
                joinedParentsResult = { Result.success(emptyList()) },
                getSpaceRoomResult = { null }
            ),
        ),
        spaceSelectionStateHolder: SpaceSelectionStateHolder = SpaceSelectionStateHolder(),
    ): SecurityAndPrivacyPresenter {
        return SecurityAndPrivacyPresenter(
            room = room,
            matrixClient = matrixClient,
            navigator = navigator,
            featureFlagService = featureFlagService,
            spaceSelectionStateHolder = spaceSelectionStateHolder,
        )
    }
}
