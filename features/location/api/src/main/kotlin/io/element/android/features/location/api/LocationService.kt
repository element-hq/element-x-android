/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.api

/**
 * Tells whether the location features can be used at all, which depends on the build having a map provider configured.
 */
interface LocationService {
    /** Whether location sharing and map rendering are available; `false` disables the whole feature in the UI. */
    fun isServiceAvailable(): Boolean
}
