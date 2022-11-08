package io.element.android.x.textcomposer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.isVisible
import io.element.android.x.element.resources.R as ElementR

@Composable
fun TextComposer(
    callback: Callback,
    modifier: Modifier = Modifier,
){
    AndroidView(
        modifier = modifier,
        factory = { context ->
            RichTextComposerLayout(context).apply {
                // Sets up listeners for View -> Compose communication
                this.callback = callback
                val messageComposerView = (this as MessageComposerView)
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
        }
    )
}

private fun setupComposer(messageComposerView: MessageComposerView) {
    messageComposerView.editText.setHint(ElementR.string.room_message_placeholder)
    messageComposerView.emojiButton?.isVisible = true
    messageComposerView.sendButton.isVisible = true
}
