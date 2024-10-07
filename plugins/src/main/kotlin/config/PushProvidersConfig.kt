/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
