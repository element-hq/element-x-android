/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.recentcalls.impl

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.features.recentcalls.api.RecentCallDirection
import io.element.android.features.recentcalls.api.RecentCallEntry
import io.element.android.features.recentcalls.api.RecentCallStatus
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomInfo

@SingleIn(SessionScope::class)
@Inject
class RecentCallsNormalizer {
    fun normalize(
        events: List<RawCallEvent>,
        currentUserId: UserId,
    ): List<RecentCallEntry> {
        return events
            .sortedByDescending { it.timestamp }
            .groupBy { it.roomId }
            .flatMap { (_, roomEvents) -> groupRoomEvents(roomEvents, currentUserId) }
            .sortedByDescending { it.timestamp }
    }

    private fun groupRoomEvents(
        events: List<RawCallEvent>,
        currentUserId: UserId,
    ): List<RecentCallEntry> {
        val sorted = events.sortedByDescending { it.timestamp }
        val groups = mutableListOf<MutableList<RawCallEvent>>()
        for (event in sorted) {
            val lastGroup = groups.lastOrNull()
            if (lastGroup != null &&
                lastGroup.first().timestamp - event.timestamp < GROUP_WINDOW_MS &&
                lastGroup.first().senderId == event.senderId
            ) {
                lastGroup.add(event)
            } else {
                groups.add(mutableListOf(event))
            }
        }
        return groups.map { group -> toEntry(group.first(), currentUserId) }
    }

    private fun toEntry(
        event: RawCallEvent,
        currentUserId: UserId,
    ): RecentCallEntry {
        val roomInfo = event.roomInfo
        val direction = if (event.isOwn) RecentCallDirection.OUTGOING else RecentCallDirection.INCOMING
        val status = when {
            event.declinedBy.contains(currentUserId) -> RecentCallStatus.DECLINED
            event.declinedBy.isNotEmpty() && direction == RecentCallDirection.INCOMING -> RecentCallStatus.MISSED
            else -> RecentCallStatus.COMPLETED
        }
        val (displayName, counterpartUserId) = roomDisplayInfo(roomInfo, event.senderId, event.isOwn)
        return RecentCallEntry(
            id = "${event.roomId.value}_${event.eventId.value}",
            eventId = event.eventId,
            roomId = event.roomId,
            roomDisplayName = displayName,
            avatarUrl = roomInfo.avatarUrl,
            isDirect = roomInfo.isDm,
            counterpartUserId = counterpartUserId,
            direction = direction,
            status = status,
            callIntent = event.callIntent,
            timestamp = event.timestamp,
            durationMs = null,
        )
    }

    private fun roomDisplayInfo(
        roomInfo: RoomInfo,
        senderId: UserId,
        isOwn: Boolean,
    ): Pair<String, UserId?> {
        val name = roomInfo.name?.takeIf { it.isNotBlank() }
            ?: roomInfo.rawName?.takeIf { it.isNotBlank() }
            ?: roomInfo.id.value
        if (!roomInfo.isDm) {
            return name to null
        }
        val hero = roomInfo.heroes.firstOrNull()
        val counterpart = if (isOwn) hero?.userId else senderId
        val displayName = hero?.displayName?.takeIf { it.isNotBlank() }
            ?: counterpart?.value
            ?: name
        return displayName to counterpart
    }

    companion object {
        private const val GROUP_WINDOW_MS = 5 * 60 * 1000L
    }
}
