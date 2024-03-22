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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.push.api.PushService
import javax.inject.Inject

class LoggedInPresenter @Inject constructor(
    private val matrixClient: MatrixClient,
    private val networkMonitor: NetworkMonitor,
    private val pushService: PushService,
    private val sessionVerificationService: SessionVerificationService,
) : Presenter<LoggedInState> {
    @Composable
    override fun present(): LoggedInState {
        val verifiedStatus by sessionVerificationService.sessionVerifiedStatus.collectAsState()
        LaunchedEffect(verifiedStatus) {
            if (verifiedStatus == SessionVerifiedStatus.Verified) {
                // Ensure pusher is registered
                // TODO Manually select push provider for now
                val pushProvider = pushService.getAvailablePushProviders().firstOrNull() ?: return@LaunchedEffect
                val distributor = pushProvider.getDistributors().firstOrNull() ?: return@LaunchedEffect
                pushService.registerWith(matrixClient, pushProvider, distributor)
            }
        }

        val syncIndicator by matrixClient.roomListService.syncIndicator.collectAsState()
        val networkStatus by networkMonitor.connectivity.collectAsState()
        val showSyncSpinner by remember {
            derivedStateOf {
                networkStatus == NetworkStatus.Online && syncIndicator == RoomListService.SyncIndicator.Show
            }
        }
        return LoggedInState(
            showSyncSpinner = showSyncSpinner,
        )
    }
}
