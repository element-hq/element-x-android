/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.location.impl.send

import io.element.android.features.location.api.Location

sealed interface SendLocationEvents {
    data class SendLocation(
        val cameraPosition: CameraPosition,
        val location: Location?,
    ) : SendLocationEvents {
        data class CameraPosition(
            val lat: Double,
            val lon: Double,
            val zoom: Double,
        )
    }

    data object SwitchToMyLocationMode : SendLocationEvents
    data object SwitchToPinLocationMode : SendLocationEvents
    data object DismissDialog : SendLocationEvents
    data object RequestPermissions : SendLocationEvents
    data object OpenAppSettings : SendLocationEvents
}
