/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.qrcode.intro

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.permissions.api.PermissionsEvents
import io.element.android.libraries.permissions.api.PermissionsPresenter
import javax.inject.Inject

class QrCodeIntroPresenter @Inject constructor(
    private val buildMeta: BuildMeta,
    permissionsPresenterFactory: PermissionsPresenter.Factory,
) : Presenter<QrCodeIntroState> {
    private val cameraPermissionPresenter: PermissionsPresenter = permissionsPresenterFactory.create(Manifest.permission.CAMERA)
    private var pendingPermissionRequest by mutableStateOf(false)

    @Composable
    override fun present(): QrCodeIntroState {
        val cameraPermissionState = cameraPermissionPresenter.present()
        var canContinue by remember { mutableStateOf(false) }
        LaunchedEffect(cameraPermissionState.permissionGranted) {
            if (cameraPermissionState.permissionGranted && pendingPermissionRequest) {
                pendingPermissionRequest = false
                canContinue = true
            }
        }

        fun handleEvents(event: QrCodeIntroEvents) {
            when (event) {
                QrCodeIntroEvents.Continue -> if (cameraPermissionState.permissionGranted) {
                    canContinue = true
                } else {
                    pendingPermissionRequest = true
                    cameraPermissionState.eventSink(PermissionsEvents.RequestPermissions)
                }
            }
        }

        return QrCodeIntroState(
            appName = buildMeta.applicationName,
            desktopAppName = buildMeta.desktopApplicationName,
            cameraPermissionState = cameraPermissionState,
            canContinue = canContinue,
            eventSink = ::handleEvents
        )
    }
}
