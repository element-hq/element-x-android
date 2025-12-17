/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.reset

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.encryption.IdentityResetHandle
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.matrix.test.encryption.FakeIdentityPasswordResetHandle
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import io.element.android.tests.testutils.lambda.lambdaRecorder
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
            @Suppress("UNCHECKED_CAST")
            result = awaitItem() as? AsyncData.Success<IdentityResetHandle>
            assertThat(result).isNotNull()
        }

        flowManager.getResetHandle().test {
            assertThat(awaitItem()).isSameInstanceAs(result)
        }
    }

    @Test
    fun `getResetHandle - will success if it receives a null reset handle`() = runTest {
        val startResetLambda = lambdaRecorder<Result<IdentityResetHandle?>> { Result.success(null) }
        val encryptionService = FakeEncryptionService(startIdentityResetLambda = startResetLambda)
        val flowManager = createFlowManager(encryptionService = encryptionService)

        flowManager.getResetHandle().test {
            assertThat(awaitItem().isLoading()).isTrue()
            val finalItem = awaitItem()
            assertThat(finalItem.isSuccess()).isTrue()
            assertThat(finalItem.dataOrNull()).isNull()
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
        sessionVerificationService: FakeSessionVerificationService = FakeSessionVerificationService(),
    ) = ResetIdentityFlowManager(
        encryptionService = encryptionService,
        sessionCoroutineScope = this,
        sessionVerificationService = sessionVerificationService,
    )
}
