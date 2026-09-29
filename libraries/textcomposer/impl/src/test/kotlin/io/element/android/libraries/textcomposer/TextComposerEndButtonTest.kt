/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Copyright 2026 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.libraries.textcomposer

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.libraries.textcomposer.model.VoiceMessageState
import io.element.android.libraries.textcomposer.model.aTextEditorStateMarkdown
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.robolectric.RobolectricTest
import io.element.android.wysiwyg.display.TextDisplay
import org.junit.Test

class TextComposerEndButtonTest : RobolectricTest() {
    @Test
    fun `editing a message with text shows the send edited message button`() = runAndroidComposeUiTest {
        setTextComposer(text = "Some text", composerMode = aMessageComposerModeEdit())
        onNodeWithContentDescription(getString(CommonStrings.action_send_edited_message)).assertIsDisplayed()
    }

    @Test
    fun `emptying the composer while editing keeps the send edited message button`() = runAndroidComposeUiTest {
        setTextComposer(text = "", composerMode = aMessageComposerModeEdit())
        onNodeWithContentDescription(getString(CommonStrings.action_send_edited_message)).assertIsDisplayed()
        onNodeWithContentDescription(getString(CommonStrings.a11y_voice_message_record)).assertDoesNotExist()
    }

    @Test
    fun `emptying the composer while editing a caption keeps the send edited message button`() = runAndroidComposeUiTest {
        setTextComposer(text = "", composerMode = aMessageComposerModeEditCaption(content = "An existing caption"))
        onNodeWithContentDescription(getString(CommonStrings.action_send_edited_message)).assertIsDisplayed()
        onNodeWithContentDescription(getString(CommonStrings.a11y_voice_message_record)).assertDoesNotExist()
    }

    @Test
    fun `an empty composer outside of edition still offers to record a voice message`() = runAndroidComposeUiTest {
        setTextComposer(text = "", composerMode = MessageComposerMode.Normal)
        onNodeWithContentDescription(getString(CommonStrings.a11y_voice_message_record)).assertIsDisplayed()
    }

    private fun AndroidComposeUiTest<ComponentActivity>.getString(resId: Int): String {
        return activity!!.getString(resId)
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setTextComposer(
        text: String,
        composerMode: MessageComposerMode,
    ) {
        setContent {
            TextComposer(
                state = aTextEditorStateMarkdown(initialText = text, initialFocus = true),
                voiceMessageState = VoiceMessageState.Idle,
                composerMode = composerMode,
                onRequestFocus = {},
                onSendMessage = {},
                onResetComposerMode = {},
                onAddAttachment = {},
                onDismissTextFormatting = {},
                onVoiceRecorderEvent = {},
                onVoicePlayerEvent = {},
                onSendVoiceMessage = {},
                onDeleteVoiceMessage = {},
                onError = {},
                onTyping = {},
                onReceiveSuggestion = {},
                onSelectRichContent = null,
                resolveMentionDisplay = { _, _ -> TextDisplay.Plain },
                resolveAtRoomMentionDisplay = { TextDisplay.Plain },
            )
        }
    }
}
