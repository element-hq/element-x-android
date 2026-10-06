/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav

import androidx.annotation.VisibleForTesting
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.coroutine.childScope
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import io.element.android.libraries.push.api.notifications.NotificationCleaner
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AssistedInject
class LoggedInEventProcessor(
    @Assisted private val snackbarDispatcher: SnackbarDispatcher,
    private val roomMembershipObserver: RoomMembershipObserver,
    private val sessionId: SessionId,
    private val roomListService: RoomListService,
    private val notificationCleaner: NotificationCleaner,
    private val dispatchers: CoroutineDispatchers
) {
    @AssistedFactory
    interface Factory {
        fun create(snackbarDispatcher: SnackbarDispatcher): LoggedInEventProcessor
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var currentChildScope: CoroutineScope? = null

    fun observeEvents(coroutineScope: CoroutineScope) {
        if (currentChildScope != null) return

        val childScope = coroutineScope.childScope(dispatchers.computation, "LoggedInEventProcessor")
            .also { currentChildScope = it }

        roomMembershipObserver.updates
            .filter { !it.isUserInRoom }
            .distinctUntilChanged()
            .onEach { roomMemberShipUpdate ->
                when (roomMemberShipUpdate.change) {
                    MembershipChange.LEFT -> {
                        displayMessage(
                            if (roomMemberShipUpdate.isSpace) {
                                CommonStrings.common_current_user_left_space
                            } else {
                                CommonStrings.common_current_user_left_room
                            }
                        )
                    }
                    MembershipChange.INVITATION_REJECTED -> displayMessage(CommonStrings.common_current_user_rejected_invite)
                    MembershipChange.KNOCK_RETRACTED -> displayMessage(CommonStrings.common_current_user_canceled_knock)
                    else -> Unit
                }
            }
            .launchIn(childScope)

        // Use the room list service state as a 'heartbeat' update to check existing notifications
        roomListService.state
            .filter { it == RoomListService.State.Running }
            .onEach {
                notificationCleaner.clearReadRoomsNotifications(sessionId)
            }
            .launchIn(childScope)
    }

    fun stopObserving() {
        currentChildScope?.cancel()
        currentChildScope = null
    }

    private fun displayMessage(message: Int) {
        snackbarDispatcher.post(SnackbarMessage(message))
    }
}
