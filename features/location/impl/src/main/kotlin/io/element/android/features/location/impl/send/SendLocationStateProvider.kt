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

package io.element.android.features.location.impl.send

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

private const val APP_NAME = "ApplicationName"

class SendLocationStateProvider : PreviewParameterProvider<SendLocationState> {
    override val values: Sequence<SendLocationState>
        get() = sequenceOf(
            aSendLocationState(
                permissionDialog = SendLocationState.Dialog.None,
                mode = SendLocationState.Mode.PinLocation,
                hasLocationPermission = false,
            ),
            aSendLocationState(
                permissionDialog = SendLocationState.Dialog.PermissionDenied,
                mode = SendLocationState.Mode.PinLocation,
                hasLocationPermission = false,
            ),
            aSendLocationState(
                permissionDialog = SendLocationState.Dialog.PermissionRationale,
                mode = SendLocationState.Mode.PinLocation,
                hasLocationPermission = false,
            ),
            aSendLocationState(
                permissionDialog = SendLocationState.Dialog.None,
                mode = SendLocationState.Mode.PinLocation,
                hasLocationPermission = true,
            ),
            aSendLocationState(
                permissionDialog = SendLocationState.Dialog.None,
                mode = SendLocationState.Mode.SenderLocation,
                hasLocationPermission = true,
            ),
        )
}

private fun aSendLocationState(
    permissionDialog: SendLocationState.Dialog,
    mode: SendLocationState.Mode,
    hasLocationPermission: Boolean,
): SendLocationState {
    return SendLocationState(
        permissionDialog = permissionDialog,
        mode = mode,
        hasLocationPermission = hasLocationPermission,
        appName = APP_NAME,
        eventSink = {}
    )
}
