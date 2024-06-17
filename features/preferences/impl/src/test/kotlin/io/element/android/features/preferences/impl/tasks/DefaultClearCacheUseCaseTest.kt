/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.preferences.impl.tasks

import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.ftue.test.FakeFtueService
import io.element.android.features.preferences.impl.DefaultCacheService
import io.element.android.features.roomlist.impl.migration.InMemoryMigrationScreenStore
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DefaultClearCacheUseCaseTest {
    @Test
    fun `execute clear cache should do all the expected tasks`() = runTest {
        val clearCacheLambda = lambdaRecorder<Unit> { }
        val matrixClient = FakeMatrixClient(
            clearCacheLambda = clearCacheLambda,
        )
        val defaultCacheService = DefaultCacheService()
        val resetFtueLambda = lambdaRecorder<Unit> { }
        val ftueService = FakeFtueService(
            resetLambda = resetFtueLambda,
        )
        val resetMigrationLambda = lambdaRecorder<Unit> { }
        val migrationScreenStore = InMemoryMigrationScreenStore(
            resetLambda = resetMigrationLambda,
        )
        val sut = DefaultClearCacheUseCase(
            context = InstrumentationRegistry.getInstrumentation().context,
            matrixClient = matrixClient,
            coroutineDispatchers = testCoroutineDispatchers(),
            defaultCacheService = defaultCacheService,
            okHttpClient = { OkHttpClient.Builder().build() },
            ftueService = ftueService,
            migrationScreenStore = migrationScreenStore
        )
        defaultCacheService.clearedCacheEventFlow.test {
            sut.invoke()
            clearCacheLambda.assertions().isCalledOnce()
            resetFtueLambda.assertions().isCalledOnce()
            resetMigrationLambda.assertions().isCalledOnce()
            assertThat(awaitItem()).isEqualTo(matrixClient.sessionId)
        }
    }
}
