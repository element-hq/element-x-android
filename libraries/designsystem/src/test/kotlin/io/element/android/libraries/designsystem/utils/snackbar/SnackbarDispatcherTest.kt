/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.utils.snackbar

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SnackbarDispatcherTest {
    @Test
    fun `given an empty queue the flow emits a null item`() = runTest {
        val snackbarDispatcher = SnackbarDispatcher()
        snackbarDispatcher.snackbarMessage.test {
            assertThat(awaitItem()).isNull()
        }
    }

    @Test
    fun `given an empty queue calling clear does nothing`() = runTest {
        val snackbarDispatcher = SnackbarDispatcher()
        snackbarDispatcher.snackbarMessage.test {
            assertThat(awaitItem()).isNull()
            snackbarDispatcher.clear()
            expectNoEvents()
        }
    }

    @Test
    fun `given a non-empty queue the flow emits an item`() = runTest {
        val snackbarDispatcher = SnackbarDispatcher()
        snackbarDispatcher.snackbarMessage.test {
            snackbarDispatcher.post(SnackbarMessage(0))
            val result = expectMostRecentItem()
            assertThat(result).isNotNull()
        }
    }

    @Test
    fun `given a call to clear, the current message is cleared`() = runTest {
        val snackbarDispatcher = SnackbarDispatcher()
        snackbarDispatcher.snackbarMessage.test {
            snackbarDispatcher.post(SnackbarMessage(0))
            val item = expectMostRecentItem()
            assertThat(item).isNotNull()
            snackbarDispatcher.clear()
            assertThat(awaitItem()).isNull()
        }
    }

    @Test
    fun `given 2 message emissions, the next message is displayed only after a call to clear`() = runTest {
        val snackbarDispatcher = SnackbarDispatcher()
        snackbarDispatcher.snackbarMessage.test {
            val messageA = SnackbarMessage(0)
            val messageB = SnackbarMessage(1)

            // Send message A - it is the most recent item
            snackbarDispatcher.post(messageA)
            assertThat(expectMostRecentItem()).isEqualTo(messageA)

            // Send message B - message A is still the most recent item
            snackbarDispatcher.post(messageB)
            expectNoEvents()

            // Clear the last message - message B is now the most recent item
            snackbarDispatcher.clear()
            assertThat(expectMostRecentItem()).isEqualTo(messageB)

            // Clear again - the queue is empty
            snackbarDispatcher.clear()
            assertThat(awaitItem()).isNull()
        }
    }
}
