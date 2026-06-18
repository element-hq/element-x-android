/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import io.element.android.features.lockscreen.impl.unlock.activity.PinUnlockActivity
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class DefaultLockScreenEntryPointIntentTest : RobolectricTest() {
    @Test
    fun `test pin unlock intent`() {
        val entryPoint = DefaultLockScreenEntryPoint()
        val result = entryPoint.pinUnlockIntent(InstrumentationRegistry.getInstrumentation().context)
        assertThat(result.component?.className).isEqualTo(PinUnlockActivity::class.qualifiedName)
    }
}
