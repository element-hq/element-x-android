/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.exception

sealed class ClientException(message: String) : Exception(message) {
    class Generic(message: String) : ClientException(message)
    class MatrixApi(val kind: ErrorKind, val code: String, message: String) : ClientException(message)
    class Other(message: String) : ClientException(message)
}

fun ClientException.isNetworkError(): Boolean {
    return this is ClientException.Generic && message?.contains("error sending request for url", ignoreCase = true) == true
}
