/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.libraries.textcomposer

import android.graphics.Color
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewDefaults
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.R as StringR

@Composable
fun TextComposer(
    fullscreen: Boolean,
    composerText: String?,
    composerMode: MessageComposerMode,
    composerCanSendMessage: Boolean,
    isInDarkMode: Boolean,
    modifier: Modifier = Modifier,
    onSendMessage: (String) -> Unit = {},
    onFullscreenToggle: () -> Unit = {},
    onCloseSpecialMode: () -> Unit = {},
    onComposerTextChange: (CharSequence) -> Unit = {},
) {
    if (LocalInspectionMode.current) {
        FakeComposer(modifier)
    } else {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                RichTextComposerLayout(context).apply {
                    // Sets up listeners for View -> Compose communication
                    this.callback = object : Callback {
                        override fun onRichContentSelected(contentUri: Uri): Boolean {
                            return false
                        }

                        override fun onTextChanged(text: CharSequence) {
                            onComposerTextChange(text)
                        }

                        override fun onCloseRelatedMessage() {
                            onCloseSpecialMode()
                        }

                        override fun onSendMessage(text: CharSequence) {
                            // text contains markdown.
                            onSendMessage(text.toString())
                        }

                        override fun onAddAttachment() {
                        }

                        override fun onExpandOrCompactChange() {
                        }

                        override fun onFullScreenModeChanged() {
                            onFullscreenToggle()
                        }
                    }
                    setFullScreen(fullscreen, animated = false, manageKeyboard = true)
                    (this as MessageComposerView).apply {
                        setup(isInDarkMode, composerMode)
                    }
                }
            },
            update = { view ->
                // View's been inflated or state read in this block has been updated
                // Add logic here if necessary

                // As selectedItem is read here, AndroidView will recompose
                // whenever the state changes
                // Example of Compose -> View communication
                val messageComposerView = (view as MessageComposerView)
                view.setFullScreen(fullscreen, animated = false, manageKeyboard = false)
                messageComposerView.renderComposerMode(composerMode)
                messageComposerView.sendButton.isInvisible = !composerCanSendMessage
                messageComposerView.setTextIfDifferent(composerText ?: "")
                messageComposerView.editText.requestFocus()
            }
        )
    }
}

@Composable
private fun FakeComposer(
    modifier: Modifier = Modifier,
) {
    // AndroidView is not Available in this mode, just render a Text
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.Center),
            textAlign = TextAlign.Center,
            text = "Composer Preview",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

private fun MessageComposerView.setup(isDarkMode: Boolean, composerMode: MessageComposerMode) {
    val editTextColor = if (isDarkMode) {
        Color.WHITE
    } else {
        Color.BLACK
    }
    editText.setTextColor(editTextColor)
    editText.setHintTextColor(editTextColor)
    editText.setHint(StringR.string.room_message_placeholder)
    emojiButton?.isVisible = true
    sendButton.isVisible = true
    editText.maxLines = MessageComposerView.MAX_LINES_WHEN_COLLAPSED
    renderComposerMode(composerMode)
}

@PreviewDefaults
@Composable
internal fun TextComposerPreview() = ElementPreview {
    TextComposer(
        onSendMessage = {},
        fullscreen = false,
        onFullscreenToggle = { },
        onComposerTextChange = {},
        composerMode = MessageComposerMode.Normal(""),
        onCloseSpecialMode = {},
        composerCanSendMessage = true,
        composerText = "Message",
        isInDarkMode = true,
    )
}
