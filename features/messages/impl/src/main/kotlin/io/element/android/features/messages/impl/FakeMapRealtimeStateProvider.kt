/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.maprealtime.impl.MapRealtimePresenterState
import io.element.android.features.maprealtime.impl.MapType

open class FakeMapRealtimeStateProvider : PreviewParameterProvider<MapRealtimePresenterState> {
    override val values: Sequence<MapRealtimePresenterState>
        get() = sequenceOf(
            MapRealtimePresenterState(
                eventSink = { },
                permissionDialog = MapRealtimePresenterState.Dialog.None,
                hasLocationPermission = true,
                hasGpsEnabled = true,
                showMapTypeDialog = false,
                appName = "Test",
                roomName = "TestRoom",
                isSharingLocation = true,
                mapType = MapType("OSM", "openstreetmap"),
                liveLocationShares = emptyList(),
                isWaitingForLocation = false,
            )
        )
}
