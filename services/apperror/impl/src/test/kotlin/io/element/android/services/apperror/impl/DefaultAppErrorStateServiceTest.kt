/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.apperror.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.services.apperror.api.AppErrorState
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class DefaultAppErrorStateServiceTest {
    @Test
    fun `initial value is no error`() = runTest {
        val service = DefaultAppErrorStateService()

        service.appErrorStateFlow.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(AppErrorState.NoError::class.java)
        }
    }

    @Test
    fun `showError - emits value`() = runTest {
        val service = DefaultAppErrorStateService()

        service.appErrorStateFlow.test {
            skipItems(1)

            service.showError("Title", "Body")
            val state = awaitItem()
            assertThat(state).isInstanceOf(AppErrorState.Error::class.java)

            val errorState = state as AppErrorState.Error
            assertThat(errorState.title).isEqualTo("Title")
            assertThat(errorState.body).isEqualTo("Body")
        }
    }

    @Test
    fun `dismiss - clears value`() = runTest {
        val service = DefaultAppErrorStateService()

        service.appErrorStateFlow.test {
            skipItems(1)

            service.showError("Title", "Body")
            val state = awaitItem()
            assertThat(state).isInstanceOf(AppErrorState.Error::class.java)

            val errorState = state as AppErrorState.Error
            errorState.dismiss()

            assertThat(awaitItem()).isInstanceOf(AppErrorState.NoError::class.java)
        }
    }
}
