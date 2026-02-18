/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.share

import io.element.android.features.location.api.Location

sealed interface ShareLocationEvents {
    data class ShareLocation(
        val cameraPosition: CameraPosition,
        val location: Location?,
    ) : ShareLocationEvents {
        data class CameraPosition(
            val lat: Double,
            val lon: Double,
            val zoom: Double,
        )
    }

    data object SwitchToMyLocationMode : ShareLocationEvents
    data object SwitchToPinLocationMode : ShareLocationEvents
    data object DismissDialog : ShareLocationEvents
    data object RequestPermissions : ShareLocationEvents
    data object OpenAppSettings : ShareLocationEvents
}
