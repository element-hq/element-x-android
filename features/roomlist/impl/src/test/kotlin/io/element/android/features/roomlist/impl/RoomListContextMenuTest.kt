/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomlist.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureCalledOnceWithParam
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomListContextMenuTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on Mark as read generates expected Events`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>()
        val contextMenu = aContextMenuShown(hasNewContent = true)
        rule.setContent {
            RoomListContextMenu(
                contextMenu = contextMenu,
                eventSink = eventsRecorder,
                onRoomSettingsClick = EnsureNeverCalledWithParam(),
            )
        }
        rule.clickOn(R.string.screen_roomlist_mark_as_read)
        eventsRecorder.assertList(
            listOf(
                RoomListEvents.HideContextMenu,
                RoomListEvents.MarkAsRead(contextMenu.roomId),
            )
        )
    }

    @Test
    fun `clicking on Mark as unread generates expected Events`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>()
        val contextMenu = aContextMenuShown(hasNewContent = false)
        rule.setContent {
            RoomListContextMenu(
                contextMenu = contextMenu,
                eventSink = eventsRecorder,
                onRoomSettingsClick = EnsureNeverCalledWithParam(),
            )
        }
        rule.clickOn(R.string.screen_roomlist_mark_as_unread)
        eventsRecorder.assertList(
            listOf(
                RoomListEvents.HideContextMenu,
                RoomListEvents.MarkAsUnread(contextMenu.roomId),
            )
        )
    }

    @Test
    fun `clicking on Leave dm generates expected Events`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>()
        val contextMenu = aContextMenuShown(isDm = true)
        rule.setContent {
            RoomListContextMenu(
                contextMenu = contextMenu,
                eventSink = eventsRecorder,
                onRoomSettingsClick = EnsureNeverCalledWithParam(),
            )
        }
        rule.clickOn(CommonStrings.action_leave_conversation)
        eventsRecorder.assertList(
            listOf(
                RoomListEvents.HideContextMenu,
                RoomListEvents.LeaveRoom(contextMenu.roomId),
            )
        )
    }

    @Test
    fun `clicking on Leave room generates expected Events`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>()
        val contextMenu = aContextMenuShown(isDm = false)
        rule.setContent {
            RoomListContextMenu(
                contextMenu = contextMenu,
                eventSink = eventsRecorder,
                onRoomSettingsClick = EnsureNeverCalledWithParam(),
            )
        }
        rule.clickOn(CommonStrings.action_leave_room)
        eventsRecorder.assertList(
            listOf(
                RoomListEvents.HideContextMenu,
                RoomListEvents.LeaveRoom(contextMenu.roomId),
            )
        )
    }

    @Test
    fun `clicking on Settings invokes the expected callback and generates expected Event`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>()
        val contextMenu = aContextMenuShown()
        val callback = EnsureCalledOnceWithParam(contextMenu.roomId, Unit)
        rule.setContent {
            RoomListContextMenu(
                contextMenu = contextMenu,
                eventSink = eventsRecorder,
                onRoomSettingsClick = callback,
            )
        }
        rule.clickOn(CommonStrings.common_settings)
        eventsRecorder.assertSingle(RoomListEvents.HideContextMenu)
        callback.assertSuccess()
    }

    @Test
    fun `clicking on Favourites generates expected Event`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>()
        val contextMenu = aContextMenuShown(isDm = false, isFavorite = false)
        val callback = EnsureNeverCalledWithParam<RoomId>()
        rule.setContent {
            RoomListContextMenu(
                contextMenu = contextMenu,
                eventSink = eventsRecorder,
                onRoomSettingsClick = callback,
            )
        }
        rule.clickOn(CommonStrings.common_favourite)
        eventsRecorder.assertList(
            listOf(
                RoomListEvents.SetRoomIsFavorite(contextMenu.roomId, true),
            )
        )
    }
}
