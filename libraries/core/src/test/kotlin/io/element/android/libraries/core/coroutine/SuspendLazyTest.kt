/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.core.coroutine

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SuspendLazyTest {
    @Test
    fun `the value is computed lazily and returned`() = runTest {
        var computeCount = 0
        val lazyValue = suspendLazy(coroutineContext) {
            computeCount++
            "value"
        }
        assertThat(lazyValue.isInitialized()).isFalse()
        assertThat(lazyValue.value.await()).isEqualTo("value")
        assertThat(lazyValue.value.await()).isEqualTo("value")
        assertThat(computeCount).isEqualTo(1)
    }

    @Test
    fun `await fails instead of suspending forever if the scope is cancelled`() = runTest {
        val job = Job().apply { cancel() }
        val lazyValue = suspendLazy(job) { "value" }
        val error = awaitError { lazyValue.value.await() }
        assertThat(error).isInstanceOf(CancellationException::class.java)
    }

    @Test
    fun `await fails instead of suspending forever if the block throws`() = runTest {
        val error = IllegalStateException("Failure")
        val lazyValue = suspendLazy(SupervisorJob() + CoroutineExceptionHandler { _, _ -> }) { throw error }
        val thrown = awaitError { lazyValue.value.await() }
        // The exception may be a copy with a recovered stack trace, so check its type and message
        assertThat(thrown).isInstanceOf(IllegalStateException::class.java)
        assertThat(thrown).hasMessageThat().isEqualTo(error.message)
    }

    @Suppress("TooGenericExceptionCaught", "SuspendFunSwallowedCancellation")
    private suspend fun awaitError(block: suspend () -> Unit): Throwable? {
        return try {
            block()
            null
        } catch (e: Throwable) {
            e
        }
    }
}
