/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.location

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.maplibre.compose.location.Location

class NoopDeviceLocationProvider(override val location: StateFlow<Location?> = MutableStateFlow(null)) : DeviceLocationProvider {
    override fun onPermissionStatusRefreshed() = Unit
}
