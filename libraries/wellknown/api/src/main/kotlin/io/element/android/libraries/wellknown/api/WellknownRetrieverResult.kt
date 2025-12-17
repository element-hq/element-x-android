/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.api

sealed interface WellknownRetrieverResult<out T> {
    /**
     * Well-known data has been successfully retrieved.
     */
    data class Success<out T>(val data: T) : WellknownRetrieverResult<T>

    /**
     * Well-known data is not found (file does not exist server side, we got a 404).
     */
    data object NotFound : WellknownRetrieverResult<Nothing>

    /**
     * Any other error.
     */
    data class Error(val exception: Exception) : WellknownRetrieverResult<Nothing>

    fun dataOrNull(): T? = when (this) {
        is Success<T> -> data
        is Error -> null
        NotFound -> null
    }
}
