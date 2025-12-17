/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.members.details

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.roomdetails.impl.aJoinedRoom
import io.element.android.features.roomdetails.impl.members.aRoomMember
import io.element.android.features.userprofile.api.UserProfileEvents
import io.element.android.features.userprofile.api.UserProfilePresenterFactory
import io.element.android.features.userprofile.api.UserProfileVerificationState
import io.element.android.features.userprofile.shared.aUserProfileState
import io.element.android.libraries.androidutils.clipboard.ClipboardHelper
import io.element.android.libraries.androidutils.clipboard.FakeClipboardHelper
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.identity.IdentityState
import io.element.android.libraries.matrix.api.encryption.identity.IdentityStateChange
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.RoomMembersState
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class RoomMemberDetailsPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - returns the room member's data, then updates it if needed`() = runTest {
        val roomMember = aRoomMember(displayName = "Alice")
        val room = aJoinedRoom(
            userDisplayNameResult = { Result.success("A custom name") },
            userAvatarUrlResult = { Result.success("A custom avatar") },
            getUpdatedMemberResult = { Result.success(roomMember) },
        ).apply {
            givenRoomMembersState(RoomMembersState.Ready(persistentListOf(roomMember)))
        }
        val presenter = createRoomMemberDetailsPresenter(
            room = room,
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.userName).isEqualTo("Alice")
            assertThat(initialState.avatarUrl).isEqualTo("Profile avatar url")
            skipItems(1)
            val nextState = awaitItem()
            assertThat(nextState.userName).isEqualTo("A custom name")
            assertThat(nextState.avatarUrl).isEqualTo("A custom avatar")
        }
    }

    @Test
    fun `present - will recover when retrieving room member details fails`() = runTest {
        val roomMember = aRoomMember(
            displayName = "Alice",
            avatarUrl = "Alice Avatar url",
        )
        val room = aJoinedRoom(
            userDisplayNameResult = { Result.failure(RuntimeException()) },
            userAvatarUrlResult = { Result.failure(RuntimeException()) },
            getUpdatedMemberResult = { Result.failure(AN_EXCEPTION) },
        ).apply {
            givenRoomMembersState(RoomMembersState.Ready(persistentListOf(roomMember)))
        }

        val presenter = createRoomMemberDetailsPresenter(
            room = room,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.userName).isEqualTo("Alice")
            assertThat(initialState.avatarUrl).isEqualTo("Alice Avatar url")
        }
    }

    @Test
    fun `present - will fallback to original data if the updated data is null`() = runTest {
        val roomMember = aRoomMember(displayName = "Alice")
        val room = aJoinedRoom(
            userDisplayNameResult = { Result.success(null) },
            userAvatarUrlResult = { Result.success(null) },
            getUpdatedMemberResult = { Result.success(roomMember) }
        ).apply {
            givenRoomMembersState(RoomMembersState.Ready(persistentListOf(roomMember)))
        }
        val presenter = createRoomMemberDetailsPresenter(
            room = room,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.userName).isEqualTo("Alice")
            assertThat(initialState.avatarUrl).isEqualTo("Profile avatar url")
        }
    }

    @Test
    fun `present - will fallback to user profile if user is not a member of the room`() = runTest {
        val room = aJoinedRoom(
            userDisplayNameResult = { Result.failure(Exception("Not a member!")) },
            userAvatarUrlResult = { Result.failure(Exception("Not a member!")) },
            getUpdatedMemberResult = { Result.failure(AN_EXCEPTION) },
        )
        val presenter = createRoomMemberDetailsPresenter(
            room = room,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.userName).isEqualTo("Profile user name")
            assertThat(initialState.avatarUrl).isEqualTo("Profile avatar url")
        }
    }

    @Test
    fun `present - null cases`() = runTest {
        val roomMember = aRoomMember(
            displayName = null,
            avatarUrl = null,
        )
        val room = aJoinedRoom(
            userDisplayNameResult = { Result.success(null) },
            userAvatarUrlResult = { Result.success(null) },
            getUpdatedMemberResult = { Result.success(roomMember) },
        )
        val presenter = createRoomMemberDetailsPresenter(
            room = room,
            userProfilePresenterFactory = {
                Presenter {
                    aUserProfileState(
                        userName = null,
                        avatarUrl = null,
                    )
                }
            },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.userName).isNull()
            assertThat(initialState.avatarUrl).isNull()
        }
    }

    @Test
    fun `present - when user's identity is verified, the value in the state is VERIFIED`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
            getUpdatedMemberResult = { Result.success(aRoomMember(A_USER_ID)) },
            userDisplayNameResult = { Result.success("A custom name") },
            userAvatarUrlResult = { Result.success("A custom avatar") },
            initialRoomInfo = aRoomInfo(isEncrypted = true),
        )
        )
        val encryptionService = FakeEncryptionService(
            getUserIdentityResult = { Result.success(IdentityState.Verified) },
        )
        val presenter = createRoomMemberDetailsPresenter(room = room, encryptionService = encryptionService)
        presenter.test {
            // Initial state, then the verification state is updated
            assertThat(awaitItem().verificationState).isEqualTo(UserProfileVerificationState.UNKNOWN)
            consumeItemsUntilPredicate { it.verificationState == UserProfileVerificationState.VERIFIED }
        }
    }

    @Test
    fun `present - when user's identity is unknown, the value in the state is UNKNOWN`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
            getUpdatedMemberResult = { Result.success(aRoomMember(A_USER_ID)) },
            userDisplayNameResult = { Result.success("A custom name") },
            userAvatarUrlResult = { Result.success("A custom avatar") },
            initialRoomInfo = aRoomInfo(isEncrypted = true),
        )
        )
        val encryptionService = FakeEncryptionService(
            getUserIdentityResult = { Result.success(null) },
        )
        val presenter = createRoomMemberDetailsPresenter(room = room, encryptionService = encryptionService)
        presenter.test {
            // Initial state, then the verification state is updated
            assertThat(awaitItem().verificationState).isEqualTo(UserProfileVerificationState.UNKNOWN)
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - when user's identity is pinned, the value in the state is UNVERIFIED`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
            getUpdatedMemberResult = { Result.success(aRoomMember(A_USER_ID)) },
            userDisplayNameResult = { Result.success("A custom name") },
            userAvatarUrlResult = { Result.success("A custom avatar") },
            initialRoomInfo = aRoomInfo(isEncrypted = true),
        )
        )
        val encryptionService = FakeEncryptionService(
            getUserIdentityResult = { Result.success(IdentityState.Pinned) },
        )
        val presenter = createRoomMemberDetailsPresenter(room = room, encryptionService = encryptionService)
        presenter.test {
            // Initial state, then the verification state is updated
            assertThat(awaitItem().verificationState).isEqualTo(UserProfileVerificationState.UNKNOWN)
            consumeItemsUntilPredicate { it.verificationState == UserProfileVerificationState.UNVERIFIED }
        }
    }

    @Test
    fun `present - when user's identity is pin violation, the value in the state is UNVERIFIED`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
            getUpdatedMemberResult = { Result.success(aRoomMember(A_USER_ID)) },
            userDisplayNameResult = { Result.success("A custom name") },
            userAvatarUrlResult = { Result.success("A custom avatar") },
            initialRoomInfo = aRoomInfo(isEncrypted = true),
        )
        )
        val encryptionService = FakeEncryptionService(
            getUserIdentityResult = { Result.success(IdentityState.PinViolation) },
        )
        val presenter = createRoomMemberDetailsPresenter(room = room, encryptionService = encryptionService)
        presenter.test {
            // Initial state, then the verification state is updated
            assertThat(awaitItem().verificationState).isEqualTo(UserProfileVerificationState.UNKNOWN)
            consumeItemsUntilPredicate { it.verificationState == UserProfileVerificationState.UNVERIFIED }
        }
    }

    @Test
    fun `present - when user's identity has a verification violation, the value in the state is VERIFICATION_VIOLATION`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
            getUpdatedMemberResult = { Result.success(aRoomMember(A_USER_ID)) },
            userDisplayNameResult = { Result.success("A custom name") },
            userAvatarUrlResult = { Result.success("A custom avatar") },
            initialRoomInfo = aRoomInfo(isEncrypted = true),
        )
        )
        val encryptionService = FakeEncryptionService(
            getUserIdentityResult = { Result.success(IdentityState.VerificationViolation) },
        )
        val presenter = createRoomMemberDetailsPresenter(room = room, encryptionService = encryptionService)
        presenter.test {
            // Initial state, then the verification state is updated
            assertThat(awaitItem().verificationState).isEqualTo(UserProfileVerificationState.UNKNOWN)
            consumeItemsUntilPredicate { it.verificationState == UserProfileVerificationState.VERIFICATION_VIOLATION }
        }
    }

    @Test
    fun `present - user identity updates in real time if the room is encrypted`() = runTest {
        val identityStateChanges = MutableStateFlow(emptyList<IdentityStateChange>())
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
            getUpdatedMemberResult = { Result.success(aRoomMember(A_USER_ID)) },
            userDisplayNameResult = { Result.success("A custom name") },
            userAvatarUrlResult = { Result.success("A custom avatar") },
            initialRoomInfo = aRoomInfo(isEncrypted = true),
        ),
            identityStateChangesFlow = identityStateChanges,
        )
        val encryptionService = FakeEncryptionService(
            getUserIdentityResult = { Result.success(null) },
        )
        val presenter = createRoomMemberDetailsPresenter(room = room, encryptionService = encryptionService)
        presenter.test {
            // Initial state, then the verification state is updated
            assertThat(awaitItem().verificationState).isEqualTo(UserProfileVerificationState.UNKNOWN)

            room.emitSyncUpdate()

            identityStateChanges.emit(listOf(IdentityStateChange(A_USER_ID, IdentityState.Pinned)))
            consumeItemsUntilPredicate { it.verificationState == UserProfileVerificationState.UNVERIFIED }

            identityStateChanges.emit(listOf(IdentityStateChange(A_USER_ID, IdentityState.Verified)))
            consumeItemsUntilPredicate { it.verificationState == UserProfileVerificationState.VERIFIED }

            identityStateChanges.emit(listOf(IdentityStateChange(A_USER_ID, IdentityState.VerificationViolation)))
            consumeItemsUntilPredicate { it.verificationState == UserProfileVerificationState.VERIFICATION_VIOLATION }
        }
    }

    @Test
    fun `present - user identity can't update in real time if the room is not encrypted`() = runTest {
        val identityStateChanges = MutableStateFlow(emptyList<IdentityStateChange>())
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
            getUpdatedMemberResult = { Result.success(aRoomMember(A_USER_ID)) },
            userDisplayNameResult = { Result.success("A custom name") },
            userAvatarUrlResult = { Result.success("A custom avatar") },
            initialRoomInfo = aRoomInfo(isEncrypted = false),
        ),
            identityStateChangesFlow = identityStateChanges,
        )
        val encryptionService = FakeEncryptionService(
            getUserIdentityResult = { Result.success(null) },
        )
        val presenter = createRoomMemberDetailsPresenter(room = room, encryptionService = encryptionService)
        presenter.test {
            // Initial state, then the verification state is updated
            assertThat(awaitItem().verificationState).isEqualTo(UserProfileVerificationState.UNKNOWN)

            room.emitSyncUpdate()
            identityStateChanges.emit(listOf(IdentityStateChange(A_USER_ID, IdentityState.Pinned)))

            // No new events emitted
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - handles WithdrawVerification action`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
            getUpdatedMemberResult = { Result.success(aRoomMember(A_USER_ID)) },
            userDisplayNameResult = { Result.success("A custom name") },
            userAvatarUrlResult = { Result.success("A custom avatar") },
            initialRoomInfo = aRoomInfo(isEncrypted = true),
        )
        )
        val withdrawVerificationResult = lambdaRecorder<UserId, Result<Unit>> { Result.success(Unit) }
        val encryptionService = FakeEncryptionService(
            getUserIdentityResult = { Result.success(IdentityState.VerificationViolation) },
            withdrawVerificationResult = withdrawVerificationResult,
        )
        val presenter = createRoomMemberDetailsPresenter(room = room, encryptionService = encryptionService)
        presenter.test {
            // Initial state, then the verification state is updated
            val initialState = awaitItem()
            assertThat(initialState.verificationState).isEqualTo(UserProfileVerificationState.UNKNOWN)

            consumeItemsUntilPredicate { it.verificationState == UserProfileVerificationState.VERIFICATION_VIOLATION }

            initialState.eventSink(UserProfileEvents.WithdrawVerification)
            withdrawVerificationResult.assertions().isCalledOnce()
        }
    }

    private fun createRoomMemberDetailsPresenter(
        room: JoinedRoom,
        userProfilePresenterFactory: UserProfilePresenterFactory = UserProfilePresenterFactory {
            Presenter {
                aUserProfileState(
                    userName = "Profile user name",
                    avatarUrl = "Profile avatar url",
                )
            }
        },
        encryptionService: FakeEncryptionService = FakeEncryptionService(getUserIdentityResult = { Result.success(null) }),
        clipboardHelper: ClipboardHelper = FakeClipboardHelper(),
    ): RoomMemberDetailsPresenter {
        return RoomMemberDetailsPresenter(
            roomMemberId = UserId("@alice:server.org"),
            room = room,
            userProfilePresenterFactory = userProfilePresenterFactory,
            encryptionService = encryptionService,
            clipboardHelper = clipboardHelper,
        )
    }
}
