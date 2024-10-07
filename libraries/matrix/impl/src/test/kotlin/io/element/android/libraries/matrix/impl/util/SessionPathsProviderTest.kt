/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.util

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.sessionstorage.impl.memory.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SessionPathsProviderTest {
    @Test
    fun `if session is not found, provides returns null`() = runTest {
        val sut = SessionPathsProvider(InMemorySessionStore())
        val result = sut.provides(A_SESSION_ID)
        assertThat(result).isNull()
    }

    @Test
    fun `if session is found, provides returns the data`() = runTest {
        val store = InMemorySessionStore()
        val sut = SessionPathsProvider(store)
        store.storeData(
            aSessionData(
                sessionPath = "/a/path/to/a/session",
                cachePath = "/a/path/to/a/cache",
            )
        )
        val result = sut.provides(A_SESSION_ID)!!
        assertThat(result.fileDirectory.absolutePath).isEqualTo("/a/path/to/a/session")
        assertThat(result.cacheDirectory.absolutePath).isEqualTo("/a/path/to/a/cache")
    }
}
