/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.preferences.api.store

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class EnableNativeSlidingSyncUseCaseTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `ensure that the use case sets the simplified sliding sync enabled flag`() = runTest {
        val preferencesStore = InMemoryAppPreferencesStore()
        val useCase = EnableNativeSlidingSyncUseCase(preferencesStore, this)
        assertThat(preferencesStore.isSimplifiedSlidingSyncEnabledFlow().first()).isFalse()

        useCase()
        advanceUntilIdle()

        assertThat(preferencesStore.isSimplifiedSlidingSyncEnabledFlow().first()).isTrue()
    }
}
