/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.startchat.impl.joinbyaddress

import com.google.common.truth.Truth.assertThat
import io.element.android.features.startchat.StartChatNavigator
import io.element.android.features.startchat.impl.FakeStartChatNavigator
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.room.alias.RoomAliasHelper
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.alias.FakeRoomAliasHelper
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Test

class JoinBaseRoomByAddressPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createJoinRoomByAddressPresenter()
        presenter.test {
            with(awaitItem()) {
                assertThat(address).isEmpty()
                assertThat(addressState).isEqualTo(RoomAddressState.Unknown)
            }
        }
    }

    @Test
    fun `present - invalid address`() = runTest {
        val presenter = createJoinRoomByAddressPresenter(
            roomAliasHelper = FakeRoomAliasHelper(
                isRoomAliasValidLambda = { false }
            )
        )
        presenter.test {
            with(awaitItem()) {
                eventSink(JoinRoomByAddressEvents.UpdateAddress("invalid_address"))
            }
            with(awaitItem()) {
                assertThat(address).isEqualTo("invalid_address")
                assertThat(addressState).isEqualTo(RoomAddressState.Unknown)
                eventSink(JoinRoomByAddressEvents.Continue)
            }
            // The address should be marked as invalid only after the user tries to continue
            with(awaitItem()) {
                assertThat(address).isEqualTo("invalid_address")
                assertThat(addressState).isEqualTo(RoomAddressState.Invalid)
            }
        }
    }

    @Test
    fun `present - room found`() = runTest {
        val openRoomLambda = lambdaRecorder<RoomIdOrAlias, List<String>, Unit> { _, _ -> }
        val dismissJoinRoomByAddressLambda = lambdaRecorder<Unit> { }
        val navigator = FakeStartChatNavigator(
            openRoomLambda = openRoomLambda,
            dismissJoinRoomByAddressLambda = dismissJoinRoomByAddressLambda
        )
        val presenter = createJoinRoomByAddressPresenter(navigator = navigator)
        presenter.test {
            with(awaitItem()) {
                eventSink(JoinRoomByAddressEvents.UpdateAddress("#room_found:matrix.org"))
            }
            with(awaitItem()) {
                assertThat(address).isEqualTo("#room_found:matrix.org")
                assertThat(addressState).isEqualTo(RoomAddressState.Unknown)
            }
            with(awaitItem()) {
                assertThat(address).isEqualTo("#room_found:matrix.org")
                assertThat(addressState).isInstanceOf(RoomAddressState.RoomFound::class.java)
                eventSink(JoinRoomByAddressEvents.Continue)
            }
            assert(openRoomLambda).isCalledOnce()
            assert(dismissJoinRoomByAddressLambda).isCalledOnce()
        }
    }

    @Test
    fun `present - room not found`() = runTest {
        val presenter = createJoinRoomByAddressPresenter(
            matrixClient = FakeMatrixClient(
                resolveRoomAliasResult = { Result.failure(RuntimeException()) }
            )
        )
        presenter.test {
            with(awaitItem()) {
                eventSink(JoinRoomByAddressEvents.UpdateAddress("#room_not_found:matrix.org"))
            }
            with(awaitItem()) {
                assertThat(address).isEqualTo("#room_not_found:matrix.org")
                assertThat(addressState).isEqualTo(RoomAddressState.Unknown)
                eventSink(JoinRoomByAddressEvents.Continue)
            }
            with(awaitItem()) {
                assertThat(address).isEqualTo("#room_not_found:matrix.org")
                assertThat(addressState).isEqualTo(RoomAddressState.Resolving)
            }
            with(awaitItem()) {
                assertThat(address).isEqualTo("#room_not_found:matrix.org")
                assertThat(addressState).isEqualTo(RoomAddressState.RoomNotFound)
            }
        }
    }

    @Test
    fun `present - dismiss`() = runTest {
        val dismissJoinRoomByAddressLambda = lambdaRecorder<Unit> { }
        val navigator = FakeStartChatNavigator(
            dismissJoinRoomByAddressLambda = dismissJoinRoomByAddressLambda
        )
        val presenter = createJoinRoomByAddressPresenter(navigator = navigator)
        presenter.test {
            with(awaitItem()) {
                eventSink(JoinRoomByAddressEvents.Dismiss)
            }
            assert(dismissJoinRoomByAddressLambda).isCalledOnce()
        }
    }

    private fun createJoinRoomByAddressPresenter(
        navigator: StartChatNavigator = FakeStartChatNavigator(),
        matrixClient: MatrixClient = FakeMatrixClient(),
        roomAliasHelper: RoomAliasHelper = FakeRoomAliasHelper(),
    ): JoinRoomByAddressPresenter {
        return JoinRoomByAddressPresenter(
            navigator = navigator,
            client = matrixClient,
            roomAliasHelper = roomAliasHelper,
        )
    }
}
