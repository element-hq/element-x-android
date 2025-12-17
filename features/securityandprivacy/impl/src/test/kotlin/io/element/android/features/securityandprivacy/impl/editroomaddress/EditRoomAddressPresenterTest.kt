/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.editroomaddress

import com.google.common.truth.Truth.assertThat
import io.element.android.features.securityandprivacy.impl.FakeSecurityAndPrivacyNavigator
import io.element.android.features.securityandprivacy.impl.SecurityAndPrivacyNavigator
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.alias.ResolvedRoomAlias
import io.element.android.libraries.matrix.api.room.alias.RoomAliasHelper
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.matrix.test.room.alias.FakeRoomAliasHelper
import io.element.android.libraries.matrix.ui.room.address.RoomAddressValidity
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Optional

class EditRoomAddressPresenterTest {
    @Test
    fun `present - initial state no address`() = runTest {
        val presenter = createEditRoomAddressPresenter(
            room = FakeJoinedRoom().apply {
                givenRoomInfo(aRoomInfo(name = ""))
            }
        )
        presenter.test {
            with(awaitItem()) {
                assertThat(homeserverName).isEqualTo("matrix.org")
                assertThat(canBeSaved).isFalse()
                assertThat(saveAction).isEqualTo(AsyncAction.Uninitialized)
                assertThat(roomAddressValidity).isEqualTo(RoomAddressValidity.Unknown)
                assertThat(roomAddress).isEmpty()
            }
        }
    }

    @Test
    fun `present - initial state address matching own homeserver`() = runTest {
        val room = FakeJoinedRoom().apply {
            givenRoomInfo(aRoomInfo(canonicalAlias = RoomAlias("#canonical:matrix.org")))
        }
        val presenter = createEditRoomAddressPresenter(room = room)
        presenter.test {
            with(awaitItem()) {
                assertThat(homeserverName).isEqualTo("matrix.org")
                assertThat(canBeSaved).isFalse()
                assertThat(saveAction).isEqualTo(AsyncAction.Uninitialized)
                assertThat(roomAddressValidity).isEqualTo(RoomAddressValidity.Unknown)
                assertThat(roomAddress).isEqualTo("canonical")
            }
        }
    }

    @Test
    fun `present - initial state address not matching own homeserver`() = runTest {
        val room = FakeJoinedRoom().apply {
            givenRoomInfo(
                aRoomInfo(
                    name = "",
                    canonicalAlias = RoomAlias("#canonical:notmatrix.org")
                )
            )
        }
        val presenter = createEditRoomAddressPresenter(room = room)
        presenter.test {
            with(awaitItem()) {
                assertThat(homeserverName).isEqualTo("matrix.org")
                assertThat(canBeSaved).isFalse()
                assertThat(saveAction).isEqualTo(AsyncAction.Uninitialized)
                assertThat(roomAddressValidity).isEqualTo(RoomAddressValidity.Unknown)
                assertThat(roomAddress).isEmpty()
            }
        }
    }

    @Test
    fun `present - room address change invalid state`() = runTest {
        val roomAliasHelper = FakeRoomAliasHelper(
            isRoomAliasValidLambda = { false }
        )
        val presenter = createEditRoomAddressPresenter(roomAliasHelper = roomAliasHelper)
        presenter.test {
            with(awaitItem()) {
                eventSink(EditRoomAddressEvents.RoomAddressChanged("invalid"))
            }
            with(awaitItem()) {
                assertThat(roomAddress).isEqualTo("invalid")
                assertThat(roomAddressValidity).isEqualTo(RoomAddressValidity.Unknown)
            }
            with(awaitItem()) {
                assertThat(roomAddressValidity).isEqualTo(RoomAddressValidity.InvalidSymbols)
                assertThat(canBeSaved).isFalse()
            }
        }
    }

    @Test
    fun `present - room address change valid state`() = runTest {
        val presenter = createEditRoomAddressPresenter()
        presenter.test {
            with(awaitItem()) {
                eventSink(EditRoomAddressEvents.RoomAddressChanged("valid"))
            }
            with(awaitItem()) {
                assertThat(roomAddress).isEqualTo("valid")
                assertThat(roomAddressValidity).isEqualTo(RoomAddressValidity.Unknown)
            }
            with(awaitItem()) {
                assertThat(roomAddressValidity).isEqualTo(RoomAddressValidity.Valid)
                assertThat(canBeSaved).isTrue()
            }
        }
    }

    @Test
    fun `present - room address change alias unavailable`() = runTest {
        val client = createMatrixClient(isAliasAvailable = false)
        val presenter = createEditRoomAddressPresenter(client = client)
        presenter.test {
            with(awaitItem()) {
                eventSink(EditRoomAddressEvents.RoomAddressChanged("valid"))
            }
            with(awaitItem()) {
                assertThat(roomAddress).isEqualTo("valid")
                assertThat(roomAddressValidity).isEqualTo(RoomAddressValidity.Unknown)
            }
            with(awaitItem()) {
                assertThat(roomAddressValidity).isEqualTo(RoomAddressValidity.NotAvailable)
                assertThat(canBeSaved).isFalse()
            }
        }
    }

