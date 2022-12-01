package io.element.android.x.features.messages.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.x.features.messages.components.html.HtmlDocument
import io.element.android.x.features.messages.model.content.MessagesTimelineItemTextBasedContent

@Composable
fun MessagesTimelineItemTextView(
    content: MessagesTimelineItemTextBasedContent,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    onTextClicked: () -> Unit,
    onTextLongClicked: () -> Unit,
) {
    val htmlDocument = content.htmlDocument
    if (htmlDocument != null) {
        HtmlDocument(
            document = htmlDocument,
            modifier = modifier,
            onTextClicked = onTextClicked,
            onTextLongClicked = onTextLongClicked,
            interactionSource = interactionSource
        )
    } else {
        Box(modifier) {
            Text(text = content.body)
        }
    }
}