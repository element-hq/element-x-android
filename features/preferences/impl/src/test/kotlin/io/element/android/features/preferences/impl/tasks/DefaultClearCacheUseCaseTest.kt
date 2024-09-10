/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.tasks

import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.ftue.test.FakeFtueService
import io.element.android.features.preferences.impl.DefaultCacheService
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.push.test.FakePushService
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
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
        val setIgnoreRegistrationErrorLambda = lambdaRecorder<SessionId, Boolean, Unit> { _, _ -> }
        val pushService = FakePushService(
            setIgnoreRegistrationErrorLambda = setIgnoreRegistrationErrorLambda
        )
        val sut = DefaultClearCacheUseCase(
            context = InstrumentationRegistry.getInstrumentation().context,
            matrixClient = matrixClient,
            coroutineDispatchers = testCoroutineDispatchers(),
            defaultCacheService = defaultCacheService,
            okHttpClient = { OkHttpClient.Builder().build() },
            ftueService = ftueService,
            pushService = pushService,
        )
        defaultCacheService.clearedCacheEventFlow.test {
            sut.invoke()
            clearCacheLambda.assertions().isCalledOnce()
            resetFtueLambda.assertions().isCalledOnce()
            setIgnoreRegistrationErrorLambda.assertions().isCalledOnce()
                .with(value(matrixClient.sessionId), value(false))
            assertThat(awaitItem()).isEqualTo(matrixClient.sessionId)
        }
    }
}
