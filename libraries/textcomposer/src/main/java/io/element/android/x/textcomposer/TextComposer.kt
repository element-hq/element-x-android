package io.element.android.x.textcomposer

import android.graphics.Color
import android.net.Uri
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import io.element.android.x.element.resources.R as ElementR

@Composable
fun TextComposer(
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    fullscreen: Boolean,
    onFullscreenToggle: () -> Unit,
    composerMode: MessageComposerMode,
    onCloseSpecialMode: () -> Unit,
    onComposerTextChange: (CharSequence) -> Unit,
    composerCanSendMessage: Boolean,
    composerText: String?,
) {
    if (LocalInspectionMode.current) {
        FakeComposer(modifier)
        return
    }

    val isInDarkMode = isSystemInDarkTheme()
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

@Composable
private fun FakeComposer(modifier: Modifier) {
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
            fontSize = 20.sp
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
    editText.setHint(ElementR.string.room_message_placeholder)
    emojiButton?.isVisible = true
    sendButton.isVisible = true
    editText.maxLines = MessageComposerView.MAX_LINES_WHEN_COLLAPSED
    renderComposerMode(composerMode)
}

@Preview
@Composable
fun TextComposerPreview() {
    TextComposer(
        onSendMessage = {},
        fullscreen = false,
        onFullscreenToggle = { },
        onComposerTextChange = {},
        composerMode = MessageComposerMode.Normal(""),
        onCloseSpecialMode = {},
        composerCanSendMessage = true,
        composerText = "Message",
    )
}
