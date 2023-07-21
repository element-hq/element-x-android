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

class ShowLocationStateProvider : PreviewParameterProvider<ShowLocationState> {
    override val values: Sequence<ShowLocationState>
        get() = sequenceOf(
            ShowLocationState(
                Location(1.23, 2.34, 4f),
                description = null,
                hasLocationPermission = false,
                isTrackMyLocation = false,
                eventSink = {},
            ),
            ShowLocationState(
                Location(1.23, 2.34, 4f),
                description = null,
                hasLocationPermission = true,
                isTrackMyLocation = false,
                eventSink = {},
            ),
            ShowLocationState(
                Location(1.23, 2.34, 4f),
                description = null,
                hasLocationPermission = true,
                isTrackMyLocation = true,
                eventSink = {},
            ),
            ShowLocationState(
                Location(1.23, 2.34, 4f),
                description = "My favourite place!",
                hasLocationPermission = false,
                isTrackMyLocation = false,
                eventSink = {},
            ),
            ShowLocationState(
                Location(1.23, 2.34, 4f),
                description = "For some reason I decided to to write a small essay that wraps at just two lines!",
                hasLocationPermission = false,
                isTrackMyLocation = false,
                eventSink = {},
            ),
            ShowLocationState(
                Location(1.23, 2.34, 4f),
                description = "For some reason I decided to write a small essay in the location description. " +
                    "It is so long that it will wrap onto more than two lines!",
                hasLocationPermission = false,
                isTrackMyLocation = false,
                eventSink = {},
            ),
        )
}
