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

package io.element.android.features.preferences.impl.notifications

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.preferences.impl.notifications.edit.EditDefaultNotificationSettingPresenter
import io.element.android.features.preferences.impl.notifications.edit.EditDefaultNotificationSettingStateEvents
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.notificationsettings.FakeNotificationSettingsService
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomSummaryDetail
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.tests.testutils.awaitLastSequentialItem
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import kotlinx.coroutines.test.runTest
import org.junit.Test

class EditDefaultNotificationSettingsPresenterTests {
    @Test
    fun `present - ensures initial state is correct`() = runTest {
        val notificationSettingsService = FakeNotificationSettingsService()
        val presenter = createEditDefaultNotificationSettingPresenter(notificationSettingsService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.mode).isNull()
            assertThat(initialState.isOneToOne).isFalse()

            val loadedState = consumeItemsUntilPredicate {
                it.mode == RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY
            }.last()
            assertThat(loadedState.mode).isEqualTo(RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY)

            assertThat(loadedState.displayMentionsOnlyDisclaimer).isFalse()
        }
    }

    @Test
    fun `present - ensure list of rooms with user defined mode`() = runTest {
        val room = FakeMatrixRoom()
        val notificationSettingsService = FakeNotificationSettingsService(
            initialRoomMode = RoomNotificationMode.ALL_MESSAGES,
            initialRoomModeIsDefault = false
        )
        val matrixClient = FakeMatrixClient(notificationSettingsService = notificationSettingsService).apply {
            givenGetRoomResult(A_ROOM_ID, room)
        }
        val roomListService = FakeRoomListService()
        val presenter = createEditDefaultNotificationSettingPresenter(notificationSettingsService, roomListService, matrixClient)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            roomListService.postAllRooms(listOf(RoomSummary.Filled(aRoomSummaryDetail(notificationMode = RoomNotificationMode.ALL_MESSAGES))))
            val loadedState = consumeItemsUntilPredicate { state ->
                state.roomsWithUserDefinedMode.any { it.details.notificationMode == RoomNotificationMode.ALL_MESSAGES }
            }.last()
            assertThat(loadedState.roomsWithUserDefinedMode.any { it.details.notificationMode == RoomNotificationMode.ALL_MESSAGES }).isTrue()
        }
    }

    @Test
    fun `present - edit default notification setting`() = runTest {
        val presenter = createEditDefaultNotificationSettingPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(EditDefaultNotificationSettingStateEvents.SetNotificationMode(RoomNotificationMode.ALL_MESSAGES))
            val loadedState = consumeItemsUntilPredicate {
                it.mode == RoomNotificationMode.ALL_MESSAGES
            }.last()
            assertThat(loadedState.mode).isEqualTo(RoomNotificationMode.ALL_MESSAGES)
        }
    }

    @Test
    fun `present - edit default notification setting failed`() = runTest {
        val notificationSettingsService = FakeNotificationSettingsService()
        val presenter = createEditDefaultNotificationSettingPresenter(notificationSettingsService)
        notificationSettingsService.givenSetDefaultNotificationModeError(A_THROWABLE)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(EditDefaultNotificationSettingStateEvents.SetNotificationMode(RoomNotificationMode.ALL_MESSAGES))
            val errorState = consumeItemsUntilPredicate {
                it.changeNotificationSettingAction.isFailure()
            }.last()
            assertThat(errorState.changeNotificationSettingAction.isFailure()).isTrue()
            errorState.eventSink(EditDefaultNotificationSettingStateEvents.ClearError)
            val clearErrorState = consumeItemsUntilPredicate {
                it.changeNotificationSettingAction.isUninitialized()
            }.last()
            assertThat(clearErrorState.changeNotificationSettingAction.isUninitialized()).isTrue()
        }
    }

    @Test
    fun `present - display mentions only warning if homeserver does not support it`() = runTest {
        val notificationSettingsService = FakeNotificationSettingsService().apply {
            givenCanHomeServerPushEncryptedEventsToDeviceResult(Result.success(false))
        }
        val presenter = createEditDefaultNotificationSettingPresenter(notificationSettingsService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitLastSequentialItem().displayMentionsOnlyDisclaimer).isTrue()
        }
    }

    private fun createEditDefaultNotificationSettingPresenter(
        notificationSettingsService: FakeNotificationSettingsService = FakeNotificationSettingsService(),
        roomListService: FakeRoomListService = FakeRoomListService(),
        matrixClient: FakeMatrixClient = FakeMatrixClient(notificationSettingsService = notificationSettingsService)
    ): EditDefaultNotificationSettingPresenter {
        return EditDefaultNotificationSettingPresenter(
            notificationSettingsService = notificationSettingsService,
            isOneToOne = false,
            roomListService = roomListService,
            matrixClient = matrixClient
        )
    }
}
