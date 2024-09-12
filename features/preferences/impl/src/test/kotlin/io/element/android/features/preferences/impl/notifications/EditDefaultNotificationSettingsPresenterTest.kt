/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.notifications

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.preferences.impl.notifications.edit.EditDefaultNotificationSettingPresenter
import io.element.android.features.preferences.impl.notifications.edit.EditDefaultNotificationSettingStateEvents
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.notificationsettings.FakeNotificationSettingsService
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomSummary
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.tests.testutils.awaitLastSequentialItem
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import kotlinx.coroutines.test.runTest
import org.junit.Test

class EditDefaultNotificationSettingsPresenterTest {
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
            roomListService.postAllRooms(listOf(aRoomSummary(notificationMode = RoomNotificationMode.ALL_MESSAGES)))
            val loadedState = consumeItemsUntilPredicate { state ->
                state.roomsWithUserDefinedMode.any { it.userDefinedNotificationMode == RoomNotificationMode.ALL_MESSAGES }
            }.last()
            assertThat(loadedState.roomsWithUserDefinedMode.any { it.userDefinedNotificationMode == RoomNotificationMode.ALL_MESSAGES }).isTrue()
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
