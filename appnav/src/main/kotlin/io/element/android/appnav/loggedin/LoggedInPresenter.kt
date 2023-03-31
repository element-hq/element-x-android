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
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.permissions.noop.NoopPermissionsPresenter
import io.element.android.libraries.push.api.PushService
import javax.inject.Inject

class LoggedInPresenter @Inject constructor(
    private val matrixClient: MatrixClient,
    private val permissionsPresenterFactory: PermissionsPresenter.Factory,
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
            pushService.registerFirebasePusher(matrixClient)
        }

        val permissionsState = postNotificationPermissionsPresenter.present()

        fun handleEvents(event: LoggedInEvents) {
            when (event) {
                LoggedInEvents.MyEvent -> Unit
            }
        }

        return LoggedInState(
            permissionsState = permissionsState,
            eventSink = ::handleEvents
        )
    }
}
