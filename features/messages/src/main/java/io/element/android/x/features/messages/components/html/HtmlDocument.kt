@file:OptIn(ExperimentalMaterialApi::class)

package io.element.android.x.features.messages.components.html

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowRow
import io.element.android.x.matrix.permalink.PermalinkData
import io.element.android.x.matrix.permalink.PermalinkParser
import kotlinx.coroutines.launch
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

private const val chipId = "chip"

@Composable
fun HtmlDocument(
    document: Document,
    interactionSource: MutableInteractionSource,
    onTextClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HtmlBody(
        body = document.body(),
        modifier = modifier,
        onTextClicked = onTextClicked,
        interactionSource = interactionSource
    )
}

@Composable
private fun HtmlBody(
    body: Element,
    modifier: Modifier = Modifier,
    onTextClicked: () -> Unit,
    interactionSource: MutableInteractionSource,
) {

    @Composable
    fun NodesFlowRode(
        nodes: Iterator<Node>,
        onTextClicked: () -> Unit,
        interactionSource: MutableInteractionSource,
    ) = FlowRow(
        mainAxisSpacing = 2.dp,
        crossAxisSpacing = 8.dp,
    ) {
        var sameRow = true
        while (sameRow && nodes.hasNext()) {
            when (val node = nodes.next()) {
                is TextNode -> {
                    if (!node.isBlank) {
                        Text(text = node.text())
                    }
                }
                is Element -> {
                    if (node.isInline()) {
                        HtmlInline(
                            node,
                            onTextClicked = onTextClicked,
                            interactionSource = interactionSource
                        )
                    } else {
                        HtmlBlock(
                            element = node,
                            onTextClicked = onTextClicked,
                            interactionSource = interactionSource
                        )
                        sameRow = false
                    }
                }
                else -> continue
            }
        }
    }

    Column(modifier = modifier) {
        val nodesIterator = body.childNodes().iterator()
        while (nodesIterator.hasNext()) {
            NodesFlowRode(
                nodes = nodesIterator,
                onTextClicked = onTextClicked,
                interactionSource = interactionSource
            )
        }
    }
}

private fun Element.isInline(): Boolean {
    return when (normalName()) {
        "del" -> true
        "mx-reply" -> false
        else -> !isBlock
    }
}

@Composable
private fun HtmlBlock(
    element: Element,
    modifier: Modifier = Modifier,
    onTextClicked: () -> Unit,
    interactionSource: MutableInteractionSource,
) {
    val blockModifier = modifier
        .padding(top = 4.dp)
    when (element.normalName()) {
        "p" -> HtmlParagraph(
            paragraph = element,
            modifier = blockModifier,
            onTextClicked = onTextClicked,
            interactionSource = interactionSource
        )
        "h1", "h2", "h3", "h4", "h5", "h6" -> HtmlHeading(
            heading = element,
            modifier = blockModifier,
            onTextClicked = onTextClicked,
            interactionSource = interactionSource
        )
        "ol" -> HtmlOrderedList(
            orderedList = element,
            modifier = blockModifier,
            onTextClicked = onTextClicked,
            interactionSource = interactionSource
        )
        "ul" -> HtmlUnorderedList(
            unorderedList = element,
            modifier = blockModifier,
            onTextClicked = onTextClicked,
            interactionSource = interactionSource
        )
        "blockquote" -> HtmlBlockquote(
            blockquote = element,
            modifier = blockModifier,
            onTextClicked = onTextClicked,
            interactionSource = interactionSource
        )
        "pre" -> HtmlPreformatted(element, blockModifier)
        "mx-reply" -> HtmlMxReply(
            mxReply = element,
            modifier = blockModifier,
            onTextClicked = onTextClicked,
            interactionSource = interactionSource
        )
        else -> return
    }
}

@Composable
private fun HtmlInline(
    element: Element,
    modifier: Modifier = Modifier,
    onTextClicked: () -> Unit,
    interactionSource: MutableInteractionSource,
) {
    Box(modifier) {
        val styledText = buildAnnotatedString {
            appendInlineElement(element, MaterialTheme.colorScheme)
        }
        HtmlText(text = styledText, onClick = onTextClicked, interactionSource = interactionSource)
    }
}

@Composable
private fun HtmlPreformatted(pre: Element, modifier: Modifier = Modifier) {
    val isCode = pre.firstElementChild()?.normalName() == "code"
    val backgroundColor =
        if (isCode) MaterialTheme.colorScheme.codeBackground() else Color.Unspecified
    Box(
        modifier
            .background(color = backgroundColor)
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = pre.wholeText(),
            style = TextStyle(fontFamily = FontFamily.Monospace),
        )
    }
}

