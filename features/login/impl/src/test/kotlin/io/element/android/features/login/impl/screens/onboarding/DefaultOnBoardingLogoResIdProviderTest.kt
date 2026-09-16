/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.onboarding

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class DefaultOnBoardingLogoResIdProviderTest : RobolectricTest() {
    @Test
    fun `when onboarding_logo resource does not exist, get() returns null`() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val sut = DefaultOnBoardingLogoResIdProvider(context)
        val result = sut.get()
        assertThat(result).isNull()
    }
}
