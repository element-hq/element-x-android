/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.testutils

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import io.element.android.libraries.ui.strings.CommonStrings
import org.junit.rules.TestRule

fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.clickOn(@StringRes res: Int) {
    val text = activity.getString(res)
    onNode(hasText(text) and hasClickAction())
        .performClick()
}

fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.clickOnFirst(@StringRes res: Int) {
    val text = activity.getString(res)
    onAllNodes(hasText(text) and hasClickAction()).onFirst().performClick()
}

fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.clickOnLast(@StringRes res: Int) {
    val text = activity.getString(res)
    onAllNodes(hasText(text) and hasClickAction()).onFirst().performClick()
}

/**
 * Press the back button in the app bar.
 */
fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.pressBack() {
    val text = activity.getString(CommonStrings.action_back)
    onNode(hasContentDescription(text)).performClick()
}

/**
 * Press the back key.
 */
fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.pressBackKey() {
    activity.onBackPressedDispatcher.onBackPressed()
}

fun SemanticsNodeInteractionsProvider.pressTag(tag: String) {
    onNode(hasTestTag(tag)).performClick()
}
