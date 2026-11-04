/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.rtc

import org.matrix.rtc.FfiBackendException
import org.matrix.rustcomponents.sdk.ClientException
import org.matrix.rustcomponents.sdk.ErrorKind

/**
 * A failure as the core classifies it, which it does on the Matrix errcode. The server's retry-after is passed on so the
 * core waits that long rather than its own backoff step, though the SDK has usually spent it already, retrying a rate
 * limit itself before the call returns.
 */
internal fun Throwable.toFfiBackendException(): FfiBackendException = when (this) {
    is FfiBackendException -> this
    // The bindings do not carry the HTTP status.
    is ClientException.MatrixApi -> FfiBackendException.Failed(
        errcode = code,
        status = null,
        retryAfterMs = (kind as? ErrorKind.LimitExceeded)?.retryAfterMs,
        permanent = false,
        reason = msg,
    )
    else -> FfiBackendException.Failed(errcode = null, status = null, retryAfterMs = null, permanent = false, reason = message ?: javaClass.simpleName)
}
