package io.element.android.x.features.messages.components

import android.text.SpannableString
import android.text.style.URLSpan
import android.text.util.Linkify
import android.text.util.Linkify.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.core.text.util.LinkifyCompat
import io.element.android.x.designsystem.components.ClickableLinkText
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
        val uriHandler = LocalUriHandler.current
        Box(modifier) {
            val linkStyle = SpanStyle(
                color = Color.Blue,
            )
            val styledText = remember(content.body) { content.body.linkify(linkStyle) }
            ClickableLinkText(
                text = styledText,
                linkAnnotationTag = "URL",
                onClick = onTextClicked,
                onLongClick = onTextLongClicked,
                interactionSource = interactionSource
            )
        }
    }
}

private fun String.linkify(
    linkStyle: SpanStyle,
) = buildAnnotatedString {
    append(this@linkify)
    val spannable = SpannableString(this@linkify)
    LinkifyCompat.addLinks(spannable, WEB_URLS or PHONE_NUMBERS)

    val spans = spannable.getSpans(0, spannable.length, URLSpan::class.java)
    for (span in spans) {
        val start = spannable.getSpanStart(span)
        val end = spannable.getSpanEnd(span)
        addStyle(
            start = start,
            end = end,
            style = linkStyle,
        )
        addStringAnnotation(
            tag = "URL",
            annotation = span.url,
            start = start,
            end = end
        )
    }
}