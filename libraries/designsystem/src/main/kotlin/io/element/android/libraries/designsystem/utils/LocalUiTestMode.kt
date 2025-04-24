/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.utils

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import org.junit.rules.TestRule

/**
 * A composition local that indicates whether the app is running in UI test mode.
 */
val LocalUiTestMode = staticCompositionLocalOf { false }

/**
 * Sets the UI testing mode as enabled.
 *
 * This is used for working around issues like https://issuetracker.google.com/issues/366255137.
 */
fun <R : TestRule, A : ComponentActivity> AndroidComposeTestRule<R, A>.setContentForUiTest(
    block: @Composable () -> Unit,
) {
    setContent {
        CompositionLocalProvider(LocalUiTestMode provides true, block)
    }
}
