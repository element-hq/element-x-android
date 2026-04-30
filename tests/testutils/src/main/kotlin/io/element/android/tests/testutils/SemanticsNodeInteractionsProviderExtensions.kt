/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.tests.testutils

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.element.android.libraries.ui.strings.CommonStrings

val trueMatcher = SemanticsMatcher("true matcher") { true }

fun AndroidComposeUiTest<ComponentActivity>.clickOn(
    @StringRes res: Int,
    inDialog: Boolean = false,
) {
    val text = activity!!.getString(res)
    onNode(
        hasText(text) and hasClickAction() and if (inDialog) hasAnyAncestor(isDialog()) else trueMatcher
    )
        .performClick()
}

/**
 * Press the back button in the app bar.
 */
fun AndroidComposeUiTest<ComponentActivity>.pressBack() {
    val text = activity!!.getString(CommonStrings.action_back)
    onNode(hasContentDescription(text)).performClick()
}

/**
 * Press the back key.
 */
fun AndroidComposeUiTest<ComponentActivity>.pressBackKey() {
    activity!!.onBackPressedDispatcher.onBackPressed()
}

fun SemanticsNodeInteractionsProvider.pressTag(tag: String) {
    onNode(hasTestTag(tag)).performClick()
}

fun AndroidComposeUiTest<ComponentActivity>.assertNoNodeWithText(@StringRes res: Int) {
    val text = activity!!.getString(res)
    onNodeWithText(text).assertDoesNotExist()
}

fun AndroidComposeUiTest<ComponentActivity>.assertNodeWithTextIsDisplayed(@StringRes res: Int) {
    val text = activity!!.getString(res)
    onNodeWithText(text).assertIsDisplayed()
}
