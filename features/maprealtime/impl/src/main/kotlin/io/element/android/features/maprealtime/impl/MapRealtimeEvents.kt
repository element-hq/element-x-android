/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.maprealtime.impl

import org.maplibre.android.geometry.LatLng

sealed interface MapRealtimeEvents {
    data object DismissDialog : MapRealtimeEvents
    data object RequestPermissions : MapRealtimeEvents
    data object OpenAppSettings : MapRealtimeEvents
    data object OpenMapTypeDialog : MapRealtimeEvents
    data object CloseMapTypeDialog : MapRealtimeEvents
    data class MapLongPress(val coords: LatLng) : MapRealtimeEvents
    data class SendLongPressLocation(val coords: LatLng) : MapRealtimeEvents
    data class MapTypeSelected(val mapType: MapType) : MapRealtimeEvents
}
