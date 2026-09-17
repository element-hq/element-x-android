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

    /**
     * Presents a 'gate' that checks for local network permission before proceeding with the given action.
     * @param T the type of the value that is submitted to the gate. This value is passed to [onProceed] when the permission is granted.
     * @param urlOf a function that returns the homeserver URL or server name for the given value. This is used to determine if the permission is needed.
     * @param onProceed a suspend function that is called when the permission is granted and the action can proceed.
     * @param onDenyPermission a suspend function that is called when the permission is actively denied by the user.
     * This is optional and defaults to an empty function.
     * @return a [LocalNetworkPermissionGateState] that contains the current dialog state and functions to submit, request permission, or abort the action.
     */
    @Composable
    fun <T : Any> present(
        urlOf: (T) -> String,
        onProceed: suspend (T) -> Unit,
        onDenyPermission: suspend () -> Unit = {},
    ): LocalNetworkPermissionGateState<T> {
        val coroutineScope = rememberCoroutineScope()
        val permissionsState = permissionsPresenter.present()
        var pendingSubmit by remember { mutableStateOf<T?>(null) }
        // True while a submitted value is waiting for the outcome of a permission request.
        // This lets us tell an in-flow denial apart from a permission that was already denied before any submit,
        // so we don't report a denial (or block the screen) when the user hasn't even submitted anything yet.
        var awaitingPermissionOutcome by remember { mutableStateOf(false) }

        val latestUrlOf by rememberUpdatedState(urlOf)
        val latestOnProceed by rememberUpdatedState(onProceed)
        val latestOnDenyPermission by rememberUpdatedState(onDenyPermission)

        LaunchedEffect(permissionsState.permissionGranted, pendingSubmit) {
            val pending = pendingSubmit
            if (pending != null && permissionsState.permissionGranted) {
                awaitingPermissionOutcome = false
                pendingSubmit = null
                coroutineScope.launch { latestOnProceed(pending) }
            } else if (awaitingPermissionOutcome && pendingSubmit == null && permissionsState.permissionAlreadyDenied) {
                awaitingPermissionOutcome = false
                coroutineScope.launch { latestOnDenyPermission() }
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
                    awaitingPermissionOutcome = true
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
