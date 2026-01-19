/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.test.notifications.channels

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.push.impl.notifications.channels.NotificationChannels

class FakeNotificationChannels(
    var channelForIncomingCall: (ring: Boolean) -> String = { _ -> "" },
    var channelIdForMessage: (noisy: Boolean) -> String = { _ -> "" },
    var channelIdForTest: () -> String = { "" },
    var getOrCreateChannelForRoomResult: (RoomId, String) -> String = { roomId, _ -> "ROOM_CHANNEL_${roomId.value}" },
    var deleteChannelForRoomResult: (RoomId) -> Boolean = { _ -> true },
    var hasChannelForRoomResult: (RoomId) -> Boolean = { _ -> false },
    var getChannelIdForRoomResult: (RoomId) -> String? = { _ -> null },
) : NotificationChannels {
    override fun getChannelForIncomingCall(ring: Boolean): String {
        return channelForIncomingCall(ring)
    }

    override fun getChannelIdForMessage(noisy: Boolean): String {
        return channelIdForMessage(noisy)
    }

    override fun getChannelIdForTest(): String {
        return channelIdForTest()
    }

    override fun getOrCreateChannelForRoom(roomId: RoomId, roomDisplayName: String): String {
        return getOrCreateChannelForRoomResult(roomId, roomDisplayName)
    }

    override fun deleteChannelForRoom(roomId: RoomId): Boolean {
        return deleteChannelForRoomResult(roomId)
    }

    override fun hasChannelForRoom(roomId: RoomId): Boolean {
        return hasChannelForRoomResult(roomId)
    }

    override fun getChannelIdForRoom(roomId: RoomId): String? {
        return getChannelIdForRoomResult(roomId)
    }
}
