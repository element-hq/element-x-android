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

package io.element.android.libraries.permissions.api

import android.Manifest
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class PermissionsStateProvider : PreviewParameterProvider<PermissionsState> {
    override val values: Sequence<PermissionsState>
        get() = sequenceOf(
            aPermissionsState(showDialog = true, permission = Manifest.permission.POST_NOTIFICATIONS),
            aPermissionsState(showDialog = true, permission = Manifest.permission.CAMERA),
            aPermissionsState(showDialog = true, permission = Manifest.permission.RECORD_AUDIO),
            aPermissionsState(showDialog = true, permission = Manifest.permission.INTERNET),
        )
}

fun aPermissionsState(
    showDialog: Boolean,
    permission: String = Manifest.permission.POST_NOTIFICATIONS,
    permissionGranted: Boolean = false,
) = PermissionsState(
    permission = permission,
    permissionGranted = permissionGranted,
    shouldShowRationale = false,
    showDialog = showDialog,
    permissionAlreadyAsked = false,
    permissionAlreadyDenied = false,
    eventSink = {}
)
