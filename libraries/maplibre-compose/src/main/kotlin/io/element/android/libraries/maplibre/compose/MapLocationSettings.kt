/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 * Copyright 2021 Google LLC
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.maplibre.compose

import androidx.compose.ui.graphics.Color

internal val DefaultMapLocationSettings = MapLocationSettings()

/**
 * Data class for UI-related settings on the map.
 *
 * Note: Should not be a data class if in need of maintaining binary compatibility
 * on future changes. See: https://jakewharton.com/public-api-challenges-in-kotlin/
 */
public data class MapLocationSettings(
    public val locationEnabled: Boolean = false,
    public val backgroundTintColor: Color = Color.Unspecified,
    public val foregroundTintColor: Color = Color.Unspecified,
    public val backgroundStaleTintColor: Color = Color.Unspecified,
    public val foregroundStaleTintColor: Color = Color.Unspecified,
    public val accuracyColor: Color = Color.Unspecified,
    public val pulseEnabled: Boolean = false,
    public val pulseColor: Color = Color.Unspecified
)
