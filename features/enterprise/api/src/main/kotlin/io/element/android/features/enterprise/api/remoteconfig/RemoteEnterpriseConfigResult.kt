/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.api.remoteconfig

sealed interface RemoteEnterpriseConfigResult<out T> {
    /**
     * Well-known data has been successfully retrieved.
     */
    data class Success<out T>(val data: T) : RemoteEnterpriseConfigResult<T>

    /**
     * Well-known data has been retrieved from the local cache but is outdated and a manual refresh has been launched.
     */
    data class Outdated<out T>(val data: T) : RemoteEnterpriseConfigResult<T>

    /**
     * Well-known data is not found (file does not exist server side, we got a 404).
     */
    data object NotFound : RemoteEnterpriseConfigResult<Nothing>

    /**
     * Any other error.
     */
    data class Error(val exception: Exception) : RemoteEnterpriseConfigResult<Nothing>

    fun dataOrNull(): T? = when (this) {
        is Success<T> -> data
        is Outdated<T> -> data
        is Error -> null
        NotFound -> null
    }

    fun upToDateDataOrNull(): T? = when (this) {
        is Success<T> -> data
        is Outdated<T> -> null
        is Error -> null
        NotFound -> null
    }
}
