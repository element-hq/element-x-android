/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.api.remoteconfig

/**
 * A provider of a runtime-fetched MapTiler configuration, which can be used to override the default MapTiler configuration.
 */
fun interface CustomMapTilerConfigProvider {
    suspend fun get(): Result<MapTilerConfig?>
}
