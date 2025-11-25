/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.historyvisible

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.history.RoomHistoryVisibility
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.awaitLastSequentialItem
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class HistoryVisibleStatePresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - not visible if feature disabled`() = runTest {
        val room = FakeJoinedRoom()
        room.givenRoomInfo(aRoomInfo(historyVisibility = RoomHistoryVisibility.Joined, isEncrypted = true))
        val presenter = createHistoryVisibleStatePresenter(room, enabled = false, acknowledged = false)
        presenter.test {
            assertThat(awaitLastSequentialItem().showAlert).isFalse()
        }
    }

    @Test
    fun `present - initial with room shared, unencrypted`() = runTest {
        val room = FakeJoinedRoom()
        room.givenRoomInfo(aRoomInfo(historyVisibility = RoomHistoryVisibility.Shared, isEncrypted = false))
        val presenter = createHistoryVisibleStatePresenter(room)
        presenter.test {
            assertThat(awaitLastSequentialItem().showAlert).isFalse()
        }
    }

    @Test
    fun `present - initial with room joined, encrypted`() = runTest {
        val room = FakeJoinedRoom()
        room.givenRoomInfo(aRoomInfo(historyVisibility = RoomHistoryVisibility.Joined, isEncrypted = false))
        val presenter = createHistoryVisibleStatePresenter(room)
        presenter.test {
            assertThat(awaitLastSequentialItem().showAlert).isFalse()
        }
    }

    @Test
    fun `present - initial with room shared, encrypted, unacknowledged`() = runTest {
        val room = FakeJoinedRoom()
        room.givenRoomInfo(aRoomInfo(historyVisibility = RoomHistoryVisibility.Shared, isEncrypted = true))
        val presenter = createHistoryVisibleStatePresenter(room, acknowledged = false)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.showAlert).isFalse()
            val nextState = awaitItem()
            assertThat(nextState.showAlert).isTrue()
        }
    }

    @Test
    fun `present - initial with room shared, encrypted, acknowledged`() = runTest {
        val room = FakeJoinedRoom()
        room.givenRoomInfo(aRoomInfo(historyVisibility = RoomHistoryVisibility.Shared, isEncrypted = true))
        val presenter = createHistoryVisibleStatePresenter(room, acknowledged = true)
        presenter.test {
            assertThat(awaitLastSequentialItem().showAlert).isFalse()
        }
    }

    @Test
    fun `present - transition from joined + unencrypted, to shared + encrypted`() = runTest {
        val room = FakeJoinedRoom()
        val featureFlagService = FakeFeatureFlagService(mapOf(FeatureFlags.EnableKeyShareOnInvite.key to true))
        val repository = FakeHistoryVisibleAcknowledgementRepository()

        room.givenRoomInfo(aRoomInfo(historyVisibility = RoomHistoryVisibility.Joined, isEncrypted = false))

        val presenter = HistoryVisibleStatePresenter(
            featureFlagService,
            repository,
            room,
        )

        presenter.test {
            // emitted by the feature flag service(?)
            assertThat(awaitItem().showAlert).isFalse()

            // emitted state from room info assignment
            assertThat(awaitItem().showAlert).isFalse()

            // room is marked as encrypted
            room.givenRoomInfo(aRoomInfo(historyVisibility = RoomHistoryVisibility.Joined, isEncrypted = true))
            assertThat(awaitItem().showAlert).isFalse()

            // room history visibility is changed to shared
            room.givenRoomInfo(aRoomInfo(historyVisibility = RoomHistoryVisibility.Shared, isEncrypted = true))
            assertThat(awaitItem().showAlert).isTrue()

            // alert is acknowledged
            repository.setAcknowledged(room.roomId, true)
            assertThat(awaitItem().showAlert).isFalse()
        }
    }

    private fun createHistoryVisibleStatePresenter(
        room: JoinedRoom = FakeJoinedRoom(),
        enabled: Boolean = true,
        acknowledged: Boolean = false
    ): HistoryVisibleStatePresenter {
        return HistoryVisibleStatePresenter(
            room = room,
            featureFlagService = FakeFeatureFlagService(mapOf("feature.enableKeyShareOnInvite" to enabled)),
            repository = FakeHistoryVisibleAcknowledgementRepository.withRoom(room.roomId, acknowledged)
        )
    }
}
