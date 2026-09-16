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
import androidx.compose.ui.platform.LocalContext
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.tests.testutils.robolectric.RobolectricTest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.robolectric.RuntimeEnvironment

class ThemeTest : RobolectricTest() {
    @Test
    fun `isDark for System dark returns true`() {
        `isDark for System`(
            systemUiMode = Configuration.UI_MODE_NIGHT_YES,
            expected = true,
        )
    }

    @Test
    fun `isDark for System light return false`() {
        `isDark for System`(
            systemUiMode = Configuration.UI_MODE_NIGHT_NO,
            expected = false,
        )
    }

    @Test
    fun `isDark for System ignores the night mode AppCompat forced on the activity`() {
        `isDark for System`(
            systemUiMode = Configuration.UI_MODE_NIGHT_YES,
            activityUiMode = Configuration.UI_MODE_NIGHT_NO,
            expected = true,
        )
        `isDark for System`(
            systemUiMode = Configuration.UI_MODE_NIGHT_NO,
            activityUiMode = Configuration.UI_MODE_NIGHT_YES,
            expected = false,
        )
    }

    private fun `isDark for System`(
        systemUiMode: Int,
        activityUiMode: Int = systemUiMode,
        expected: Boolean,
    ) = runTest {
        val application = RuntimeEnvironment.getApplication()
        application.resources.configuration.uiMode = systemUiMode
        moleculeFlow(RecompositionMode.Immediate) {
            var result: Boolean? = null
            CompositionLocalProvider(
                LocalContext provides application,
                LocalConfiguration provides Configuration().apply {
                    this.uiMode = activityUiMode
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

    @Test
    fun `mapToTheme falls back to dark when black theme is disabled`() = runTest {
        flowOf(Theme.Black.name)
            .mapToTheme(allowBlackTheme = false)
            .test {
                assertThat(awaitItem()).isEqualTo(Theme.Dark)
                awaitComplete()
            }
    }
}
