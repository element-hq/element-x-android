package io.element.android.x.textcomposer

import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.widget.AppCompatEditText
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    onComposerTextChange: (CharSequence) -> Unit,
    composerCanSendMessage: Boolean,
    composerText: CharSequence?,
) {
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
                    }

                    override fun onSendMessage(text: CharSequence) {
                        // TODO The Wysiwyg team is working to be able to get a markdown version of the text
                        // For now we send only the plain text. `formattedText` is Html.
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
                (this as MessageComposerView).apply {
                    setup(fullscreen, isInDarkMode)
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
            messageComposerView.toggleFullScreen(fullscreen)
            messageComposerView.sendButton.isInvisible = !composerCanSendMessage
            messageComposerView.setTextIfDifferent(composerText ?: "")
        }
    )
}

private fun MessageComposerView.setup(fullscreen: Boolean, isDarkMode: Boolean) {
    val editTextColor = if(isDarkMode){
        Color.WHITE
    }else{
        Color.BLACK
    }
    editText.setTextColor(editTextColor)
    editText.setHintTextColor(editTextColor)
    toggleFullScreen(fullscreen)
    editText.setHint(ElementR.string.room_message_placeholder)
    emojiButton?.isVisible = true
    sendButton.isVisible = true
}
