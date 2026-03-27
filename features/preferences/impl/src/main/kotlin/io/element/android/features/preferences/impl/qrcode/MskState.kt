/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.qrcode

sealed interface MskState {
    object Loading : MskState
    data class Loaded(val value: String?) : MskState

    /**
     * Determines if the state is loading.
     */
    val isLoading: Boolean
        get() = when (this) {
            Loading -> true
            is Loaded -> false
        }

    /**
     * The MSK content, or null if unloaded or unavailable.
     */
    val valueOrNull: String?
        get() = (this as? Loaded)?.value
}
