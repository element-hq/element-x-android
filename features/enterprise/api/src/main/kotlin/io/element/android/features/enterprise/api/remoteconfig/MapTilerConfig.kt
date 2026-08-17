/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.api.remoteconfig

import androidx.compose.runtime.Immutable

/**
 * A `MapTiler` configuration received from the server. This configuration is used to display maps in the app.
 */
@Immutable
data class MapTilerConfig(
    /** The API key to use with MapTiler. This is always not null, but can be empty/blank. */
    val apiKey: String,
    /** The style ID to use for light mode. This is optional and can be null. */
    val lightStyleId: String?,
    /** The style ID to use for light mode. This is optional and can be null. */
    val darkStyleId: String?,
    /** The base URL to use for MapTiler. This is optional and can be null. */
    val baseUrl: String?,
)
