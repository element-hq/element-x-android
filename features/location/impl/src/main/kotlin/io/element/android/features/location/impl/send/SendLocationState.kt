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

data class SendLocationState(
    val permissionDialog: Dialog,
    val mode: Mode,
    val hasLocationPermission: Boolean,
    val appName: String,
    val eventSink: (SendLocationEvents) -> Unit,
) {
    sealed interface Mode {
        data object SenderLocation : Mode
        data object PinLocation : Mode
    }

    sealed interface Dialog {
        data object None : Dialog
        data object PermissionRationale : Dialog
        data object PermissionDenied : Dialog
    }
}
