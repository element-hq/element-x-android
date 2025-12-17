/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_SECRET
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.unifiedpush.registration.EndpointRegistrationHandler
import io.element.android.libraries.pushproviders.unifiedpush.registration.RegistrationResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DefaultRegisterUnifiedPushUseCaseTest {
    @Test
    fun `test registration successful`() = runTest {
        val endpointRegistrationHandler = EndpointRegistrationHandler()
        val useCase = createDefaultRegisterUnifiedPushUseCase(
            endpointRegistrationHandler = endpointRegistrationHandler
        )
        val aDistributor = Distributor("aValue", "aName")
        launch {
            delay(100)
            endpointRegistrationHandler.registrationDone(RegistrationResult(A_SECRET, Result.success(Unit)))
        }
        val result = useCase.execute(aDistributor, A_SECRET)
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `test registration error`() = runTest {
        val endpointRegistrationHandler = EndpointRegistrationHandler()
        val useCase = createDefaultRegisterUnifiedPushUseCase(
            endpointRegistrationHandler = endpointRegistrationHandler
        )
        val aDistributor = Distributor("aValue", "aName")
        launch {
            delay(100)
            endpointRegistrationHandler.registrationDone(RegistrationResult(A_SECRET, Result.failure(AN_EXCEPTION)))
        }
        val result = useCase.execute(aDistributor, A_SECRET)
        assertThat(result.isSuccess).isFalse()
    }

    @Test
    fun `test registration timeout`() = runTest {
        val endpointRegistrationHandler = EndpointRegistrationHandler()
        val useCase = createDefaultRegisterUnifiedPushUseCase(
            endpointRegistrationHandler = endpointRegistrationHandler
        )
        val aDistributor = Distributor("aValue", "aName")
        val result = useCase.execute(aDistributor, A_SECRET)
        assertThat(result.isSuccess).isFalse()
    }

    private fun TestScope.createDefaultRegisterUnifiedPushUseCase(
        endpointRegistrationHandler: EndpointRegistrationHandler
    ): DefaultRegisterUnifiedPushUseCase {
        val context = InstrumentationRegistry.getInstrumentation().context
        return DefaultRegisterUnifiedPushUseCase(
            context = context,
            endpointRegistrationHandler = endpointRegistrationHandler,
        )
    }
}