    @Test
    fun `present - save success no current alias`() = runTest {
        val publishAliasInRoomDirectoryResult = lambdaRecorder<RoomAlias, Result<Boolean>> { _ -> Result.success(true) }
        val updateCanonicalAliasResult = lambdaRecorder<RoomAlias?, List<RoomAlias>, Result<Unit>> { _, _ -> Result.success(Unit) }
        val removeAliasFromRoomDirectoryResult = lambdaRecorder<RoomAlias, Result<Boolean>> { _ -> Result.success(true) }
        val closeEditAddressLambda = lambdaRecorder<Unit> { }
        val navigator = FakeSecurityAndPrivacyNavigator(
            closeEditRoomAddressLambda = closeEditAddressLambda
        )
        val room = FakeJoinedRoom(
            updateCanonicalAliasResult = updateCanonicalAliasResult,
            publishRoomAliasInRoomDirectoryResult = publishAliasInRoomDirectoryResult
        )
        val presenter = createEditRoomAddressPresenter(room = room, navigator = navigator)
        presenter.test {
            with(awaitItem()) {
                eventSink(EditRoomAddressEvents.RoomAddressChanged("valid"))
            }
            skipItems(1)
            with(awaitItem()) {
                assertThat(roomAddressValidity).isEqualTo(RoomAddressValidity.Valid)
                assertThat(canBeSaved).isTrue()
                eventSink(EditRoomAddressEvents.Save)
            }
            with(awaitItem()) {
                assertThat(saveAction).isEqualTo(AsyncAction.Loading)
            }
            with(awaitItem()) {
                assertThat(saveAction).isEqualTo(AsyncAction.Success(Unit))
            }

            val createdAlias = RoomAlias("#valid:matrix.org")
            assert(updateCanonicalAliasResult)
                .isCalledOnce()
                .with(value(createdAlias), value(emptyList<RoomAlias>()))

            assert(publishAliasInRoomDirectoryResult)
                .isCalledOnce()
                .with(value(createdAlias))

            assert(removeAliasFromRoomDirectoryResult).isNeverCalled()

            assert(closeEditAddressLambda).isCalledOnce()
        }
    }

    @Test
    fun `present - save success current canonical alias from own homeserver`() = runTest {
        val publishAliasInRoomDirectoryResult = lambdaRecorder<RoomAlias, Result<Boolean>> { _ -> Result.success(true) }
        val removeAliasFromRoomDirectoryResult = lambdaRecorder<RoomAlias, Result<Boolean>> { _ -> Result.success(true) }
        val updateCanonicalAliasResult = lambdaRecorder<RoomAlias?, List<RoomAlias>, Result<Unit>> { _, _ -> Result.success(Unit) }
        val closeEditAddressLambda = lambdaRecorder<Unit> { }

        val navigator = FakeSecurityAndPrivacyNavigator(closeEditRoomAddressLambda = closeEditAddressLambda)
        val canonicalAlias = RoomAlias("#canonical:matrix.org")
        val room = FakeJoinedRoom(
            updateCanonicalAliasResult = updateCanonicalAliasResult,
            publishRoomAliasInRoomDirectoryResult = publishAliasInRoomDirectoryResult,
            removeRoomAliasFromRoomDirectoryResult = removeAliasFromRoomDirectoryResult
        ).apply {
            givenRoomInfo(aRoomInfo(canonicalAlias = canonicalAlias))
        }
        val presenter = createEditRoomAddressPresenter(room = room, navigator = navigator)
        presenter.test {
            with(awaitItem()) {
                eventSink(EditRoomAddressEvents.RoomAddressChanged("valid"))
            }
            skipItems(1)
            with(awaitItem()) {
                assertThat(roomAddressValidity).isEqualTo(RoomAddressValidity.Valid)
                assertThat(canBeSaved).isTrue()
                eventSink(EditRoomAddressEvents.Save)
            }
            with(awaitItem()) {
                assertThat(saveAction).isEqualTo(AsyncAction.Loading)
            }
            with(awaitItem()) {
                assertThat(saveAction).isEqualTo(AsyncAction.Success(Unit))
            }

            val createdAlias = RoomAlias("#valid:matrix.org")
            assert(updateCanonicalAliasResult)
                .isCalledOnce()
                .with(value(createdAlias), value(emptyList<RoomAlias>()))

            assert(publishAliasInRoomDirectoryResult)
                .isCalledOnce()
                .with(value(createdAlias))

            assert(removeAliasFromRoomDirectoryResult)
                .isCalledOnce()
                .with(value(canonicalAlias))

            assert(closeEditAddressLambda).isCalledOnce()
        }
    }

