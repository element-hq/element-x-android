/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.architecture

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class AsyncActionTest {
    @Test
    fun `updates state on timeout`() = runTest {
        val state: MutableState<AsyncAction<Int>> = mutableStateOf(AsyncAction.Uninitialized)
        val timeoutMillis = 500L
        val operationTimeMillis = 1000L

        try {
            runUpdatingState(state = state) {
                withTimeout(timeoutMillis.milliseconds) {
                    delay(operationTimeMillis)
                }
                Result.success(0)
            }
            fail("Expected TimeoutCancellationException, but nothing was thrown")
        } catch (e: TimeoutCancellationException) {
            assertTrue(state.value.isFailure())
            assertSame(e, state.value.errorOrNull())
        }
    }
}
