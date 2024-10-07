/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.location.impl.show

sealed interface ShowLocationEvents {
    data object Share : ShowLocationEvents
    data class TrackMyLocation(val enabled: Boolean) : ShowLocationEvents
    data object DismissDialog : ShowLocationEvents
    data object RequestPermissions : ShowLocationEvents
    data object OpenAppSettings : ShowLocationEvents
}
