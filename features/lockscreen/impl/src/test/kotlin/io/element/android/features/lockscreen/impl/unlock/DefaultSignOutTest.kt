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

package io.element.android.features.lockscreen.impl.unlock

import com.google.common.truth.Truth.assertThat
import io.element.android.features.lockscreen.impl.unlock.signout.DefaultSignOut
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.test.auth.FakeAuthenticationService
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultSignOutTest {
    private val matrixClient = FakeMatrixClient()
    private val authenticationService = FakeAuthenticationService()
    private val matrixClientProvider = FakeMatrixClientProvider(getClient = { Result.success(matrixClient) })
    private val sut = DefaultSignOut(authenticationService, matrixClientProvider)

    @Test
    fun `when no active session then it throws`() = runTest {
        authenticationService.getLatestSessionIdLambda = { null }
        val result = runCatching { sut.invoke() }
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `with one active session and successful logout on client`() = runTest {
        val logoutLambda = lambdaRecorder<Boolean, String?> { _: Boolean -> null }
        authenticationService.getLatestSessionIdLambda = { matrixClient.sessionId }
        matrixClient.logoutLambda = logoutLambda
        val result = runCatching { sut.invoke() }
        assertThat(result.isSuccess).isTrue()
        assert(logoutLambda).isCalledOnce()
    }

    @Test
    fun `with one active session and and failed logout on client`() = runTest {
        val logoutLambda = lambdaRecorder<Boolean, String?> { _: Boolean -> error("Failed to logout") }
        authenticationService.getLatestSessionIdLambda = { matrixClient.sessionId }
        matrixClient.logoutLambda = logoutLambda
        val result = runCatching { sut.invoke() }
        assertThat(result.isFailure).isTrue()
        assert(logoutLambda).isCalledOnce()
    }
}
