/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.crash

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class VectorUncaughtExceptionHandlerTest {
    @Test
    fun `activate should change the default handler`() {
        val sut = VectorUncaughtExceptionHandler(RuntimeEnvironment.getApplication())
        sut.activate()
        assertThat(Thread.getDefaultUncaughtExceptionHandler()).isInstanceOf(VectorUncaughtExceptionHandler::class.java)
    }

    @Test
    fun `uncaught exception`() = runTest {
        val crashDataStore = PreferencesCrashDataStore(RuntimeEnvironment.getApplication())
        assertThat(crashDataStore.appHasCrashed().first()).isFalse()
        assertThat(crashDataStore.crashInfo().first()).isEmpty()
        val sut = VectorUncaughtExceptionHandler(RuntimeEnvironment.getApplication())
        sut.uncaughtException(Thread(), AN_EXCEPTION)
        assertThat(crashDataStore.appHasCrashed().first()).isTrue()
        val crashInfo = crashDataStore.crashInfo().first()
        assertThat(crashInfo).isNotEmpty()
        assertThat(crashInfo).contains("Memory statuses")
        crashDataStore.resetAppHasCrashed()
        assertThat(crashDataStore.appHasCrashed().first()).isFalse()
    }
}
