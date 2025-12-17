/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.show

sealed interface ShowLocationEvents {
    data object Share : ShowLocationEvents
    data class TrackMyLocation(val enabled: Boolean) : ShowLocationEvents
    data object DismissDialog : ShowLocationEvents
    data object RequestPermissions : ShowLocationEvents
    data object OpenAppSettings : ShowLocationEvents
}
