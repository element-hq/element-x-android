/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.features.location.api.R
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import org.junit.Test

class DefaultLocationServiceTest {
    @Test
    fun `if apiKey is empty, isServiceAvailable should return false`() {
        val fakeStringProvider = FakeStringProvider(
            defaultResult = ""
        )
        val locationService = DefaultLocationService(
            stringProvider = fakeStringProvider,
        )
        assertThat(locationService.isServiceAvailable()).isFalse()
        assertThat(fakeStringProvider.lastResIdParam).isEqualTo(R.string.maptiler_api_key)
    }

    @Test
    fun `if apiKey is not empty, isServiceAvailable should return true`() {
        val locationService = DefaultLocationService(
            stringProvider = FakeStringProvider(
                defaultResult = "aKey"
            )
        )
        assertThat(locationService.isServiceAvailable()).isTrue()
    }
}