@Composable
private fun HtmlParagraph(
    paragraph: Element,
    modifier: Modifier = Modifier,
    onTextClicked: () -> Unit,
    interactionSource: MutableInteractionSource,
) {
    Box(modifier) {
        val styledText = buildAnnotatedString {
            appendInlineChildrenElements(paragraph.childNodes(), MaterialTheme.colorScheme)
        }
        HtmlText(text = styledText, onClick = onTextClicked, interactionSource = interactionSource)
    }
}

@Composable
private fun HtmlBlockquote(
    blockquote: Element,
    modifier: Modifier = Modifier,
    onTextClicked: () -> Unit,
    interactionSource: MutableInteractionSource,
) {
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
        HtmlText(text = text, onClick = onTextClicked, interactionSource = interactionSource)
    }
}


@Composable
private fun HtmlHeading(
    heading: Element,
    modifier: Modifier = Modifier,
    onTextClicked: () -> Unit,
    interactionSource: MutableInteractionSource,
) {
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
        HtmlText(
            text = text,
            style = style,
            onClick = onTextClicked,
            interactionSource = interactionSource
        )
    }
}

@Composable
private fun HtmlMxReply(
    mxReply: Element,
    modifier: Modifier = Modifier,
    onTextClicked: () -> Unit,
    interactionSource: MutableInteractionSource,
) {
    val blockquote = mxReply.childNodes().firstOrNull() ?: return
    val shape = RoundedCornerShape(12.dp)
    Surface(
        modifier = modifier
            .padding(bottom = 4.dp)
            .offset(x = -(8.dp)),
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
        HtmlText(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            onClick = onTextClicked,
            interactionSource = interactionSource
        )
    }
}

@Composable
private fun HtmlOrderedList(
    orderedList: Element,
    modifier: Modifier = Modifier,
    onTextClicked: () -> Unit,
    interactionSource: MutableInteractionSource,
) {
    var number = 1
    val delimiter = "."
    HtmlListItems(
        list = orderedList,
        modifier = modifier,
        onTextClicked = onTextClicked,
        interactionSource = interactionSource
    ) {
        val text = buildAnnotatedString {
            append("${number++}$delimiter ${it.text()}")
        }
        HtmlText(text = text, onClick = onTextClicked, interactionSource = interactionSource)
    }
}

@Composable
private fun HtmlUnorderedList(
    unorderedList: Element,
    modifier: Modifier = Modifier,
    onTextClicked: () -> Unit,
    interactionSource: MutableInteractionSource,
) {
    val marker = "・"
    HtmlListItems(
        list = unorderedList,
        modifier = modifier,
        onTextClicked = onTextClicked,
        interactionSource = interactionSource
    ) {
        val text = buildAnnotatedString {
            append("$marker ${it.text()}")
        }
        HtmlText(text = text, onClick = onTextClicked, interactionSource = interactionSource)
    }
}


@Composable
private fun HtmlListItems(
    list: Element,
    modifier: Modifier = Modifier,
    onTextClicked: () -> Unit,
    interactionSource: MutableInteractionSource,
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
                        modifier = modifier.padding(start = 4.dp),
                        onTextClicked = onTextClicked,
                        interactionSource = interactionSource
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
            appendLink(element)
        }
        else -> {
            appendInlineChildrenElements(element.childNodes(), colors)
        }
    }

}

private fun AnnotatedString.Builder.appendLink(link: Element) {
    val uriString = link.attr("href")
    val permalinkData = PermalinkParser.parse(uriString)
    when (permalinkData) {
        is PermalinkData.FallbackLink -> {
            pushStringAnnotation(tag = "link", annotation = link.ownText())
            withStyle(
                style = SpanStyle(color = Color.Blue)
            ) {
                append(link.ownText())
            }
            pop()
        }
        is PermalinkData.RoomEmailInviteLink -> {
            appendInlineContent(chipId, link.ownText())
        }
        is PermalinkData.RoomLink -> {
            appendInlineContent(chipId, link.ownText())
        }
        is PermalinkData.UserLink -> {
            appendInlineContent(chipId, link.ownText())
        }
    }
}

@Composable
private fun HtmlText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource,
) {
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val pressIndicator = Modifier.pointerInput(onClick) {
        detectTapGestures { offset ->
            layoutResult.value?.let { layoutResult ->
                val position = layoutResult.getOffsetForPosition(offset)
                val linkAnnotations = text.getStringAnnotations("link", position, position)
                if (linkAnnotations.isEmpty()) {
                    onClick()
                    coroutineScope.launch {
                        val pressInteraction = PressInteraction.Press(offset)
                        interactionSource.emit(pressInteraction)
                        interactionSource.emit(PressInteraction.Release(pressInteraction))
                    }
                } else {
                    uriHandler.openUri(linkAnnotations.first().item)
                }
            }

        }
    }
    val inlineContentMap = emptyMap<String, InlineTextContent>()
    Text(
        text = text,
        modifier = modifier.then(pressIndicator),
        style = style,
        onTextLayout = {
            layoutResult.value = it
        },
        inlineContent = inlineContentMap
    )
}

