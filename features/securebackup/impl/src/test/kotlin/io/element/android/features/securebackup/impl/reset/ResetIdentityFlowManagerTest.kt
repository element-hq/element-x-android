/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.securebackup.impl.reset

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.encryption.IdentityResetHandle
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.matrix.test.encryption.FakeIdentityPasswordResetHandle
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ResetIdentityFlowManagerTest {
    @Test
    fun `getResetHandle - emits a reset handle`() = runTest {
        val startResetLambda = lambdaRecorder<Result<IdentityResetHandle?>> { Result.success(FakeIdentityPasswordResetHandle()) }
        val encryptionService = FakeEncryptionService(startIdentityResetLambda = startResetLambda)
        val flowManager = createFlowManager(encryptionService = encryptionService)

        flowManager.getResetHandle().test {
            assertThat(awaitItem().isLoading()).isTrue()
            assertThat(awaitItem().isSuccess()).isTrue()
            startResetLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `getResetHandle - om successful handle retrieval returns that same handle`() = runTest {
        val startResetLambda = lambdaRecorder<Result<IdentityResetHandle?>> { Result.success(FakeIdentityPasswordResetHandle()) }
        val encryptionService = FakeEncryptionService(startIdentityResetLambda = startResetLambda)
        val flowManager = createFlowManager(encryptionService = encryptionService)

        var result: AsyncData.Success<IdentityResetHandle>? = null
        flowManager.getResetHandle().test {
            assertThat(awaitItem().isLoading()).isTrue()
            result = awaitItem() as? AsyncData.Success<IdentityResetHandle>
            assertThat(result).isNotNull()
        }

        flowManager.getResetHandle().test {
            assertThat(awaitItem()).isSameInstanceAs(result)
        }
    }

    @Test
    fun `getResetHandle - will fail if it receives a null reset handle`() = runTest {
        val startResetLambda = lambdaRecorder<Result<IdentityResetHandle?>> { Result.success(null) }
        val encryptionService = FakeEncryptionService(startIdentityResetLambda = startResetLambda)
        val flowManager = createFlowManager(encryptionService = encryptionService)

        flowManager.getResetHandle().test {
            assertThat(awaitItem().isLoading()).isTrue()
            assertThat(awaitItem().isFailure()).isTrue()
            startResetLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `getResetHandle - fails gracefully when receiving an exception from the encryption service`() = runTest {
        val startResetLambda = lambdaRecorder<Result<IdentityResetHandle?>> { Result.failure(IllegalStateException("Failure")) }
        val encryptionService = FakeEncryptionService(startIdentityResetLambda = startResetLambda)
        val flowManager = createFlowManager(encryptionService = encryptionService)

        flowManager.getResetHandle().test {
            assertThat(awaitItem().isLoading()).isTrue()
            assertThat(awaitItem().isFailure()).isTrue()
            startResetLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `cancel - resets the state and calls cancel on the reset handle`() = runTest {
        val cancelLambda = lambdaRecorder<Unit> { }
        val resetHandle = FakeIdentityPasswordResetHandle(cancelLambda = cancelLambda)
        val startResetLambda = lambdaRecorder<Result<IdentityResetHandle?>> { Result.success(resetHandle) }
        val encryptionService = FakeEncryptionService(startIdentityResetLambda = startResetLambda)
        val flowManager = createFlowManager(encryptionService = encryptionService)

        flowManager.getResetHandle().test {
            assertThat(awaitItem().isLoading()).isTrue()
            assertThat(awaitItem().isSuccess()).isTrue()

            flowManager.cancel()
            cancelLambda.assertions().isCalledOnce()
            assertThat(awaitItem().isUninitialized()).isTrue()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `whenResetIsDone - will trigger the lambda when verification status is verified`() = runTest {
        val verificationService = FakeSessionVerificationService()
        val flowManager = createFlowManager(sessionVerificationService = verificationService)
        var isDone = false

        flowManager.whenResetIsDone {
            isDone = true
        }

        assertThat(isDone).isFalse()

        verificationService.emitVerifiedStatus(SessionVerifiedStatus.Unknown)
        advanceUntilIdle()
        assertThat(isDone).isFalse()

        verificationService.emitVerifiedStatus(SessionVerifiedStatus.NotVerified)
        advanceUntilIdle()
        assertThat(isDone).isFalse()

        verificationService.emitVerifiedStatus(SessionVerifiedStatus.Verified)
        advanceUntilIdle()
        assertThat(isDone).isTrue()
    }

    private fun TestScope.createFlowManager(
        encryptionService: FakeEncryptionService = FakeEncryptionService(),
        client: FakeMatrixClient = FakeMatrixClient(encryptionService = encryptionService),
        sessionCoroutineScope: CoroutineScope = this,
        sessionVerificationService: FakeSessionVerificationService = FakeSessionVerificationService(),
    ) = ResetIdentityFlowManager(
        matrixClient = client,
        sessionCoroutineScope = sessionCoroutineScope,
        sessionVerificationService = sessionVerificationService,
    )
}
