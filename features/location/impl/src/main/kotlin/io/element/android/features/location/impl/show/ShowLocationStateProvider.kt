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

package io.element.android.features.location.impl.show

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.location.api.Location

private const val APP_NAME = "ApplicationName"

class ShowLocationStateProvider : PreviewParameterProvider<ShowLocationState> {
    override val values: Sequence<ShowLocationState>
        get() = sequenceOf(
            aShowLocationState(),
            aShowLocationState(
                permissionDialog = ShowLocationState.Dialog.PermissionDenied,
            ),
            aShowLocationState(
                permissionDialog = ShowLocationState.Dialog.PermissionRationale,
            ),
            aShowLocationState(
                hasLocationPermission = true,
            ),
            aShowLocationState(
                hasLocationPermission = true,
                isTrackMyLocation = true,
            ),
            aShowLocationState(
                description = "My favourite place!",
            ),
            aShowLocationState(
                description = "For some reason I decided to to write a small essay that wraps at just two lines!",
            ),
            aShowLocationState(
                description = "For some reason I decided to write a small essay in the location description. " +
                    "It is so long that it will wrap onto more than two lines!",
            ),
        )
}

fun aShowLocationState(
    permissionDialog: ShowLocationState.Dialog = ShowLocationState.Dialog.None,
    location: Location = Location(1.23, 2.34, 4f),
    description: String? = null,
    hasLocationPermission: Boolean = false,
    isTrackMyLocation: Boolean = false,
    appName: String = APP_NAME,
    eventSink: (ShowLocationEvents) -> Unit = {},
) = ShowLocationState(
    permissionDialog = permissionDialog,
    location = location,
    description = description,
    hasLocationPermission = hasLocationPermission,
    isTrackMyLocation = isTrackMyLocation,
    appName = appName,
    eventSink = eventSink,
)
