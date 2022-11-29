package io.element.android.x.features.messages.html

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

@Composable
fun HtmlDocument(document: Document, modifier: Modifier = Modifier) {
    HtmlBody(body = document.body(), modifier = modifier)
}

@Composable
fun HtmlBody(body: Element, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
    ) {
        for (node in body.childNodes()) {
            when (node) {
                is TextNode -> {
                    if (!node.isBlank) {
                        Text(
                            text = node.text(),
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
                is Element -> {
                    HtmlBlock(node)
                }
                else -> {
                    return
                }
            }
        }
    }
}

@Composable
fun HtmlBlock(element: Element, modifier: Modifier = Modifier) {
    val blockModifier = modifier
        .fillMaxWidth()
        .padding(top = 4.dp)
    when (element.normalName()) {
        "p" -> HtmlParagraph(element, blockModifier)
        "h1", "h2", "h3", "h4", "h5", "h6" -> HtmlHeading(heading = element, blockModifier)
        "ol" -> HtmlOrderedList(element, blockModifier)
        "ul" -> HtmlUnorderedList(element, blockModifier)
        "blockquote" -> Column {
            for (e in element.children()) {
                HtmlBlock(element = e)
            }
        }
    }
}


@Composable
fun HtmlHeading(heading: Element, modifier: Modifier = Modifier) {
    val style = when (heading.normalName()) {
        "h1" -> MaterialTheme.typography.h1
        "h2" -> MaterialTheme.typography.h2
        "h3" -> MaterialTheme.typography.h3
        "h4" -> MaterialTheme.typography.h4
        "h5" -> MaterialTheme.typography.h5
        "h6" -> MaterialTheme.typography.h6
        else -> {
            return
        }
    }
    Box(modifier) {
        val text = buildAnnotatedString {
            appendInlineChildrenElements(heading.childNodes())
        }
        HtmlText(text, style)
    }
}


@Composable
private fun HtmlOrderedList(unorderedList: Element, modifier: Modifier = Modifier) {
    var number = 0
    val delimiter = "."
    HtmlListItems(unorderedList, modifier = modifier) {
        val text = buildAnnotatedString {
            pushStyle(MaterialTheme.typography.body1.toSpanStyle())
            append("${number++}$delimiter ")
            appendInlineElements(it)
            pop()
        }
        HtmlText(text, MaterialTheme.typography.body1, modifier)
    }
}

@Composable
private fun HtmlUnorderedList(unorderedList: Element, modifier: Modifier = Modifier) {
    val marker = "-"
    HtmlListItems(unorderedList, modifier = modifier) {
        val text = buildAnnotatedString {
            pushStyle(MaterialTheme.typography.body1.toSpanStyle())
            append("$marker ")
            appendInlineElements(it)
            pop()
        }
        HtmlText(text, MaterialTheme.typography.body1, modifier)
    }
}


@Composable
fun HtmlListItems(
    list: Element,
    modifier: Modifier = Modifier,
    content: @Composable (node: Element) -> Unit
) {
    if (list.children().isEmpty()) return
    Column(modifier = modifier) {
        val children = list.children().iterator()
        var listItem = children.next()
        while (listItem != null) {
            val innerChildren = listItem.children().iterator()
            var child = if (innerChildren.hasNext()) {
                innerChildren.next()
            } else {
                null
            }
            while (child != null) {
                when (child.normalName()) {
                    "ul" -> HtmlUnorderedList(child, modifier)
                    "ol" -> HtmlOrderedList(child, modifier)
                    else -> content(child)
                }
                child = if (innerChildren.hasNext()) {
                    innerChildren.next()
                } else {
                    null
                }
            }
            listItem = if (children.hasNext()) {
                children.next()
            } else {
                null
            }
        }
    }
}

private fun AnnotatedString.Builder.appendInlineChildrenElements(childNodes: List<Node>) {
    for (node in childNodes) {
        when (node) {
            is TextNode -> {
                append(node.text())
            }
            is Element -> {
                appendInlineElements(node)
            }
        }
    }
}

private fun AnnotatedString.Builder.appendInlineElements(element: Element) {
    when (element.normalName()) {
        "br" -> {
            append('\n')
        }
        "del" -> {
            withStyle(style = SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                appendInlineChildrenElements(element.childNodes())
            }
        }
        "em" -> {
            withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                appendInlineChildrenElements(element.childNodes())
            }
        }
        "strong" -> {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                appendInlineChildrenElements(element.childNodes())
            }
        }
        "a" -> {
            val href = element.attr("href")
            pushStringAnnotation(tag = "url", annotation = href)
            withStyle(
                style = SpanStyle(
                    color = Color.Blue,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(element.ownText())
            }
            pop()
        }
    }
}

@Composable
fun HtmlText(text: AnnotatedString, style: TextStyle, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    Text(text = text,
        modifier.pointerInput(Unit) {
            detectTapGestures { offset ->
                layoutResult.value?.let { layoutResult ->
                    val position = layoutResult.getOffsetForPosition(offset)
                    text.getStringAnnotations(position, position)
                        .firstOrNull()
                        ?.let { sa ->
                            if (sa.tag == "url") {
                                uriHandler.openUri(sa.item)
                            }
                        }
                }
            }
        },
        style = style,
        inlineContent = mapOf(
            "imageUrl" to InlineTextContent(
                Placeholder(style.fontSize, style.fontSize, PlaceholderVerticalAlign.Bottom)
            ) {
                Image(
                    painter = rememberImagePainter(
                        data = it,
                    ),
                    contentDescription = null,
                    modifier = modifier,
                    alignment = Alignment.Center
                )

            }
        ),
        onTextLayout = { layoutResult.value = it }
    )
}

@Composable
private fun HtmlParagraph(paragraph: Element, modifier: Modifier = Modifier) {
    Box(modifier) {
        val styledText = buildAnnotatedString {
            pushStyle(MaterialTheme.typography.body1.toSpanStyle())
            appendInlineChildrenElements(paragraph.childNodes())
            pop()
        }
        HtmlText(styledText, MaterialTheme.typography.body1)
    }
}