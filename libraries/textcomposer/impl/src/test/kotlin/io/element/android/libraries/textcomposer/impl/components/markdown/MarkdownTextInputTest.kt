/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.impl.components.markdown

import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.text.getSpans
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.textcomposer.ElementRichTextEditorStyle
import io.element.android.libraries.textcomposer.components.markdown.MarkdownTextInput
import io.element.android.libraries.textcomposer.impl.mentions.aMentionSpanProvider
import io.element.android.libraries.textcomposer.mentions.MentionSpan
import io.element.android.libraries.textcomposer.mentions.ResolvedSuggestion
import io.element.android.libraries.textcomposer.model.MarkdownTextEditorState
import io.element.android.libraries.textcomposer.model.Suggestion
import io.element.android.libraries.textcomposer.model.SuggestionType
import io.element.android.libraries.textcomposer.model.aMarkdownTextEditorState
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
            editText.setText(null)
        }
        rule.awaitIdle()
        onTyping.assertList(listOf(true, false, false))
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
        onSuggestionReceived.assertSingle(null)
    }

    @Test
    fun `when user types something that's a mention onSuggestionReceived is triggered a real value`() = runTest {
        val state = aMarkdownTextEditorState(initialFocus = true)
        val onSuggestionReceived = EventsRecorder<Suggestion?>()
        rule.setMarkdownTextInput(state = state, onSuggestionReceived = onSuggestionReceived)
        rule.activityRule.scenario.onActivity {
            it.findEditor().setText("@")
            it.findEditor().setText("#")
            it.findEditor().setText("/")
        }
        rule.awaitIdle()
        onSuggestionReceived.assertList(
            listOf(
                // User mention suggestion
                Suggestion(0, 1, SuggestionType.Mention, ""),
                // Room suggestion
                Suggestion(0, 1, SuggestionType.Room, ""),
                // Slash command suggestion
                Suggestion(0, 1, SuggestionType.Command, ""),
            )
        )
    }

    @Test
    fun `when the selection changes in the UI the state is updated`() = runTest {
        val state = aMarkdownTextEditorState(initialText = "Test", initialFocus = true)
        rule.setMarkdownTextInput(state = state)
        rule.activityRule.scenario.onActivity {
            val editor = it.findEditor()
            editor.setSelection(2)
        }
        rule.awaitIdle()
        // Selection is updated
        assertThat(state.selection).isEqualTo(2..2)
    }

    @Test
    fun `when the selection state changes in the view is updated`() = runTest {
        val state = aMarkdownTextEditorState(initialText = "Test", initialFocus = true)
        rule.setMarkdownTextInput(state = state)
        var editor: EditText? = null
        rule.activityRule.scenario.onActivity {
            editor = it.findEditor()
            state.selection = 2..2
        }
        rule.awaitIdle()
        // Selection state is updated
        assertThat(editor?.selectionStart).isEqualTo(2)
        assertThat(editor?.selectionEnd).isEqualTo(2)
    }

    @Test
    fun `when the view focus changes the state is updated`() = runTest {
        val state = aMarkdownTextEditorState(initialText = "Test", initialFocus = false)
        rule.setMarkdownTextInput(state = state)
        rule.activityRule.scenario.onActivity {
            val editor = it.findEditor()
            editor.requestFocus()
        }
        // Focus state is updated
        assertThat(state.hasFocus).isTrue()
    }

    @Test
    fun `inserting a mention replaces the existing text with a span`() = runTest {
        val permalinkParser = FakePermalinkParser(result = { PermalinkData.UserLink(A_SESSION_ID) })
        val state = aMarkdownTextEditorState(initialText = "@", initialFocus = true)
        state.currentSuggestion = Suggestion(0, 1, SuggestionType.Mention, "")
        rule.setMarkdownTextInput(state = state)
        var editor: EditText? = null
        rule.activityRule.scenario.onActivity {
            editor = it.findEditor()
            state.insertSuggestion(
                ResolvedSuggestion.Member(roomMember = aRoomMember()),
                aMentionSpanProvider(permalinkParser),
            )
        }
        rule.awaitIdle()

        // Text is replaced with a placeholder
        assertThat(editor?.editableText.toString()).isEqualTo("@ ")
        // The placeholder contains a MentionSpan
        val mentionSpans = editor?.editableText?.getSpans<MentionSpan>(0, 2).orEmpty()
        assertThat(mentionSpans).isNotEmpty()
    }

    private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setMarkdownTextInput(
        state: MarkdownTextEditorState = aMarkdownTextEditorState(),
        onTyping: (Boolean) -> Unit = {},
        onSuggestionReceived: (Suggestion?) -> Unit = {},
    ) {
        setContent {
            val style = ElementRichTextEditorStyle.composerStyle(hasFocus = state.hasFocus)
            MarkdownTextInput(
                state = state,
                placeholder = "Placeholder",
                placeholderColor = ElementTheme.colors.textSecondary,
                onTyping = onTyping,
                onReceiveSuggestion = onSuggestionReceived,
                richTextEditorStyle = style,
                onSelectRichContent = null,
            )
        }
    }

    private fun ComponentActivity.findEditor(): EditText {
        return window.decorView.findViewWithTag(TestTags.plainTextEditor.value)
    }
}
