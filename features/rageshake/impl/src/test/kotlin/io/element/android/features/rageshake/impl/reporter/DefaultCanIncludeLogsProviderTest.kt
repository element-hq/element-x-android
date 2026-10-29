/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.reporter

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultCanIncludeLogsProviderTest {
    @Test
    fun `logs can be included when they are written to the default folder`() = runTest {
        DefaultCanIncludeLogsProvider { flowOf(LogFolder.Default) }.canIncludeLogs().test {
            assertThat(awaitItem()).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `logs can be included when they are written to a server folder`() = runTest {
        DefaultCanIncludeLogsProvider { flowOf(LogFolder.Server("a.org")) }.canIncludeLogs().test {
            assertThat(awaitItem()).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `logs cannot be included when they are written to the local-only folder`() = runTest {
        DefaultCanIncludeLogsProvider { flowOf(LogFolder.LocalOnly) }.canIncludeLogs().test {
            assertThat(awaitItem()).isFalse()
            awaitComplete()
        }
    }

    @Test
    fun `changes are not emitted when the value does not change`() = runTest {
        val logFolders = flowOf(LogFolder.Default, LogFolder.Server("a.org"), LogFolder.LocalOnly, LogFolder.LocalOnly)
        DefaultCanIncludeLogsProvider { logFolders }.canIncludeLogs().test {
            assertThat(awaitItem()).isTrue()
            assertThat(awaitItem()).isFalse()
            awaitComplete()
        }
    }
}
