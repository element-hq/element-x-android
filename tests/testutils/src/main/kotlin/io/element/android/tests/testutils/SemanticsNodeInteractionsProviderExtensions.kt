/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
