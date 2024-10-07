/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
