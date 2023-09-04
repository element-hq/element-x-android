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

package io.element.android.features.ftue.impl.notitications

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.permissions.api.PermissionStateProvider
import io.element.android.libraries.permissions.api.PermissionsEvents
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.permissions.api.PermissionsStore
import io.element.android.libraries.permissions.noop.NoopPermissionsPresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class NotificationsOptInPresenter @AssistedInject constructor(
    private val permissionsPresenterFactory: PermissionsPresenter.Factory,
    @Assisted private val callback: NotificationsOptInNode.Callback,
    private val appCoroutineScope: CoroutineScope,
    private val permissionStateProvider: PermissionStateProvider,
) : Presenter<NotificationsOptInState> {

    @AssistedFactory
    interface Factory {
        fun create(callback: NotificationsOptInNode.Callback): NotificationsOptInPresenter
    }

    private val postNotificationPermissionsPresenter by lazy {
        // Ask for POST_NOTIFICATION PERMISSION on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsPresenterFactory.create(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            NoopPermissionsPresenter()
        }
    }

    @Composable
    override fun present(): NotificationsOptInState {
        val notificationPremissionsState = postNotificationPermissionsPresenter.present()

        fun handleEvents(event: NotificationsOptInEvents) {
            when (event) {
                NotificationsOptInEvents.ContinueClicked -> {
                    if (notificationPremissionsState.permissionGranted) {
                        callback.onNotificationsOptInFinished()
                    } else {
                        notificationPremissionsState.eventSink(PermissionsEvents.OpenSystemDialog)
                    }
                }
                NotificationsOptInEvents.NotNowClicked -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        appCoroutineScope.setPermissionDenied()
                    }
                    callback.onNotificationsOptInFinished()
                }
            }
        }

        return NotificationsOptInState(
            notificationsPermissionState = notificationPremissionsState,
            eventSink = ::handleEvents
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun CoroutineScope.setPermissionDenied() = launch {
        permissionStateProvider.setPermissionDenied(Manifest.permission.POST_NOTIFICATIONS, true)
    }
}
