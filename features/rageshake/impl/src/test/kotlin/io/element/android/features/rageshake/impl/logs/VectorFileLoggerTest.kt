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

package io.element.android.features.rageshake.impl.logs

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class VectorFileLoggerTest {
    @Test
    fun `init VectorFileLogger log debug`() = runTest {
        val sut = createVectorFileLogger()
        sut.d("A debug log")
    }

    @Test
    fun `init VectorFileLogger log error`() = runTest {
        val sut = createVectorFileLogger()
        sut.e(A_THROWABLE, "A debug log")
    }

    @Test
    fun `reset VectorFileLogger`() = runTest {
        val sut = createVectorFileLogger()
        sut.reset()
    }

    @Test
    fun `check getFromTimber`() {
        assertThat(VectorFileLogger.getFromTimber()).isNull()
    }

    private fun TestScope.createVectorFileLogger() = VectorFileLogger(
        context = RuntimeEnvironment.getApplication(),
        dispatcher = testCoroutineDispatchers().io,
    )
}
