/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.location

import androidx.compose.runtime.Stable
import org.maplibre.compose.location.LocationProvider

@Stable
interface DeviceLocationProvider : LocationProvider {
    fun onPermissionStatusRefreshed()
}
