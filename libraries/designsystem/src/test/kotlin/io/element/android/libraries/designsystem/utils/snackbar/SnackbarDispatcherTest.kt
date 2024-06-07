/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