    @Test
    fun `present - save success current canonical alias from other homeserver`() = runTest {
        val publishAliasInRoomDirectoryResult = lambdaRecorder<RoomAlias, Result<Boolean>> { _ -> Result.success(true) }
        val removeAliasFromRoomDirectoryResult = lambdaRecorder<RoomAlias, Result<Boolean>> { _ -> Result.success(true) }
        val updateCanonicalAliasResult = lambdaRecorder<RoomAlias?, List<RoomAlias>, Result<Unit>> { _, _ -> Result.success(Unit) }
        val closeEditAddressLambda = lambdaRecorder<Unit> { }

        val navigator = FakeSecurityAndPrivacyNavigator(closeEditRoomAddressLambda = closeEditAddressLambda)
        val canonicalAlias = RoomAlias("#canonical:notmatrix.org")
        val room = FakeJoinedRoom(
            updateCanonicalAliasResult = updateCanonicalAliasResult,
            publishRoomAliasInRoomDirectoryResult = publishAliasInRoomDirectoryResult,
            removeRoomAliasFromRoomDirectoryResult = removeAliasFromRoomDirectoryResult
        ).apply {
            givenRoomInfo(aRoomInfo(canonicalAlias = canonicalAlias))
        }
        val presenter = createEditRoomAddressPresenter(room = room, navigator = navigator)
        presenter.test {
            with(awaitItem()) {
                eventSink(EditRoomAddressEvents.RoomAddressChanged("valid"))
            }
            skipItems(1)
            with(awaitItem()) {
                assertThat(roomAddressValidity).isEqualTo(RoomAddressValidity.Valid)
                assertThat(canBeSaved).isTrue()
                eventSink(EditRoomAddressEvents.Save)
            }
            with(awaitItem()) {
                assertThat(saveAction).isEqualTo(AsyncAction.Loading)
            }
            with(awaitItem()) {
                assertThat(saveAction).isEqualTo(AsyncAction.Success(Unit))
            }

            val createdAlias = RoomAlias("#valid:matrix.org")
            assert(updateCanonicalAliasResult)
                .isCalledOnce()
                .with(value(canonicalAlias), value(listOf(createdAlias)))

            assert(publishAliasInRoomDirectoryResult)
                .isCalledOnce()
                .with(value(createdAlias))

            assert(removeAliasFromRoomDirectoryResult).isNeverCalled()

            assert(closeEditAddressLambda).isCalledOnce()
        }
    }

    @Test
    fun `present - save failure`() = runTest {
        val closeEditAddressLambda = lambdaRecorder<Unit> { }
        val navigator = FakeSecurityAndPrivacyNavigator(
            closeEditRoomAddressLambda = closeEditAddressLambda
        )
        val presenter = createEditRoomAddressPresenter(
            navigator = navigator,
            room = FakeJoinedRoom(
                publishRoomAliasInRoomDirectoryResult = {
                    Result.failure(AN_EXCEPTION)
                },
            )
        )
        presenter.test {
            with(awaitItem()) {
                eventSink(EditRoomAddressEvents.RoomAddressChanged("valid"))
            }
            skipItems(1)
            with(awaitItem()) {
                assertThat(roomAddressValidity).isEqualTo(RoomAddressValidity.Valid)
                assertThat(canBeSaved).isTrue()
                eventSink(EditRoomAddressEvents.Save)
            }
            with(awaitItem()) {
                assertThat(saveAction).isEqualTo(AsyncAction.Loading)
            }
            with(awaitItem()) {
                assertThat(saveAction).isInstanceOf(AsyncAction.Failure::class.java)
            }

            assert(closeEditAddressLambda).isNeverCalled()
        }
    }

    @Test
    fun `present - dismiss error`() = runTest {
        val presenter = createEditRoomAddressPresenter(
            room = FakeJoinedRoom(
                publishRoomAliasInRoomDirectoryResult = {
                    Result.failure(AN_EXCEPTION)
                },
            )
        )
        presenter.test {
            with(awaitItem()) {
                eventSink(EditRoomAddressEvents.Save)
            }
            assertThat(awaitItem().saveAction).isInstanceOf(AsyncAction.Loading::class.java)
            with(awaitItem()) {
                assertThat(saveAction).isInstanceOf(AsyncAction.Failure::class.java)
                eventSink(EditRoomAddressEvents.DismissError)
            }
            with(awaitItem()) {
                assertThat(saveAction).isEqualTo(AsyncAction.Uninitialized)
            }
        }
    }

    private fun createMatrixClient(isAliasAvailable: Boolean = true) = FakeMatrixClient(
        userIdServerNameLambda = { "matrix.org" },
        resolveRoomAliasResult = {
            val resolvedRoomAlias = if (isAliasAvailable) {
                Optional.empty()
            } else {
                Optional.of(ResolvedRoomAlias(A_ROOM_ID, emptyList()))
            }
            Result.success(resolvedRoomAlias)
        }
    )

    private fun createEditRoomAddressPresenter(
        client: FakeMatrixClient = createMatrixClient(),
        room: JoinedRoom = FakeJoinedRoom(),
        navigator: SecurityAndPrivacyNavigator = FakeSecurityAndPrivacyNavigator(),
        roomAliasHelper: RoomAliasHelper = FakeRoomAliasHelper()
    ): EditRoomAddressPresenter {
        return EditRoomAddressPresenter(
            room = room,
            client = client,
            roomAliasHelper = roomAliasHelper,
            navigator = navigator
        )
    }
}
