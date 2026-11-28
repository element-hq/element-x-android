/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.network

import io.element.android.libraries.matrix.api.network.HttpClientException
import org.matrix.rustcomponents.sdk.HttpClientException as RustHttpClientException

fun Throwable.mapHttpClientException(): HttpClientException {
    return when (this) {
        is HttpClientException -> this
        is RustHttpClientException.GetUrl -> HttpClientException(
            message = msg,
            statusCode = statusCode?.toInt(),
            cause = this,
        )
        else -> HttpClientException(message = message ?: "Unknown error", cause = this)
    }
}
