/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.api

import kotlinx.coroutines.flow.Flow

/**
 * Tells whether bug reporting is available, which an enterprise deployment can turn off.
 */
fun interface RageshakeFeatureAvailability {
    /** Whether the rageshake and bug report entry points should be offered to the user. */
    fun isAvailable(): Flow<Boolean>
}
