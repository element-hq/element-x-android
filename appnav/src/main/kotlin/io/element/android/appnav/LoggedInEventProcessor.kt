/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.appnav

import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoggedInEventProcessor @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    roomMembershipObserver: RoomMembershipObserver,
) {
    private var observingJob: Job? = null

    private val displayLeftRoomMessage = roomMembershipObserver.updates
        .map { !it.isUserInRoom }

    fun observeEvents(coroutineScope: CoroutineScope) {
        observingJob = coroutineScope.launch {
            displayLeftRoomMessage
                .filter { it }
                .onEach {
                    displayMessage(CommonStrings.common_current_user_left_room)
                }
                .launchIn(this)
        }
    }

    fun stopObserving() {
        observingJob?.cancel()
        observingJob = null
    }

    private suspend fun displayMessage(message: Int) {
        snackbarDispatcher.post(SnackbarMessage(message))
    }
}
