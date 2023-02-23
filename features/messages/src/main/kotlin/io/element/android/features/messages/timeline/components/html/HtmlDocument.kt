/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.timeline.components.html

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.flowlayout.FlowRow
import io.element.android.libraries.designsystem.LinkColor
import io.element.android.libraries.designsystem.components.ClickableLinkText
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.permalink.PermalinkData
import io.element.android.libraries.matrix.permalink.PermalinkParser
import kotlinx.collections.immutable.persistentMapOf
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

private const val chipId = "chip"

@Composable
fun HtmlDocument(
    document: Document,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    onTextClicked: () -> Unit = {},
    onTextLongClicked: () -> Unit = {},
) {
    HtmlBody(
        body = document.body(),
        interactionSource = interactionSource,
        modifier = modifier,
        onTextClicked = onTextClicked,
        onTextLongClicked = onTextLongClicked,
    )
}

@Composable
private fun HtmlBody(
    body: Element,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    onTextClicked: () -> Unit = {},
    onTextLongClicked: () -> Unit = {},
) {
    @Composable
    fun NodesFlowRode(
        nodes: Iterator<Node>,
        interactionSource: MutableInteractionSource,
        onTextClicked: () -> Unit = {},
        onTextLongClicked: () -> Unit = {},
    ) = FlowRow(
        mainAxisSpacing = 2.dp,
        crossAxisSpacing = 8.dp,
    ) {
        var sameRow = true
        while (sameRow && nodes.hasNext()) {
            when (val node = nodes.next()) {
                is TextNode -> {
                    if (!node.isBlank) {
                        Text(
                            text = node.text(),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                is Element -> {
                    if (node.isInline()) {
                        HtmlInline(
                            node,
                            onTextClicked = onTextClicked,
                            onTextLongClicked = onTextLongClicked,
                            interactionSource = interactionSource
                        )
                    } else {
                        HtmlBlock(
                            element = node,
                            onTextClicked = onTextClicked,
                            onTextLongClicked = onTextLongClicked,
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
                interactionSource = interactionSource,
                onTextClicked = onTextClicked,
                onTextLongClicked = onTextLongClicked,
            )
        }
    }
}

private fun Element.isInline(): Boolean {
    return when (tagName().lowercase()) {
        "del" -> true
        "mx-reply" -> false
        else -> !isBlock
    }
}

@Composable
private fun HtmlBlock(
    element: Element,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    onTextClicked: () -> Unit = {},
    onTextLongClicked: () -> Unit = {},
) {
    val blockModifier = modifier
        .padding(top = 4.dp)
    when (element.tagName().lowercase()) {
        "p" -> HtmlParagraph(
            paragraph = element,
            modifier = blockModifier,
            onTextClicked = onTextClicked,
            onTextLongClicked = onTextLongClicked,
            interactionSource = interactionSource
        )
        "h1", "h2", "h3", "h4", "h5", "h6" -> HtmlHeading(
            heading = element,
            modifier = blockModifier,
            onTextClicked = onTextClicked,
            onTextLongClicked = onTextLongClicked,
            interactionSource = interactionSource
        )
        "ol" -> HtmlOrderedList(
            orderedList = element,
            modifier = blockModifier,
            onTextClicked = onTextClicked,
            onTextLongClicked = onTextLongClicked,
            interactionSource = interactionSource
        )
        "ul" -> HtmlUnorderedList(
            unorderedList = element,
            modifier = blockModifier,
            onTextClicked = onTextClicked,
            onTextLongClicked = onTextLongClicked,
            interactionSource = interactionSource
        )
        "blockquote" -> HtmlBlockquote(
            blockquote = element,
            modifier = blockModifier,
            onTextClicked = onTextClicked,
            onTextLongClicked = onTextLongClicked,
            interactionSource = interactionSource
        )
        "pre" -> HtmlPreformatted(element, blockModifier)
        "mx-reply" -> HtmlMxReply(
            mxReply = element,
            modifier = blockModifier,
            onTextClicked = onTextClicked,
            onTextLongClicked = onTextLongClicked,
            interactionSource = interactionSource
        )
        else -> return
    }
}

@Composable
private fun HtmlInline(
    element: Element,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    onTextClicked: () -> Unit = {},
    onTextLongClicked: () -> Unit = {},
) {
    Box(modifier) {
        val styledText = buildAnnotatedString {
            appendInlineElement(element, MaterialTheme.colorScheme)
        }
        HtmlText(
            text = styledText,
            onClick = onTextClicked,
            onLongClick = onTextLongClicked,
            interactionSource = interactionSource
        )
    }
}

@Composable
private fun HtmlPreformatted(
    pre: Element,
    modifier: Modifier = Modifier
) {
    val isCode = pre.firstElementChild()?.tagName()?.lowercase() == "code"
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
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun HtmlParagraph(
    paragraph: Element,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    onTextClicked: () -> Unit = {},
    onTextLongClicked: () -> Unit = {},
) {
    Box(modifier) {
        val styledText = buildAnnotatedString {
            appendInlineChildrenElements(paragraph.childNodes(), MaterialTheme.colorScheme)
        }
        HtmlText(
            text = styledText, onClick = onTextClicked,
            onLongClick = onTextLongClicked, interactionSource = interactionSource
        )
    }
}

@Composable
private fun HtmlBlockquote(
    blockquote: Element,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    onTextClicked: () -> Unit = {},
    onTextLongClicked: () -> Unit = {},
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
        HtmlText(
            text = text, onClick = onTextClicked,
            onLongClick = onTextLongClicked, interactionSource = interactionSource
        )
    }
}

@Composable
private fun HtmlHeading(
    heading: Element,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    onTextClicked: () -> Unit = {},
    onTextLongClicked: () -> Unit = {},
) {
    val style = when (heading.tagName().lowercase()) {
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
            onLongClick = onTextLongClicked,
            interactionSource = interactionSource
        )
    }
}

@Composable
private fun HtmlMxReply(
    mxReply: Element,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    onTextClicked: () -> Unit = {},
    onTextLongClicked: () -> Unit = {},
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
                        when (blockquoteNode.tagName().lowercase()) {
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
            onLongClick = onTextLongClicked,
            interactionSource = interactionSource
        )
    }
}

@Composable
private fun HtmlOrderedList(
    orderedList: Element,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    onTextClicked: () -> Unit = {},
    onTextLongClicked: () -> Unit = {},
) {
    var number = 1
    val delimiter = "."
    HtmlListItems(
        list = orderedList,
        modifier = modifier,
        onTextClicked = onTextClicked, onTextLongClicked = onTextLongClicked,
        interactionSource = interactionSource
    ) {
        val text = buildAnnotatedString {
            append("${number++}$delimiter ${it.text()}")
        }
        HtmlText(
            text = text, onClick = onTextClicked,
            onLongClick = onTextLongClicked, interactionSource = interactionSource
        )
    }
}

@Composable
private fun HtmlUnorderedList(
    unorderedList: Element,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    onTextClicked: () -> Unit = {},
    onTextLongClicked: () -> Unit = {},
) {
    val marker = "・"
    HtmlListItems(
        list = unorderedList,
        modifier = modifier,
        onTextClicked = onTextClicked, onTextLongClicked = onTextLongClicked,
        interactionSource = interactionSource
    ) {
        val text = buildAnnotatedString {
            append("$marker ${it.text()}")
        }
        HtmlText(
            text = text, onClick = onTextClicked,
            onLongClick = onTextLongClicked, interactionSource = interactionSource
        )
    }
}

@Composable
private fun HtmlListItems(
    list: Element,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    onTextClicked: () -> Unit = {},
    onTextLongClicked: () -> Unit = {},
    content: @Composable (node: TextNode) -> Unit = {}
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
                        modifier = Modifier.padding(start = 4.dp),
                        onTextClicked = onTextClicked, onTextLongClicked = onTextLongClicked,
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
    when (element.tagName().lowercase()) {
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
        "i",
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
            pushStringAnnotation(tag = "URL", annotation = link.ownText())
            withStyle(
                style = SpanStyle(color = LinkColor)
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
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    val inlineContentMap = persistentMapOf<String, InlineTextContent>()
    ClickableLinkText(
        text = text,
        linkAnnotationTag = "URL",
        style = style,
        modifier = modifier,
        inlineContent = inlineContentMap,
        interactionSource = interactionSource,
        onClick = onClick,
        onLongClick = onLongClick
    )
}

@Preview
@Composable
internal fun HtmlDocumentLightPreview(@PreviewParameter(DocumentProvider::class) document: Document) =
    ElementPreviewLight { ContentToPreview(document) }

@Preview
@Composable
internal fun HtmlDocumentDarkPreview(@PreviewParameter(DocumentProvider::class) document: Document) =
    ElementPreviewDark { ContentToPreview(document) }

@Composable
private fun ContentToPreview(document: Document) {
    HtmlDocument(document, MutableInteractionSource())
}
