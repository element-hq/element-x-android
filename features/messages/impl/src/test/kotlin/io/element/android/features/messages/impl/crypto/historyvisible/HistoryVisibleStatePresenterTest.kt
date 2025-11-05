/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.historyvisible

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.history.RoomHistoryVisibility
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class HistoryVisibleStatePresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createHistoryVisibleStatePresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.acknowledged).isFalse()
            assertThat(initialState.roomHistoryVisibility).isEqualTo(RoomHistoryVisibility.Joined)
        }
    }

    @Test
    fun `present - when the room history visibility changes, the presenter emits a new state`() = runTest {
        val room = FakeJoinedRoom()
        val presenter = createHistoryVisibleStatePresenter(room)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.roomHistoryVisibility).isEqualTo(RoomHistoryVisibility.Joined)
            room.givenRoomInfo(aRoomInfo(historyVisibility = RoomHistoryVisibility.Shared))
            val nextState = awaitItem()
            assertThat(nextState.roomHistoryVisibility).isEqualTo(RoomHistoryVisibility.Shared)
        }
    }

    private fun createHistoryVisibleStatePresenter(
        room: JoinedRoom = FakeJoinedRoom(),
    ): HistoryVisibleStatePresenter {
        return HistoryVisibleStatePresenter(
            room = room,
            repository = FakeHistoryVisibleAcknowledgementRepository()
        )
    }
}
