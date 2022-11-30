package io.element.android.x.features.messages.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.x.features.messages.components.html.HtmlDocument
import io.element.android.x.features.messages.model.content.MessagesTimelineItemTextBasedContent

@Composable
fun MessagesTimelineItemTextView(
    content: MessagesTimelineItemTextBasedContent,
    modifier: Modifier = Modifier
) {
    val htmlDocument = content.htmlDocument
    if (htmlDocument != null) {
        HtmlDocument(document = htmlDocument, modifier)
    } else {
        Box(modifier) {
            Text(text = content.body)
        }
    }
}