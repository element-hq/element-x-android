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

package io.element.android.libraries.permissions.impl

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import com.squareup.anvil.annotations.ContributesBinding
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.permissions.api.PermissionsEvents
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.permissions.api.PermissionsState
import io.element.android.libraries.permissions.api.PermissionsStore
import io.element.android.libraries.permissions.impl.action.PermissionActions
import kotlinx.coroutines.launch
import timber.log.Timber

private val loggerTag = LoggerTag("DefaultPermissionsPresenter")

class DefaultPermissionsPresenter @AssistedInject constructor(
    @Assisted val permission: String,
    private val permissionsStore: PermissionsStore,
    private val composablePermissionStateProvider: ComposablePermissionStateProvider,
    private val permissionActions: PermissionActions,
) : PermissionsPresenter {
    @AssistedFactory
    @ContributesBinding(AppScope::class)
    interface Factory : PermissionsPresenter.Factory {
        override fun create(permission: String): DefaultPermissionsPresenter
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @SuppressLint("InlinedApi")
    @Composable
    override fun present(): PermissionsState {
        val localCoroutineScope = rememberCoroutineScope()

        // To reset the store: ResetStore()

        val isAlreadyDenied: Boolean by permissionsStore
            .isPermissionDenied(permission)
            .collectAsState(initial = false)

        val isAlreadyAsked: Boolean by permissionsStore
            .isPermissionAsked(permission)
            .collectAsState(initial = false)

        var permissionState: PermissionState? = null

        fun onPermissionResult(result: Boolean) {
            Timber.tag(loggerTag.value).d("onPermissionResult: $result")
            localCoroutineScope.launch {
                permissionsStore.setPermissionAsked(permission, true)
            }

            if (!result) {
                // Should show rational true -> denied.
                if (permissionState?.status?.shouldShowRationale == true) {
                    Timber.tag(loggerTag.value).d("onPermissionResult: setPermissionDenied to true")
                    localCoroutineScope.launch {
                        permissionsStore.setPermissionDenied(permission, true)
                    }
                }
            }
        }

        permissionState = composablePermissionStateProvider.provide(
            permission = permission,
            onPermissionResult = ::onPermissionResult
        )

        LaunchedEffect(this) {
            if (permissionState.status.isGranted) {
                // User may have granted permission from the settings, so reset the store regarding this permission
                permissionsStore.resetPermission(permission)
            }
        }

        val showDialog = rememberSaveable { mutableStateOf(false) }

        fun handleEvents(event: PermissionsEvents) {
            when (event) {
                PermissionsEvents.CloseDialog -> {
                    showDialog.value = false
                }
                PermissionsEvents.RequestPermissions -> {
                    if (permissionState.status !is PermissionStatus.Granted && isAlreadyDenied) {
                        showDialog.value = true
                    } else {
                        permissionState.launchPermissionRequest()
                    }
                }
                PermissionsEvents.OpenSystemSettingAndCloseDialog -> {
                    permissionActions.openSettings()
                    showDialog.value = false
                }
            }
        }

        return PermissionsState(
            permission = permissionState.permission,
            permissionGranted = permissionState.status.isGranted,
            shouldShowRationale = permissionState.status.shouldShowRationale,
            showDialog = showDialog.value,
            permissionAlreadyAsked = isAlreadyAsked,
            permissionAlreadyDenied = isAlreadyDenied,
            eventSink = { handleEvents(it) }
        )
    }

    /*
    @Composable
    private fun ResetStore() {
        LaunchedEffect(this@DefaultPermissionsPresenter) {
            launch {
                permissionsStore.resetStore()
            }
        }
    }
     */
}
