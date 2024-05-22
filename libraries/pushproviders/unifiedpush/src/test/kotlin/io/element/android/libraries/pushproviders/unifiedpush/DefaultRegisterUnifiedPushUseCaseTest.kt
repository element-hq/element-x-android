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
import org.junit.Ignore
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

    @Ignore("Find a solution to test timeout")
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
            coroutineScope = this@createDefaultRegisterUnifiedPushUseCase
        )
    }
}
