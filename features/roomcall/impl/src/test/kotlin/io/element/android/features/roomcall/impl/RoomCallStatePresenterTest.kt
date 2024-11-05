/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomcall.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.features.roomcall.api.RoomCallState
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoomCallStatePresenterTest {
    @Test
    fun `present - call is disabled if user cannot join it even if there is an ongoing call`() = runTest {
        val room = FakeMatrixRoom(
            canUserJoinCallResult = { Result.success(false) },
            canUserSendMessageResult = { _, _ -> Result.success(true) },
            canRedactOwnResult = { Result.success(true) },
            canRedactOtherResult = { Result.success(true) },
            typingNoticeResult = { Result.success(Unit) },
            canUserPinUnpinResult = { Result.success(true) },
        ).apply {
            givenRoomInfo(aRoomInfo(hasRoomCall = true))
        }
        val presenter = createRoomCallStatePresenter(matrixRoom = room)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(RoomCallState.OnGoing(canJoinCall = false))
        }
    }

    private fun createRoomCallStatePresenter(
        matrixRoom: MatrixRoom
    ): RoomCallStatePresenter {
        return RoomCallStatePresenter(
            room = matrixRoom,
        )
    }
}
