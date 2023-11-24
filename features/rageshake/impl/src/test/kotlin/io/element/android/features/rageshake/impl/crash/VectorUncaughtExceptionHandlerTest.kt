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
