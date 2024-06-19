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

package io.element.android.appnav.di

import com.bumble.appyx.core.state.MutableSavedStateMapImpl
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MatrixClientsHolderTest {
    @Test
    fun `test getOrNull`() {
        val fakeAuthenticationService = FakeMatrixAuthenticationService()
        val matrixClientsHolder = MatrixClientsHolder(fakeAuthenticationService)
        assertThat(matrixClientsHolder.getOrNull(A_SESSION_ID)).isNull()
    }

    @Test
    fun `test getOrRestore`() = runTest {
        val fakeAuthenticationService = FakeMatrixAuthenticationService()
        val matrixClientsHolder = MatrixClientsHolder(fakeAuthenticationService)
        val fakeMatrixClient = FakeMatrixClient()
        fakeAuthenticationService.givenMatrixClient(fakeMatrixClient)
        assertThat(matrixClientsHolder.getOrNull(A_SESSION_ID)).isNull()
        assertThat(matrixClientsHolder.getOrRestore(A_SESSION_ID).getOrNull()).isEqualTo(fakeMatrixClient)
        // Do it again to hit the cache
        assertThat(matrixClientsHolder.getOrRestore(A_SESSION_ID).getOrNull()).isEqualTo(fakeMatrixClient)
        assertThat(matrixClientsHolder.getOrNull(A_SESSION_ID)).isEqualTo(fakeMatrixClient)
    }

    @Test
    fun `test remove`() = runTest {
        val fakeAuthenticationService = FakeMatrixAuthenticationService()
        val matrixClientsHolder = MatrixClientsHolder(fakeAuthenticationService)
        val fakeMatrixClient = FakeMatrixClient()
        fakeAuthenticationService.givenMatrixClient(fakeMatrixClient)
        assertThat(matrixClientsHolder.getOrRestore(A_SESSION_ID).getOrNull()).isEqualTo(fakeMatrixClient)
        assertThat(matrixClientsHolder.getOrNull(A_SESSION_ID)).isEqualTo(fakeMatrixClient)
        // Remove
        matrixClientsHolder.remove(A_SESSION_ID)
        assertThat(matrixClientsHolder.getOrNull(A_SESSION_ID)).isNull()
    }

    @Test
    fun `test remove all`() = runTest {
        val fakeAuthenticationService = FakeMatrixAuthenticationService()
        val matrixClientsHolder = MatrixClientsHolder(fakeAuthenticationService)
        val fakeMatrixClient = FakeMatrixClient()
        fakeAuthenticationService.givenMatrixClient(fakeMatrixClient)
        assertThat(matrixClientsHolder.getOrRestore(A_SESSION_ID).getOrNull()).isEqualTo(fakeMatrixClient)
        assertThat(matrixClientsHolder.getOrNull(A_SESSION_ID)).isEqualTo(fakeMatrixClient)
        // Remove all
        matrixClientsHolder.removeAll()
        assertThat(matrixClientsHolder.getOrNull(A_SESSION_ID)).isNull()
    }

    @Test
    fun `test save and restore`() = runTest {
        val fakeAuthenticationService = FakeMatrixAuthenticationService()
        val matrixClientsHolder = MatrixClientsHolder(fakeAuthenticationService)
        val fakeMatrixClient = FakeMatrixClient()
        fakeAuthenticationService.givenMatrixClient(fakeMatrixClient)
        matrixClientsHolder.getOrRestore(A_SESSION_ID)
        val savedStateMap = MutableSavedStateMapImpl { true }
        matrixClientsHolder.saveIntoSavedState(savedStateMap)
        assertThat(savedStateMap.size).isEqualTo(1)
        // Test Restore with non-empty map
        matrixClientsHolder.restoreWithSavedState(savedStateMap)
        // Empty the map
        matrixClientsHolder.removeAll()
        assertThat(matrixClientsHolder.getOrNull(A_SESSION_ID)).isNull()
        // Restore again
        matrixClientsHolder.restoreWithSavedState(savedStateMap)
        assertThat(matrixClientsHolder.getOrNull(A_SESSION_ID)).isEqualTo(fakeMatrixClient)
    }
}
