/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.notifications

import com.google.common.truth.Truth.assertThat
import io.element.android.features.preferences.impl.notifications.edit.EditDefaultNotificationSettingPresenter
import io.element.android.features.preferences.impl.notifications.edit.EditDefaultNotificationSettingStateEvents
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.notificationsettings.FakeNotificationSettingsService
import io.element.android.libraries.matrix.test.room.aRoomSummary
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.tests.testutils.awaitLastSequentialItem
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Test

class EditDefaultNotificationSettingsPresenterTest {
    @Test
    fun `present - ensures initial state is correct`() = runTest {
        val notificationSettingsService = FakeNotificationSettingsService(
            getRoomsWithUserDefinedRulesResult = { Result.success(emptyList()) },
        )
        val presenter = createEditDefaultNotificationSettingPresenter(notificationSettingsService)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.mode).isNull()
            assertThat(initialState.isOneToOne).isFalse()
            assertThat(initialState.roomsWithUserDefinedMode).isEmpty()

            val loadedState = consumeItemsUntilPredicate {
                it.mode == RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY
            }.last()
            assertThat(loadedState.mode).isEqualTo(RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY)

            assertThat(loadedState.displayMentionsOnlyDisclaimer).isFalse()
        }
    }

    @Test
    fun `present - ensure list of rooms with user defined mode`() = runTest {
        val notificationSettingsService = FakeNotificationSettingsService(
            initialRoomMode = RoomNotificationMode.ALL_MESSAGES,
            initialRoomModeIsDefault = false,
            getRoomsWithUserDefinedRulesResult = { Result.success(listOf(A_ROOM_ID)) },
        )
        val roomListService = FakeRoomListService()
        val presenter = createEditDefaultNotificationSettingPresenter(notificationSettingsService, roomListService)
        presenter.test {
            roomListService.postAllRooms(listOf(aRoomSummary(userDefinedNotificationMode = RoomNotificationMode.ALL_MESSAGES)))
            val loadedState = consumeItemsUntilPredicate { state ->
                state.roomsWithUserDefinedMode.any { it.notificationMode == RoomNotificationMode.ALL_MESSAGES }
            }.last()
            assertThat(loadedState.roomsWithUserDefinedMode.any { it.notificationMode == RoomNotificationMode.ALL_MESSAGES }).isTrue()
        }
    }

    @Test
    fun `present - ensure list of rooms is sorted`() = runTest {
        val notificationSettingsService = FakeNotificationSettingsService(
            initialRoomMode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY,
            initialRoomModeIsDefault = false,
            getRoomsWithUserDefinedRulesResult = { Result.success(listOf(A_ROOM_ID, A_ROOM_ID_2)) },
        )
        val roomListService = FakeRoomListService()
        val presenter = createEditDefaultNotificationSettingPresenter(notificationSettingsService, roomListService)
        presenter.test {
            roomListService.postAllRooms(
                listOf(
                    aRoomSummary(
                        roomId = A_ROOM_ID,
                        name = "Z",
                        userDefinedNotificationMode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY,
                    ),
                    aRoomSummary(
                        roomId = A_ROOM_ID_2,
                        name = "A",
                        userDefinedNotificationMode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY,
                    ),
                ),
            )
            val loadedState = consumeItemsUntilPredicate { state ->
                state.roomsWithUserDefinedMode.any { it.notificationMode == RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY }
            }.last()
            assertThat(loadedState.roomsWithUserDefinedMode[0].name).isEqualTo("A")
            assertThat(loadedState.roomsWithUserDefinedMode[1].name).isEqualTo("Z")
        }
    }

    @Test
    fun `present - ensure list of rooms is sorted, with name null`() = runTest {
        val notificationSettingsService = FakeNotificationSettingsService(
            initialRoomMode = RoomNotificationMode.MUTE,
            initialRoomModeIsDefault = false,
            getRoomsWithUserDefinedRulesResult = { Result.success(listOf(A_ROOM_ID, A_ROOM_ID_2)) },
        )
        val roomListService = FakeRoomListService()
        val presenter = createEditDefaultNotificationSettingPresenter(notificationSettingsService, roomListService)
        presenter.test {
            roomListService.postAllRooms(
                listOf(
                    aRoomSummary(
                        roomId = A_ROOM_ID,
                        name = "Z",
                        userDefinedNotificationMode = RoomNotificationMode.MUTE,
                    ),
                    aRoomSummary(
                        roomId = A_ROOM_ID_2,
                        name = null,
                        userDefinedNotificationMode = RoomNotificationMode.MUTE,
                    ),
                ),
            )
            val loadedState = consumeItemsUntilPredicate { state ->
                state.roomsWithUserDefinedMode.any { it.notificationMode == RoomNotificationMode.MUTE }
            }.last()
            assertThat(loadedState.roomsWithUserDefinedMode[0].name).isNull()
            assertThat(loadedState.roomsWithUserDefinedMode[1].name).isEqualTo("Z")
        }
    }

    @Test
    fun `present - edit default notification setting`() = runTest {
        val presenter = createEditDefaultNotificationSettingPresenter(
            notificationSettingsService = FakeNotificationSettingsService(
                getRoomsWithUserDefinedRulesResult = { Result.success(emptyList()) },
            ),
        )
        presenter.test {
            awaitItem().eventSink(EditDefaultNotificationSettingStateEvents.SetNotificationMode(RoomNotificationMode.ALL_MESSAGES))
            val loadedState = consumeItemsUntilPredicate {
                it.mode == RoomNotificationMode.ALL_MESSAGES
            }.last()
            assertThat(loadedState.mode).isEqualTo(RoomNotificationMode.ALL_MESSAGES)
        }
    }

    @Test
    fun `present - edit default notification setting failed`() = runTest {
        val notificationSettingsService = FakeNotificationSettingsService(
            getRoomsWithUserDefinedRulesResult = { Result.success(emptyList()) },
        )
        val presenter = createEditDefaultNotificationSettingPresenter(notificationSettingsService)
        notificationSettingsService.givenSetDefaultNotificationModeError(AN_EXCEPTION)
        presenter.test {
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
        val notificationSettingsService = FakeNotificationSettingsService(
            getRoomsWithUserDefinedRulesResult = { Result.success(emptyList()) },
        ).apply {
            givenCanHomeServerPushEncryptedEventsToDeviceResult(Result.success(false))
        }
        val presenter = createEditDefaultNotificationSettingPresenter(notificationSettingsService)
        presenter.test {
            assertThat(awaitLastSequentialItem().displayMentionsOnlyDisclaimer).isTrue()
        }
    }

    private fun createEditDefaultNotificationSettingPresenter(
        notificationSettingsService: FakeNotificationSettingsService = FakeNotificationSettingsService(),
        roomListService: FakeRoomListService = FakeRoomListService(),
    ): EditDefaultNotificationSettingPresenter {
        return EditDefaultNotificationSettingPresenter(
            notificationSettingsService = notificationSettingsService,
            isOneToOne = false,
            roomListService = roomListService,
        )
    }
}
