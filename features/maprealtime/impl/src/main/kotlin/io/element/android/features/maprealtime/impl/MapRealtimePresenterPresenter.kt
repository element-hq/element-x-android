/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.maprealtime.impl

import androidx.compose.runtime.Composable
import io.element.android.libraries.architecture.Presenter
import javax.inject.Inject

class MapRealtimePresenterPresenter @Inject constructor() : Presenter<MapRealtimePresenterState> {

    @Composable
    override fun present(): MapRealtimePresenterState {

        fun handleEvents(event: MapRealtimeEvents) {
            when (event) {
                MapRealtimeEvents.CloseMapTypeDialog -> TODO()
                MapRealtimeEvents.DismissDialog -> TODO()
                is MapRealtimeEvents.MapLongPress -> TODO()
                is MapRealtimeEvents.MapTypeSelected -> TODO()
                MapRealtimeEvents.OpenAppSettings -> TODO()
                MapRealtimeEvents.OpenMapTypeDialog -> TODO()
                MapRealtimeEvents.RequestPermissions -> TODO()
            }
        }

        return MapRealtimePresenterState(
            eventSink = ::handleEvents,
            permissionDialog = TODO(),
            hasLocationPermission = TODO(),
            showMapTypeDialog = TODO(),
            appName = TODO(),
            roomName = TODO(),
            isSharingLocation = TODO(),
            mapType = TODO()
        )
    }
}
