/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.roomdetails.notificationsettings

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth
import io.element.android.features.roomdetails.aMatrixRoom
import io.element.android.features.roomdetails.impl.notificationsettings.RoomNotificationSettingsEvents
import io.element.android.features.roomdetails.impl.notificationsettings.RoomNotificationSettingsPresenter
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.test.A_ROOM_NOTIFICATION_MODE
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoomNotificationSettingsPresenterTests {
    @Test
    fun `present - initial state is created from room info`() = runTest {
        val presenter = aNotificationPresenter
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.roomNotificationSettings).isNull()
            Truth.assertThat(initialState.defaultRoomNotificationMode).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - notification mode changed`() = runTest {
        val presenter = aNotificationPresenter
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(RoomNotificationSettingsEvents.RoomNotificationModeChanged(RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY))
            val updatedState = consumeItemsUntilPredicate {
                it.roomNotificationSettings?.mode ==  RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY
            }.last()
            Truth.assertThat(updatedState.roomNotificationSettings?.mode).isEqualTo(RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY)
        }
    }

    @Test
    fun `present - notification settings restore default`() = runTest {
        val presenter = aNotificationPresenter
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomNotificationSettingsEvents.RoomNotificationModeChanged(RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY))
            initialState.eventSink(RoomNotificationSettingsEvents.SetNotificationMode(true))
            val defaultState = consumeItemsUntilPredicate {
                it.roomNotificationSettings?.mode ==  A_ROOM_NOTIFICATION_MODE
            }.last()
            Truth.assertThat(defaultState.roomNotificationSettings?.mode).isEqualTo(A_ROOM_NOTIFICATION_MODE)
        }
    }

    private val aNotificationPresenter: RoomNotificationSettingsPresenter get() {
        val room = aMatrixRoom()
        return RoomNotificationSettingsPresenter(
            room = room,
            notificationSettingsService = room.notificationSettingsService
        )
    }
}
