/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.libraries.textcomposer.impl.components.markdown

import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
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
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MarkdownTextInputTest {
    @Test
    fun `when user types onTyping is triggered with value 'true'`() = runAndroidComposeUiTest {
        val state = aMarkdownTextEditorState(initialFocus = true)
        val onTyping = EnsureCalledOnceWithParam(expectedParam = true, result = Unit)
        setMarkdownTextInput(state = state, onTyping = onTyping)
        activity!!.findEditor().setText("Test")
        awaitIdle()
        onTyping.assertSuccess()
    }

    @Test
    fun `when user removes text onTyping is triggered with value 'false'`() = runAndroidComposeUiTest {
        val state = aMarkdownTextEditorState(initialFocus = true)
        val onTyping = EventsRecorder<Boolean>()
        setMarkdownTextInput(state = state, onTyping = onTyping)
        val editText = activity!!.findEditor()
        editText.setText("Test")
        editText.setText("")
        editText.setText(null)
        awaitIdle()
        onTyping.assertList(listOf(true, false, false))
    }

    @Test
    fun `when user types something that's not a mention onSuggestionReceived is triggered with 'null'`() = runAndroidComposeUiTest {
        val state = aMarkdownTextEditorState(initialFocus = true)
        val onSuggestionReceived = EventsRecorder<Suggestion?>()
        setMarkdownTextInput(state = state, onSuggestionReceived = onSuggestionReceived)
        activity!!.findEditor().setText("Test")
        awaitIdle()
        onSuggestionReceived.assertSingle(null)
    }

    @Test
    fun `when user types something that's a mention onSuggestionReceived is triggered a real value`() = runAndroidComposeUiTest {
        val state = aMarkdownTextEditorState(initialFocus = true)
        val onSuggestionReceived = EventsRecorder<Suggestion?>()
        setMarkdownTextInput(state = state, onSuggestionReceived = onSuggestionReceived)
        val editor = activity!!.findEditor()
        editor.setText("@")
        editor.setText("#")
        editor.setText("/")
        awaitIdle()
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
    fun `when the selection changes in the UI the state is updated`() = runAndroidComposeUiTest {
        val state = aMarkdownTextEditorState(initialText = "Test", initialFocus = true)
        setMarkdownTextInput(state = state)
        val editor = activity!!.findEditor()
        editor.setSelection(2)
        awaitIdle()
        // Selection is updated
        assertThat(state.selection).isEqualTo(2..2)
    }

    @Test
    fun `when the selection state changes in the view is updated`() = runAndroidComposeUiTest {
        val state = aMarkdownTextEditorState(initialText = "Test", initialFocus = true)
        setMarkdownTextInput(state = state)
        val editor = activity!!.findEditor()
        state.selection = 2..2
        awaitIdle()
        // Selection state is updated
        assertThat(editor.selectionStart).isEqualTo(2)
        assertThat(editor.selectionEnd).isEqualTo(2)
    }

    @Test
    fun `when the view focus changes the state is updated`() = runAndroidComposeUiTest {
        val state = aMarkdownTextEditorState(initialText = "Test", initialFocus = false)
        setMarkdownTextInput(state = state)
        val editor = activity!!.findEditor()
        editor.requestFocus()
        // Focus state is updated
        assertThat(state.hasFocus).isTrue()
    }

    @Test
    fun `inserting a mention replaces the existing text with a span`() = runAndroidComposeUiTest {
        val permalinkParser = FakePermalinkParser(result = { PermalinkData.UserLink(A_SESSION_ID) })
        val state = aMarkdownTextEditorState(initialText = "@", initialFocus = true)
        state.currentSuggestion = Suggestion(0, 1, SuggestionType.Mention, "")
        setMarkdownTextInput(state = state)
        val editor = activity!!.findEditor()
        state.insertSuggestion(
            ResolvedSuggestion.Member(roomMember = aRoomMember()),
            aMentionSpanProvider(permalinkParser),
        )
        awaitIdle()

        // Text is replaced with a placeholder
        assertThat(editor.editableText.toString()).isEqualTo("@ ")
        // The placeholder contains a MentionSpan
        val mentionSpans = editor.editableText?.getSpans<MentionSpan>(0, 2).orEmpty()
        assertThat(mentionSpans).isNotEmpty()
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setMarkdownTextInput(
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
