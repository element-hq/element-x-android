package io.element.android.x.features.messages.components.html

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

@Composable
fun HtmlDocument(document: Document, modifier: Modifier = Modifier) {
    HtmlBody(body = document.body(), modifier = modifier)
}

@Composable
private fun HtmlBody(body: Element, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
    ) {
        for (node in body.childNodes()) {
            when (node) {
                is TextNode -> {
                    if (!node.isBlank) {
                        Text(text = node.text())
                    }
                }
                is Element -> {
                    HtmlBlock(element = node)
                }
                else -> {
                    continue
                }
            }
        }
    }
}

@Composable
private fun HtmlBlock(element: Element, modifier: Modifier = Modifier) {
    val blockModifier = modifier
        .padding(top = 4.dp)
    when (element.normalName()) {
        "p" -> HtmlParagraph(element, blockModifier)
        "h1", "h2", "h3", "h4", "h5", "h6" -> HtmlHeading(element, blockModifier)
        "ol" -> HtmlOrderedList(element, blockModifier)
        "ul" -> HtmlUnorderedList(element, blockModifier)
        "blockquote" -> HtmlBlockquote(element, blockModifier)
        "pre" -> HtmlPreformatted(element, blockModifier)
        "mx-reply" -> HtmlMxReply(element, blockModifier)
        // fallback to html inline
        else -> HtmlInline(element, modifier)
    }
}

@Composable
private fun HtmlInline(element: Element, modifier: Modifier = Modifier) {
    Box(modifier.padding(start = 8.dp)) {
        val styledText = buildAnnotatedString {
            appendInlineElement(element, MaterialTheme.colorScheme)
        }
        Text(styledText)
    }
}

@Composable
private fun HtmlPreformatted(pre: Element, modifier: Modifier = Modifier) {
    val isCode = pre.firstElementChild()?.normalName() == "code"
    val backgroundColor =
        if (isCode) MaterialTheme.colorScheme.codeBackground() else Color.Unspecified
    Box(modifier.background(color = backgroundColor)) {
        Text(
            text = pre.wholeText(),
            style = TextStyle(fontFamily = FontFamily.Monospace),
        )
    }
}

@Composable
private fun HtmlParagraph(paragraph: Element, modifier: Modifier = Modifier) {
    Box(modifier) {
        val styledText = buildAnnotatedString {
            appendInlineChildrenElements(paragraph.childNodes(), MaterialTheme.colorScheme)
        }
        Text(styledText)
    }
}

@Composable
private fun HtmlBlockquote(blockquote: Element, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.onBackground
    Box(
        modifier = modifier
            .drawBehind {
                drawLine(
                    color = color,
                    strokeWidth = 2f,
                    start = Offset(12.dp.value, 0f),
                    end = Offset(12.dp.value, size.height)
                )
            }
            .padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
    ) {
        val text = buildAnnotatedString {
            withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                appendInlineChildrenElements(blockquote.childNodes(), MaterialTheme.colorScheme)
            }
        }
        Text(text)
    }
}


@Composable
private fun HtmlHeading(heading: Element, modifier: Modifier = Modifier) {
    val style = when (heading.normalName()) {
        "h1" -> MaterialTheme.typography.headlineLarge.copy(fontSize = 30.sp)
        "h2" -> MaterialTheme.typography.headlineLarge.copy(fontSize = 26.sp)
        "h3" -> MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp)
        "h4" -> MaterialTheme.typography.headlineMedium.copy(fontSize = 18.sp)
        "h5" -> MaterialTheme.typography.headlineSmall.copy(fontSize = 14.sp)
        "h6" -> MaterialTheme.typography.headlineSmall.copy(fontSize = 12.sp)
        else -> {
            return
        }
    }
    Box(modifier) {
        val text = buildAnnotatedString {
            appendInlineChildrenElements(heading.childNodes(), MaterialTheme.colorScheme)
        }
        Text(text, style = style)
    }
}

@Composable
private fun HtmlMxReply(mxReply: Element, modifier: Modifier = Modifier) {
    val blockquote = mxReply.childNodes().firstOrNull() ?: return
    val shape = RoundedCornerShape(12.dp)
    Surface(
        modifier = modifier.offset(x = -(8.dp)),
        color = MaterialTheme.colorScheme.background,
        shape = shape,
    ) {
        val text = buildAnnotatedString {
            for (blockquoteNode in blockquote.childNodes()) {
                when (blockquoteNode) {
                    is TextNode -> {
                        withStyle(
                            style = SpanStyle(
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            append(blockquoteNode.text())
                        }
                    }
                    is Element -> {
                        when (blockquoteNode.normalName()) {
                            "br" -> {
                                append('\n')
                            }
                            "a" -> {
                                append(blockquoteNode.ownText())
                            }
                        }
                    }
                }
            }
        }
        Text(text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
    }
}

@Composable
private fun HtmlOrderedList(unorderedList: Element, modifier: Modifier = Modifier) {
    var number = 1
    val delimiter = "."
    HtmlListItems(unorderedList, modifier = modifier) {
        val text = buildAnnotatedString {
            append("${number++}$delimiter ${it.text()}")
        }
        Text(text)
    }
}

@Composable
private fun HtmlUnorderedList(unorderedList: Element, modifier: Modifier = Modifier) {
    val marker = "・"
    HtmlListItems(unorderedList, modifier = modifier) {
        val text = buildAnnotatedString {
            append("$marker ${it.text()}")
        }
        Text(text)
    }
}


@Composable
private fun HtmlListItems(
    list: Element,
    modifier: Modifier = Modifier,
    content: @Composable (node: TextNode) -> Unit
) {
    Column(modifier = modifier) {
        for (node in list.children()) {
            for (innerNode in node.childNodes()) {
                when (innerNode) {
                    is TextNode -> {
                        if (!innerNode.isBlank) content(innerNode)
                    }
                    is Element -> HtmlBlock(
                        element = innerNode,
                        modifier = modifier.padding(start = 4.dp)
                    )
                }
            }

        }
    }
}

private fun ColorScheme.codeBackground(): Color {
    return background.copy(alpha = 0.3f)
}

private fun AnnotatedString.Builder.appendInlineChildrenElements(
    childNodes: List<Node>,
    colors: ColorScheme
) {

    for (node in childNodes) {
        when (node) {
            is TextNode -> {
                append(node.text())
            }
            is Element -> {
                appendInlineElement(node, colors)
            }
        }
    }
}


private fun AnnotatedString.Builder.appendInlineElement(element: Element, colors: ColorScheme) {
    when (element.normalName()) {
        "br" -> {
            append('\n')
        }
        "code" -> {
            withStyle(
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    background = colors.codeBackground()
                ).toSpanStyle()
            ) {
                appendInlineChildrenElements(element.childNodes(), colors)
            }
        }
        "del" -> {
            withStyle(style = SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                appendInlineChildrenElements(element.childNodes(), colors)
            }
        }
        "em" -> {
            withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                appendInlineChildrenElements(element.childNodes(), colors)
            }
        }
        "strong" -> {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                appendInlineChildrenElements(element.childNodes(), colors)
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
        else -> {
            appendInlineChildrenElements(element.childNodes(), colors)
        }
    }
}
