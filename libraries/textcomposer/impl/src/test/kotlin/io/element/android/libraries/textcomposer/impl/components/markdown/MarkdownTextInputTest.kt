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

package io.element.android.libraries.textcomposer.impl.components.markdown

import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.textcomposer.ElementRichTextEditorStyle
import io.element.android.libraries.textcomposer.components.markdown.MarkdownTextInput
import io.element.android.libraries.textcomposer.components.markdown.aMarkdownTextEditorState
import io.element.android.libraries.textcomposer.model.MarkdownTextEditorState
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.libraries.textcomposer.model.Suggestion
import io.element.android.libraries.textcomposer.model.SuggestionType
import io.element.android.tests.testutils.EnsureCalledOnceWithParam
import io.element.android.tests.testutils.EventsRecorder
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MarkdownTextInputTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `when user types onTyping is triggered with value 'true'`() = runTest {
        val state = aMarkdownTextEditorState(initialFocus = true)
        val onTyping = EnsureCalledOnceWithParam(expectedParam = true, result = Unit)
        rule.setMarkdownTextInput(state = state, onTyping = onTyping)
        rule.activityRule.scenario.onActivity {
            it.findEditor().setText("Test")
        }
        rule.awaitIdle()
        onTyping.assertSuccess()
    }

    @Test
    fun `when user removes text onTyping is triggered with value 'false'`() = runTest {
        val state = aMarkdownTextEditorState(initialFocus = true)
        val onTyping = EventsRecorder<Boolean>()
        rule.setMarkdownTextInput(state = state, onTyping = onTyping)
        rule.activityRule.scenario.onActivity {
            val editText = it.findEditor()
            editText.setText("Test")
            editText.setText("")
        }
        rule.awaitIdle()
        onTyping.assertList(listOf(true, false))
    }

    @Test
    fun `when user types something that's not a mention onSuggestionReceived is triggered with 'null'`() = runTest {
        val state = aMarkdownTextEditorState(initialFocus = true)
        val onSuggestionReceived = EventsRecorder<Suggestion?>()
        rule.setMarkdownTextInput(state = state, onSuggestionReceived = onSuggestionReceived)
        rule.activityRule.scenario.onActivity {
            it.findEditor().setText("Test")
        }
        rule.awaitIdle()
        onSuggestionReceived.assertList(listOf(null, null)) // Initial value and after typing
    }

    @Test
    fun `when user types something that's a mention onSuggestionReceived is triggered a real value`() = runTest {
        val state = aMarkdownTextEditorState(initialFocus = true)
        val onSuggestionReceived = EventsRecorder<Suggestion?>()
        rule.setMarkdownTextInput(state = state, onSuggestionReceived = onSuggestionReceived)
        rule.activityRule.scenario.onActivity {
            it.findEditor().setText("@Al")
        }
        rule.awaitIdle()
        onSuggestionReceived.assertList(
            listOf(
                // From setting text
                Suggestion(0, 3, SuggestionType.Mention, "Al"),
                // From setting selection
                Suggestion(0, 3, SuggestionType.Mention, "Al"),
            )
        )
    }

    private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setMarkdownTextInput(
        state: MarkdownTextEditorState = aMarkdownTextEditorState(),
        subcomposing: Boolean = false,
        onTyping: (Boolean) -> Unit = {},
        onSuggestionReceived: (Suggestion?) -> Unit = {},
        composerMode: MessageComposerMode = MessageComposerMode.Normal,
    ) {
        rule.setContent {
            val style = ElementRichTextEditorStyle.composerStyle(hasFocus = state.hasFocus)
            MarkdownTextInput(
                state = state,
                subcomposing = subcomposing,
                onTyping = onTyping,
                onSuggestionReceived = onSuggestionReceived,
                composerMode = composerMode,
                richTextEditorStyle = style,
            )
        }
    }

    private fun ComponentActivity.findEditor(): EditText {
        return window.decorView.findViewWithTag(TestTags.plainTextEditor.value)
    }
}
