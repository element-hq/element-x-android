/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.localnetwork

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.libraries.permissions.api.PermissionsEvent
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.permissions.api.localnetwork.LocalNetworkPermissionAdvisor
import io.element.android.libraries.permissions.api.localnetwork.LocalNetworkPermissionDialog
import kotlinx.coroutines.launch

@Inject
class LocalNetworkPermissionGate(
    private val advisor: LocalNetworkPermissionAdvisor,
    permissionsPresenterFactory: PermissionsPresenter.Factory,
) {
    private val permissionsPresenter: PermissionsPresenter =
        permissionsPresenterFactory.create(Manifest.permission.ACCESS_LOCAL_NETWORK)

    @Composable
    fun <T : Any> present(
        urlOf: (T) -> String,
        onProceed: suspend (T) -> Unit,
    ): LocalNetworkPermissionGateState<T> {
        val coroutineScope = rememberCoroutineScope()
        val permissionsState = permissionsPresenter.present()
        var pendingSubmit by remember { mutableStateOf<T?>(null) }

        val latestUrlOf by rememberUpdatedState(urlOf)
        val latestOnProceed by rememberUpdatedState(onProceed)

        LaunchedEffect(permissionsState.permissionGranted, pendingSubmit) {
            val pending = pendingSubmit
            if (pending != null && permissionsState.permissionGranted) {
                coroutineScope.launch { latestOnProceed(pending) }
                pendingSubmit = null
            }
        }

        val dialog by rememberUpdatedState(
            when {
                pendingSubmit == null -> LocalNetworkPermissionDialog.None
                permissionsState.permissionGranted -> LocalNetworkPermissionDialog.None
                permissionsState.shouldShowRationale || !permissionsState.permissionAlreadyAsked -> LocalNetworkPermissionDialog.Rationale
                else -> LocalNetworkPermissionDialog.Settings
            }
        )

        fun submit(value: T) {
            coroutineScope.launch {
                if (advisor.shouldRequestPermissionFor(latestUrlOf(value))) {
                    pendingSubmit = value
                } else {
                    latestOnProceed(value)
                }
            }
        }

        fun requestPermission() {
            when (dialog) {
                LocalNetworkPermissionDialog.Settings -> {
                    permissionsState.eventSink(PermissionsEvent.OpenSystemSettingAndCloseDialog)
                }
                LocalNetworkPermissionDialog.Rationale -> {
                    permissionsState.eventSink(PermissionsEvent.ForceRequestPermissions)
                }
                else -> Unit
            }
        }

        fun abort() {
            pendingSubmit = null
        }

        return LocalNetworkPermissionGateState(
            dialog = dialog,
            submit = ::submit,
            requestPermission = ::requestPermission,
            abort = ::abort,
        )
    }
}
