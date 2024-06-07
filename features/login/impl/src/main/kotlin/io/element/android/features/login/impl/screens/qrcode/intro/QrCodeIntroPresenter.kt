/*
 * Copyright (c) 2024 New Vector Ltd
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
