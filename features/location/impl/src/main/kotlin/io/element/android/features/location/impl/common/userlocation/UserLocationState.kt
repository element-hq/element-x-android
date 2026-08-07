/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.userlocation

import androidx.compose.runtime.Composable
import org.maplibre.compose.location.Location

data class UserLocationState(val location: Location?) {
    fun interface Factory {
        @Composable
        fun create(hasLocationPermission: Boolean): UserLocationState
    }
}
