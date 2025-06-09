/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
import io.element.android.libraries.di.annotations.AppCoroutineScope
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
    @AppCoroutineScope
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
