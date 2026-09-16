/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.linknewdevice.impl.screens.desktop

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.permissions.api.PermissionsEvent
import io.element.android.libraries.permissions.api.PermissionsPresenter

@Inject
class DesktopNoticePresenter(
    permissionsPresenterFactory: PermissionsPresenter.Factory,
) : Presenter<DesktopNoticeState> {
    private val cameraPermissionPresenter: PermissionsPresenter = permissionsPresenterFactory.create(Manifest.permission.CAMERA)
    private var pendingPermissionRequest by mutableStateOf(false)

    @Composable
    override fun present(): DesktopNoticeState {
        val cameraPermissionState = cameraPermissionPresenter.present()
        var canContinue by remember { mutableStateOf(false) }
        LaunchedEffect(cameraPermissionState.permissionGranted) {
            if (cameraPermissionState.permissionGranted && pendingPermissionRequest) {
                pendingPermissionRequest = false
                canContinue = true
            }
        }

        fun handleEvent(event: DesktopNoticeEvent) {
            when (event) {
                DesktopNoticeEvent.Continue -> if (cameraPermissionState.permissionGranted) {
                    canContinue = true
                } else {
                    pendingPermissionRequest = true
                    cameraPermissionState.eventSink(PermissionsEvent.RequestPermissions)
                }
            }
        }

        return DesktopNoticeState(
            cameraPermissionState = cameraPermissionState,
            canContinue = canContinue,
            eventSink = ::handleEvent,
        )
    }
}
