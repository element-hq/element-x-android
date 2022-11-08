package io.element.android.x.textcomposer

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.isVisible
import io.element.android.x.element.resources.R as ElementR

@Composable
fun TextComposer(
    onSendMessage: (CharSequence) -> Unit,
    modifier: Modifier = Modifier,
    fullscreen: Boolean,
    onFullscreenToggle: () -> Unit,
) {
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
                    }

                    override fun onCloseRelatedMessage() {
                    }

                    override fun onSendMessage(text: CharSequence) {
                        onSendMessage(text)
                    }

                    override fun onAddAttachment() {
                    }

                    override fun onExpandOrCompactChange() {
                    }

                    override fun onFullScreenModeChanged() {
                        onFullscreenToggle()
                    }

                }
                val messageComposerView = (this as MessageComposerView)
                messageComposerView.toggleFullScreen(fullscreen)
                setupComposer(messageComposerView)
            }
        },
        update = { view ->
            // View's been inflated or state read in this block has been updated
            // Add logic here if necessary

            // As selectedItem is read here, AndroidView will recompose
            // whenever the state changes
            // Example of Compose -> View communication
            val messageComposerView = (view as MessageComposerView)
            messageComposerView.toggleFullScreen(fullscreen)
            messageComposerView.sendButton.isVisible = true
        }
    )
}

private fun setupComposer(messageComposerView: MessageComposerView) {
    messageComposerView.editText.setHint(ElementR.string.room_message_placeholder)
    messageComposerView.emojiButton?.isVisible = true
    messageComposerView.sendButton.isVisible = true
}
