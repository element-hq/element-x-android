/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.api

// TODO Use this interface to check if the feature is available in other modules
// instead of accessing directly RageshakeConfig.isEnabled
fun interface RageshakeFeatureAvailability {
    fun isAvailable(): Boolean
}
