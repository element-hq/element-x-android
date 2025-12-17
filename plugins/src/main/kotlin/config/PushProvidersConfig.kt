/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package config

data class PushProvidersConfig(
    val includeFirebase: Boolean,
    val includeUnifiedPush: Boolean,
) {
    init {
        require(includeFirebase || includeUnifiedPush) {
            "At least one push provider must be included"
        }
    }
}
