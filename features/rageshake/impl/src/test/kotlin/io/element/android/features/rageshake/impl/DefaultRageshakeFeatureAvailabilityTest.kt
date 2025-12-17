/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Test

class DefaultRageshakeFeatureAvailabilityTest {
    @Test
    fun `test isAvailable returns true when bug reporter URL is provided`() = runTest {
        val flow = MutableStateFlow<HttpUrl?>(null)
        val sut = DefaultRageshakeFeatureAvailability(
            bugReporterUrlProvider = { flow },
        )
        sut.isAvailable().test {
            assertThat(awaitItem()).isFalse()
            flow.value = "https://example.com/bugreport".toHttpUrl()
            assertThat(awaitItem()).isTrue()
            flow.value = null
            assertThat(awaitItem()).isFalse()
        }
    }
}
