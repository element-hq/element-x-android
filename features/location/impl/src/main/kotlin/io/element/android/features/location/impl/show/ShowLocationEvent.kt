/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.show

import io.element.android.features.location.api.Location

sealed interface ShowLocationEvent {
    data class Share(val location: Location) : ShowLocationEvent
    data class TrackMyLocation(val enabled: Boolean) : ShowLocationEvent
    data object DismissDialog : ShowLocationEvent
    data object RequestPermissions : ShowLocationEvent
    data object OpenAppSettings : ShowLocationEvent
    data object OpenLocationSettings : ShowLocationEvent
}
