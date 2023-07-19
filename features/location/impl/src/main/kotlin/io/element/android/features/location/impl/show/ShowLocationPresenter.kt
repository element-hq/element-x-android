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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.location.api.Location
import io.element.android.features.location.impl.MapDefaults
import io.element.android.features.location.impl.permissions.PermissionsPresenter
import io.element.android.features.location.impl.permissions.PermissionsState
import io.element.android.libraries.architecture.Presenter

class ShowLocationPresenter @AssistedInject constructor(
    permissionsPresenterFactory: PermissionsPresenter.Factory,
    private val actions: LocationActions,
    @Assisted private val location: Location,
    @Assisted private val description: String?
) : Presenter<ShowLocationState> {

    @AssistedFactory
    interface Factory {
        fun create(location: Location, description: String?): ShowLocationPresenter
    }

    private val permissionsPresenter = permissionsPresenterFactory.create(MapDefaults.permissions)

    @Composable
    override fun present(): ShowLocationState {
        val permissionsState: PermissionsState = permissionsPresenter.present()
        var isTrackMyLocation by remember { mutableStateOf(false) }

        fun handleEvents(event: ShowLocationEvents) {
            when (event) {
                ShowLocationEvents.Share -> actions.share(location, description)
                is ShowLocationEvents.TrackMyLocation -> isTrackMyLocation = event.enabled
            }
        }

        return ShowLocationState(
            location = location,
            description = description,
            hasLocationPermission = permissionsState.isAnyGranted,
            isTrackMyLocation = isTrackMyLocation,
            eventSink = ::handleEvents,
        )
    }
}
