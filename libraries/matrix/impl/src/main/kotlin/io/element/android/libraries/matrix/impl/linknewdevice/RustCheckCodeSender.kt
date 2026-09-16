/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.linknewdevice

import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.linknewdevice.CheckCodeSender
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.CheckCodeSender as FfiCheckCodeSender

class RustCheckCodeSender(
    private val inner: FfiCheckCodeSender,
    private val sessionDispatcher: CoroutineDispatcher,
) : CheckCodeSender {
    override suspend fun validate(code: UByte): Boolean = withContext(sessionDispatcher) {
        runCatchingExceptions {
            // TODO https://github.com/matrix-org/matrix-rust-sdk/pull/5957
            // inner.validate(code)
            true
        }.getOrNull() ?: true
    }

    override suspend fun send(code: UByte): Result<Unit> = withContext(sessionDispatcher) {
        runCatchingExceptions {
            inner.send(code)
        }
    }
}
