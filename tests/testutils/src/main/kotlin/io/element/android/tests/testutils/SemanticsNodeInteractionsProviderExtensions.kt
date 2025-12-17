/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.testutils

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.element.android.libraries.ui.strings.CommonStrings
import org.junit.rules.TestRule

val trueMatcher = SemanticsMatcher("true matcher") { true }

fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.clickOn(
    @StringRes res: Int,
    inDialog: Boolean = false,
) {
    val text = activity.getString(res)
    onNode(
        hasText(text) and hasClickAction() and if (inDialog) hasAnyAncestor(isDialog()) else trueMatcher
    )
        .performClick()
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

fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.assertNoNodeWithText(@StringRes res: Int) {
    val text = activity.getString(res)
    onNodeWithText(text).assertDoesNotExist()
}
