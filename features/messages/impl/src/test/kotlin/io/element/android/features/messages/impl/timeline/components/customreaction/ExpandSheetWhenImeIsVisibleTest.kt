/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalTestApi::class)

package io.element.android.features.messages.impl.timeline.components.customreaction

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.common.truth.Truth.assertThat
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class ExpandSheetWhenImeIsVisibleTest : RobolectricTest() {
    @Test
    fun `the sheet is left alone while the keyboard is hidden`() = runAndroidComposeUiTest<ComponentActivity> {
        val sheetState = renderWithIme(isImeVisible = false)
        assertThat(sheetState.currentValue).isEqualTo(SheetValue.PartiallyExpanded)
    }

    @Test
    fun `the sheet is expanded when the keyboard is shown`() = runAndroidComposeUiTest<ComponentActivity> {
        val sheetState = renderWithIme(isImeVisible = true)
        assertThat(sheetState.currentValue).isEqualTo(SheetValue.Expanded)
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.renderWithIme(isImeVisible: Boolean): SheetState {
    lateinit var sheetState: SheetState
    lateinit var view: View
    setContent {
        view = LocalView.current
        sheetState = rememberBottomSheetState(initialValue = SheetValue.PartiallyExpanded)
        ExpandSheetWhenImeIsVisible(sheetState)
    }
    runOnIdle {
        ViewCompat.dispatchApplyWindowInsets(
            view,
            WindowInsetsCompat.Builder()
                .setVisible(WindowInsetsCompat.Type.ime(), isImeVisible)
                .setInsets(WindowInsetsCompat.Type.ime(), if (isImeVisible) Insets.of(0, 0, 0, 300) else Insets.NONE)
                .build()
        )
    }
    waitForIdle()
    return sheetState
}
