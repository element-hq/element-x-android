/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.verification

import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiClient
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiSessionVerificationController
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RustSessionVerificationServiceTest {
    @Test
    fun `destroy destroys the verification controller once it has been created`() = runTest {
        val destroyResult = lambdaRecorder<Unit> { }
        val service = createRustSessionVerificationService(
            controller = FakeFfiSessionVerificationController(destroyResult = destroyResult),
        )
        runInitialization()
        service.destroy()
        destroyResult.assertions().isCalledOnce()
    }

    @Test
    fun `the verification controller is destroyed if it's created after the service was destroyed`() = runTest {
        val destroyResult = lambdaRecorder<Unit> { }
        val service = createRustSessionVerificationService(
            controller = FakeFfiSessionVerificationController(destroyResult = destroyResult),
        )
        // The initialization starts, and is now waiting for the E2EE initialization tasks
        runCurrent()
        service.destroy()
        destroyResult.assertions().isNeverCalled()
        // The initialization can't be cancelled, so it completes and creates the controller after the service was destroyed
        runInitialization()
        destroyResult.assertions().isCalledOnce()
    }

    // The initialization runs in the background scope, waiting for the simulated E2EE initialization tasks
    private fun TestScope.runInitialization() {
        advanceTimeBy(1)
        runCurrent()
    }

    private fun TestScope.createRustSessionVerificationService(
        controller: FakeFfiSessionVerificationController = FakeFfiSessionVerificationController(),
    ) = RustSessionVerificationService(
        client = FakeFfiClient(sessionVerificationController = controller),
        isSyncServiceReady = flowOf(false),
        sessionCoroutineScope = backgroundScope,
        sessionDispatcher = StandardTestDispatcher(testScheduler),
    )
}
