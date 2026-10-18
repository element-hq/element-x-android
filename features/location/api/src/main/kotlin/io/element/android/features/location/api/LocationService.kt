/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.api

import io.element.android.features.enterprise.api.remoteconfig.MapTilerConfig
import io.element.android.libraries.core.coroutine.mapState
import kotlinx.coroutines.flow.StateFlow

/**
 * Tells whether the location features can be used at all, which depends on a map provider being configured,
 * either by the homeserver or at build time.
 */
interface LocationService {
    /**
     * The [MapTilerConfig] to use to render the maps, or null if no map provider is configured.
     * The configuration provided by the homeserver takes precedence over the one defined at build time.
     */
    val mapTilerConfig: StateFlow<MapTilerConfig?>

    /** Whether location sharing and map rendering are available; `false` disables the whole feature in the UI. */
    fun isServiceAvailable(): StateFlow<Boolean> = mapTilerConfig.mapState { it != null }
}
