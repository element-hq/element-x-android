/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.share

import io.element.android.features.location.api.Location

sealed interface ShareLocationEvent {
    data class ShareStaticLocation(
        val cameraPosition: CameraPosition,
        val location: Location?,
    ) : ShareLocationEvent {
        data class CameraPosition(
            val lat: Double,
            val lon: Double,
            val zoom: Double,
        )
    }

    data object SelectLiveLocationDuration: ShareLocationEvent

    data object SwitchToMyLocationMode : ShareLocationEvent
    data object SwitchToPinLocationMode : ShareLocationEvent
    data object DismissDialog : ShareLocationEvent
    data object RequestPermissions : ShareLocationEvent
    data object OpenAppSettings : ShareLocationEvent
}
