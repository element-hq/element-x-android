/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.ui

import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.common.truth.Truth.assertThat
import io.element.android.tests.testutils.robolectric.RobolectricTest
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.robolectric.RuntimeEnvironment

class ViewTest : RobolectricTest() {
    @Test
    fun `awaitPreDraw resumes on the first drawing pass during which the condition holds`() = runTest {
        val view = View(RuntimeEnvironment.getApplication())
        var conditionHolds = false
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            view.awaitPreDraw { conditionHolds }
        }
        view.viewTreeObserver.dispatchOnPreDraw()
        runCurrent()
        assertThat(job.isCompleted).isFalse()
        conditionHolds = true
        view.viewTreeObserver.dispatchOnPreDraw()
        runCurrent()
        assertThat(job.isCompleted).isTrue()
    }

    @Test
    fun `awaitPreDraw stops checking the condition once cancelled`() = runTest {
        val view = View(RuntimeEnvironment.getApplication())
        var checks = 0
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            view.awaitPreDraw {
                checks++
                false
            }
        }
        view.viewTreeObserver.dispatchOnPreDraw()
        assertThat(checks).isEqualTo(1)
        job.cancel()
        runCurrent()
        view.viewTreeObserver.dispatchOnPreDraw()
        assertThat(checks).isEqualTo(1)
    }

    @Test
    fun `awaitPreDraw leaves the window insets listener of the view in place`() = runTest {
        val view = View(RuntimeEnvironment.getApplication())
        var insetsReceived = 0
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            insetsReceived++
            insets
        }
        launch(start = CoroutineStart.UNDISPATCHED) {
            view.awaitPreDraw { true }
        }
        view.viewTreeObserver.dispatchOnPreDraw()
        runCurrent()
        val insets = WindowInsetsCompat.Builder()
            .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, 100))
            .build()
        ViewCompat.dispatchApplyWindowInsets(view, insets)
        assertThat(insetsReceived).isEqualTo(1)
    }
}
