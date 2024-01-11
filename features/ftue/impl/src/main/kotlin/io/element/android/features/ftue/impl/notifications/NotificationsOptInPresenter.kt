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

package io.element.android.features.ftue.impl.notifications

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.permissions.api.PermissionStateProvider
import io.element.android.libraries.permissions.api.PermissionsEvents
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.permissions.noop.NoopPermissionsPresenter
import io.element.android.services.toolbox.api.sdk.BuildVersionSdkIntProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class NotificationsOptInPresenter @AssistedInject constructor(
    permissionsPresenterFactory: PermissionsPresenter.Factory,
    @Assisted private val callback: NotificationsOptInNode.Callback,
    private val appCoroutineScope: CoroutineScope,
    private val permissionStateProvider: PermissionStateProvider,
    private val buildVersionSdkIntProvider: BuildVersionSdkIntProvider,
) : Presenter<NotificationsOptInState> {
    @AssistedFactory
    interface Factory {
        fun create(callback: NotificationsOptInNode.Callback): NotificationsOptInPresenter
    }

    private val postNotificationPermissionsPresenter: PermissionsPresenter =
        // Ask for POST_NOTIFICATION PERMISSION on Android 13+
        if (buildVersionSdkIntProvider.isAtLeast(Build.VERSION_CODES.TIRAMISU)) {
            permissionsPresenterFactory.create(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            NoopPermissionsPresenter()
        }

    @Composable
    override fun present(): NotificationsOptInState {
        val notificationsPermissionsState = postNotificationPermissionsPresenter.present()

        fun handleEvents(event: NotificationsOptInEvents) {
            when (event) {
                NotificationsOptInEvents.ContinueClicked -> {
                    if (notificationsPermissionsState.permissionGranted) {
                        callback.onNotificationsOptInFinished()
                    } else {
                        notificationsPermissionsState.eventSink(PermissionsEvents.RequestPermissions)
                    }
                }
                NotificationsOptInEvents.NotNowClicked -> {
                    if (buildVersionSdkIntProvider.isAtLeast(Build.VERSION_CODES.TIRAMISU)) {
                        appCoroutineScope.setPermissionDenied()
                    }
                    callback.onNotificationsOptInFinished()
                }
            }
        }

        LaunchedEffect(notificationsPermissionsState) {
            if (notificationsPermissionsState.permissionGranted ||
                notificationsPermissionsState.permissionAlreadyDenied) {
                callback.onNotificationsOptInFinished()
            }
        }

        return NotificationsOptInState(
            notificationsPermissionState = notificationsPermissionsState,
            eventSink = ::handleEvents
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun CoroutineScope.setPermissionDenied() = launch {
        permissionStateProvider.setPermissionDenied(Manifest.permission.POST_NOTIFICATIONS, true)
    }
}
