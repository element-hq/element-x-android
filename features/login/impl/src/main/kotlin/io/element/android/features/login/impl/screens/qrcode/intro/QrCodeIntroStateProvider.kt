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
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.permissions.api.PermissionsState
import io.element.android.libraries.permissions.api.aPermissionsState

open class QrCodeIntroStateProvider : PreviewParameterProvider<QrCodeIntroState> {
    override val values: Sequence<QrCodeIntroState>
        get() = sequenceOf(
            aQrCodeIntroState(),
            // Add other state here
        )
}

fun aQrCodeIntroState(
    appName: String = "Element X",
    cameraPermissionState: PermissionsState = aPermissionsState(
        showDialog = false,
        permission = Manifest.permission.CAMERA,
    ),
    canContinue: Boolean = false,
    eventSink: (QrCodeIntroEvents) -> Unit = {},
) = QrCodeIntroState(
    appName = appName,
    cameraPermissionState = cameraPermissionState,
    canContinue = canContinue,
    eventSink = eventSink
)
