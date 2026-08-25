/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.impl

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.features.share.api.DirectShareShortcutsObserver
import io.element.android.features.share.api.DirectShareShortcutsPublisher
import io.element.android.features.share.api.SharingRoomInfo
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private const val MAX_DIRECT_SHARE_SHORTCUTS = 5

@SingleIn(SessionScope::class)
@ContributesBinding(SessionScope::class)
class DefaultDirectShareShortcutsObserver(
    private val client: MatrixClient,
    private val directShareShortcutsPublisher: DirectShareShortcutsPublisher,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
) : DirectShareShortcutsObserver {
    /**
     * A room list dedicated to the direct share shortcuts, so that the filters applied
     * to the room list UI do not change which rooms are published as share targets.
     */
    private val roomList by lazy {
        client.roomListService.createRoomList(
            pageSize = MAX_DIRECT_SHARE_SHORTCUTS,
            source = RoomList.Source.All,
            coroutineScope = sessionCoroutineScope,
        )
    }

    private var job: Job? = null

    override fun start() {
        if (job?.isActive == true) return
        job = sessionCoroutineScope.launch {
            roomList.updateFilter(
                RoomListFilter.any(RoomListFilter.Category.Group, RoomListFilter.Category.People)
            )
            roomList.summaries
                .map { summaries -> summaries.take(MAX_DIRECT_SHARE_SHORTCUTS).map { it.toSharingRoomInfo() } }
                .distinctUntilChanged()
                .collectLatest { directShareShortcutsPublisher.publishShortcutsForRooms(it) }
        }
    }

    private fun RoomSummary.toSharingRoomInfo() = SharingRoomInfo(
        sessionId = client.sessionId,
        roomId = roomId,
        displayName = info.name ?: roomId.value,
        avatarUrl = info.avatarUrl ?: if (isDm) {
            info.heroes.firstOrNull { it.userId != client.sessionId }?.avatarUrl
        } else {
            null
        },
    )
}
