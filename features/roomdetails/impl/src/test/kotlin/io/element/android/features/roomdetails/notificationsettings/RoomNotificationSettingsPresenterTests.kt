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
import com.google.common.truth.Truth.assertThat
import io.element.android.features.roomdetails.aMatrixRoom
import io.element.android.features.roomdetails.impl.notificationsettings.RoomNotificationSettingsEvents
import io.element.android.features.roomdetails.impl.notificationsettings.RoomNotificationSettingsPresenter
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.libraries.matrix.test.notificationsettings.FakeNotificationSettingsService
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.tests.testutils.awaitLastSequentialItem
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoomNotificationSettingsPresenterTests {
    @Test
    fun `present - initial state is created from room info`() = runTest {
        val presenter = createRoomNotificationSettingsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.roomNotificationSettings.dataOrNull()).isNull()
            assertThat(initialState.defaultRoomNotificationMode).isNull()
            val loadedState = awaitItem()
            assertThat(loadedState.displayMentionsOnlyDisclaimer).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - notification mode changed`() = runTest {
        val presenter = createRoomNotificationSettingsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(RoomNotificationSettingsEvents.RoomNotificationModeChanged(RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY))
            val updatedState = consumeItemsUntilPredicate {
                it.roomNotificationSettings.dataOrNull()?.mode == RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY
            }.last()
            assertThat(updatedState.roomNotificationSettings.dataOrNull()?.mode).isEqualTo(RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - observe notification mode changed`() = runTest {
        val notificationSettingsService = FakeNotificationSettingsService()
        val presenter = createRoomNotificationSettingsPresenter(notificationSettingsService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            notificationSettingsService.setRoomNotificationMode(A_ROOM_ID, RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY)
            val updatedState = consumeItemsUntilPredicate {
                it.roomNotificationSettings.dataOrNull()?.mode == RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY
            }.last()
            assertThat(updatedState.roomNotificationSettings.dataOrNull()?.mode).isEqualTo(RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY)
        }
    }

    @Test
    fun `present - notification settings set custom failed`() = runTest {
        val notificationSettingsService = FakeNotificationSettingsService()
        notificationSettingsService.givenSetNotificationModeError(A_THROWABLE)
        val presenter = createRoomNotificationSettingsPresenter(notificationSettingsService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomNotificationSettingsEvents.SetNotificationMode(false))
            val failedState = consumeItemsUntilPredicate {
                it.setNotificationSettingAction.isFailure()
            }.last()

            assertThat(failedState.roomNotificationSettings.dataOrNull()?.isDefault).isTrue()
            assertThat(failedState.pendingSetDefault).isNull()
            assertThat(failedState.setNotificationSettingAction.isFailure()).isTrue()

            failedState.eventSink(RoomNotificationSettingsEvents.ClearSetNotificationError)

            val errorClearedState = consumeItemsUntilPredicate {
                it.setNotificationSettingAction.isUninitialized()
            }.last()
            assertThat(errorClearedState.setNotificationSettingAction.isUninitialized()).isTrue()
        }
    }

    @Test
    fun `present - notification settings set custom`() = runTest {
        val notificationSettingsService = FakeNotificationSettingsService()
        val presenter = createRoomNotificationSettingsPresenter(notificationSettingsService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomNotificationSettingsEvents.SetNotificationMode(false))
            val defaultState = consumeItemsUntilPredicate {
                it.roomNotificationSettings.dataOrNull()?.isDefault == false
            }.last()
            assertThat(defaultState.roomNotificationSettings.dataOrNull()?.isDefault).isFalse()
        }
    }

    @Test
    fun `present - notification settings restore default`() = runTest {
        val presenter = createRoomNotificationSettingsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomNotificationSettingsEvents.RoomNotificationModeChanged(RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY))
            initialState.eventSink(RoomNotificationSettingsEvents.SetNotificationMode(true))
            val defaultState = consumeItemsUntilPredicate {
                it.roomNotificationSettings.dataOrNull()?.mode == RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY
            }.last()
            assertThat(defaultState.roomNotificationSettings.dataOrNull()?.mode).isEqualTo(RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - notification settings restore default failed`() = runTest {
        val notificationSettingsService = FakeNotificationSettingsService()
        notificationSettingsService.givenRestoreDefaultNotificationModeError(A_THROWABLE)
        val presenter = createRoomNotificationSettingsPresenter(notificationSettingsService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomNotificationSettingsEvents.RoomNotificationModeChanged(RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY))
            initialState.eventSink(RoomNotificationSettingsEvents.SetNotificationMode(true))
            val failedState = consumeItemsUntilPredicate {
                it.restoreDefaultAction.isFailure()
            }.last()
            assertThat(failedState.restoreDefaultAction.isFailure()).isTrue()
            failedState.eventSink(RoomNotificationSettingsEvents.ClearRestoreDefaultError)

            val errorClearedState = consumeItemsUntilPredicate {
                it.restoreDefaultAction.isUninitialized()
            }.last()
            assertThat(errorClearedState.restoreDefaultAction.isUninitialized()).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - display mentions only warning for a room if homeserver does not support it and it's encrypted`() = runTest {
        val notificationService = FakeNotificationSettingsService().apply {
            givenCanHomeServerPushEncryptedEventsToDeviceResult(Result.success(false))
        }
        val room = aMatrixRoom(notificationSettingsService = notificationService, isEncrypted = true)
        val presenter = createRoomNotificationSettingsPresenter(notificationService, room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitLastSequentialItem().displayMentionsOnlyDisclaimer).isTrue()
        }
    }

    @Test
    fun `present - do not display mentions only warning for a room it's not encrypted`() = runTest {
        val notificationService = FakeNotificationSettingsService().apply {
            givenCanHomeServerPushEncryptedEventsToDeviceResult(Result.success(false))
        }
        val room = aMatrixRoom(notificationSettingsService = notificationService, isEncrypted = false)
        val presenter = createRoomNotificationSettingsPresenter(notificationService, room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitLastSequentialItem().displayMentionsOnlyDisclaimer).isFalse()
        }
    }

    private fun createRoomNotificationSettingsPresenter(
        notificationSettingsService: FakeNotificationSettingsService = FakeNotificationSettingsService(),
        room: FakeMatrixRoom = aMatrixRoom(notificationSettingsService = notificationSettingsService),
    ): RoomNotificationSettingsPresenter {
        return RoomNotificationSettingsPresenter(
            room = room,
            notificationSettingsService = notificationSettingsService,
            showUserDefinedSettingStyle = false,
        )
    }
}
