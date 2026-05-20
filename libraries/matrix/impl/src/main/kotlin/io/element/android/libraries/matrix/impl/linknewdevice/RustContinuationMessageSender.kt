/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.linknewdevice

import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.linknewdevice.ContinuationMessageSender
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.ContinuationMessageSender as FfiContinuationMessageSender

class RustContinuationMessageSender(
    private val inner: FfiContinuationMessageSender,
    private val sessionDispatcher: CoroutineDispatcher,
) : ContinuationMessageSender {
    override suspend fun cancel(): Result<Unit> = withContext(sessionDispatcher) {
        runCatchingExceptions {
            inner.cancel()
        }
    }

    override suspend fun confirm(): Result<Unit> = withContext(sessionDispatcher) {
        runCatchingExceptions {
            inner.confirm()
        }
    }
}
