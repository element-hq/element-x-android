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

package io.element.android.appnav.loggedin

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.permissions.noop.NoopPermissionsPresenter
import io.element.android.libraries.push.api.PushService
import kotlinx.coroutines.delay
import javax.inject.Inject

private const val DELAY_BEFORE_SHOWING_SYNC_SPINNER_IN_MILLIS = 1500L

class LoggedInPresenter @Inject constructor(
    private val matrixClient: MatrixClient,
    private val permissionsPresenterFactory: PermissionsPresenter.Factory,
    private val networkMonitor: NetworkMonitor,
    private val pushService: PushService,
) : Presenter<LoggedInState> {

    private val postNotificationPermissionsPresenter by lazy {
        // Ask for POST_NOTIFICATION PERMISSION on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsPresenterFactory.create(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            NoopPermissionsPresenter()
        }
    }

    @Composable
    override fun present(): LoggedInState {
        LaunchedEffect(Unit) {
            // Ensure pusher is registered
            // TODO Manually select push provider for now
            val pushProvider = pushService.getAvailablePushProviders().firstOrNull() ?: return@LaunchedEffect
            val distributor = pushProvider.getDistributors().firstOrNull() ?: return@LaunchedEffect
            pushService.registerWith(matrixClient, pushProvider, distributor)
        }

        val roomListState by matrixClient.roomListService.state.collectAsState()
        val networkStatus by networkMonitor.connectivity.collectAsState()
        val permissionsState = postNotificationPermissionsPresenter.present()
        var showSyncSpinner by remember {
            mutableStateOf(false)
        }
        LaunchedEffect(roomListState, networkStatus) {
            showSyncSpinner = when {
                networkStatus == NetworkStatus.Offline -> false
                roomListState == RoomListService.State.Running -> false
                else -> {
                    delay(DELAY_BEFORE_SHOWING_SYNC_SPINNER_IN_MILLIS)
                    true
                }
            }
        }
        return LoggedInState(
            showSyncSpinner = showSyncSpinner,
            permissionsState = permissionsState,
        )
    }
}
