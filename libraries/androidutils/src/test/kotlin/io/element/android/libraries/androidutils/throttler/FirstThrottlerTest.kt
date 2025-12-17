/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.libraries.androidutils.throttler

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FirstThrottlerTest {
    @Test
    fun `throttle canHandle returns the expected result`() = runTest {
        val throttler = FirstThrottler(
            minimumInterval = 300,
            coroutineScope = backgroundScope,
        )
        assertThat(throttler.canHandle()).isTrue()
        assertThat(throttler.canHandle()).isFalse()
        advanceTimeBy(200)
        assertThat(throttler.canHandle()).isFalse()
        advanceTimeBy(110)
        assertThat(throttler.canHandle()).isTrue()
    }
}
