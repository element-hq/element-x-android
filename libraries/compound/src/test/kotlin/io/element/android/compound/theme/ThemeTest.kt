/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.theme

import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ThemeTest {
    @Test
    fun `isDark for System dark returns true`() {
        `isDark for System`(
            uiMode = Configuration.UI_MODE_NIGHT_YES,
            expected = true,
        )
    }

    @Test
    fun `isDark for System light return false`() {
        `isDark for System`(
            uiMode = Configuration.UI_MODE_NIGHT_NO,
            expected = false,
        )
    }

    fun `isDark for System`(
        uiMode: Int,
        expected: Boolean,
    ) = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            var result: Boolean? = null
            CompositionLocalProvider(
                // Let set the system to dark
                LocalConfiguration provides Configuration().apply {
                    this.uiMode = uiMode
                },
            ) {
                result = Theme.System.isDark()
            }
            result
        }.test {
            assertThat(awaitItem()).isEqualTo(expected)
        }
    }

    @Test
    fun `isDark for Light returns false`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            Theme.Light.isDark()
        }.test {
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `isDark for Dark returns true`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            Theme.Dark.isDark()
        }.test {
            assertThat(awaitItem()).isTrue()
        }
    }
}
