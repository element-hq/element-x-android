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
     * Well-known data is not found (file does not exist server side, we got a 404). `needsRefresh` indicates whether a refresh must be done
     * to replace this value: if it's `true`, the caller must trigger a refresh to try to fetch the data again;
     * if it's `false`, the caller can assume that the data is not available and no refresh is needed before returning the data.
     */
    data class NotFound(val needsRefresh: Boolean) : RemoteEnterpriseConfigResult<Nothing>

    /**
     * Any other error.
     */
    data class Error(val exception: Exception) : RemoteEnterpriseConfigResult<Nothing>

    fun dataOrNull(): T? = when (this) {
        is Success<T> -> data
        is Outdated<T> -> data
        is Error -> null
        is NotFound -> null
    }

    fun upToDateDataOrNull(): T? = when (this) {
        is Success<T> -> data
        is Outdated<T> -> null
        is Error -> null
        is NotFound -> null
    }
}
