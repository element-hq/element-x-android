/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.exception

import io.element.android.libraries.matrix.api.exception.ClientException
import org.matrix.rustcomponents.sdk.ClientException as RustClientException

fun Throwable.mapClientException(): ClientException {
    return when (this) {
        is RustClientException -> {
            when (this) {
                is RustClientException.Generic -> ClientException.Generic(msg)
            }
        }
        else -> ClientException.Other(message ?: "Unknown error")
    }
}
