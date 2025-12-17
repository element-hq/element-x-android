/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.exception

sealed class ClientException(message: String, val details: String?, cause: Throwable? = null) : Exception(message, cause) {
    class Generic(message: String, details: String?, cause: Throwable? = null) : ClientException(message, details, cause)
    class MatrixApi(val kind: ErrorKind, val code: String, message: String, details: String?, cause: Throwable? = null) : ClientException(
        message = message,
        details = details,
        cause = cause
    )
    class Other(message: String, cause: Throwable? = null) : ClientException(message, null, cause)
}

fun ClientException.isNetworkError(): Boolean {
    return this is ClientException.Generic && message?.contains("error sending request for url", ignoreCase = true) == true
}
