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
