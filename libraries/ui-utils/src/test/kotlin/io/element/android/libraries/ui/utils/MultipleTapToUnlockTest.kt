/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.libraries.ui.utils

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class MultipleTapToUnlockTest {
    @Test
    fun `test multiple tap should unlock`() = runTest {
        val sut = MultipleTapToUnlock(3)
        assertThat(sut.unlock(backgroundScope)).isFalse()
        assertThat(sut.unlock(backgroundScope)).isFalse()
        assertThat(sut.unlock(backgroundScope)).isTrue()
        assertThat(sut.unlock(backgroundScope)).isTrue()
        // All next call returns true
        advanceTimeBy(3.seconds)
        assertThat(sut.unlock(backgroundScope)).isTrue()
    }
    @Test
    fun `test waiting should reset counter`() = runTest {
        val sut = MultipleTapToUnlock(3)
        assertThat(sut.unlock(backgroundScope)).isFalse()
        assertThat(sut.unlock(backgroundScope)).isFalse()
        advanceTimeBy(3.seconds)
        assertThat(sut.unlock(backgroundScope)).isFalse()
        assertThat(sut.unlock(backgroundScope)).isFalse()
        assertThat(sut.unlock(backgroundScope)).isTrue()
    }
}
