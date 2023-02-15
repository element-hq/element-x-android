/*
 * Copyright (c) 2023 New Vector Ltd
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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

open class DocumentProvider : PreviewParameterProvider<Document> {
    override val values: Sequence<Document>
        get() = sequenceOf(
            "text",
            "<strong>Strong</strong>",
            "<b>Bold</b>",
            "<i>Italic</i>",
            // FIXME This does not work
            "<b><i>Bold then italic</i></b>",
            // FIXME This does not work
            "<i><b>Italic then bold</b></i>",
            "<em>em</em>",
            "<unknown>unknown</unknown>",
            // FIXME `br` is not rendered correctly in the Preview.
            "Line 1<br/>Line 2",
            "<code>code</code>",
            "<del>del</del>",
            "<h1>Heading 1</h1><h2>Heading 2</h2><h3>Heading 3</h3><h4>Heading 4</h4><h5>Heading 5</h5><h6>Heading 6</h6><h7>Heading 7</h7>",
            "<a href=\"https://matrix.org\">link</a>",
            "<p>paragraph</p>",
            "<p>paragraph 1</p><p>paragraph 2</p>",
            "<ol><li>ol item 1</li><li>ol item 2</li></ol>",
            "<ol><li><i>ol item 1 italic</i></li><li><b>ol item 2 bold</b></li></ol>",
            "<ul><li>ul item 1</li><li>ul item 2</li></ul>",
            "<blockquote>blockquote</blockquote>",
            // TODO Find a way to make is work with `pre`. For now there is an error with
            // jsoup: java.lang.NoSuchMethodError: 'org.jsoup.nodes.Element org.jsoup.nodes.Element.firstElementChild()'
            // "<pre>pre</pre>",
            "<mx-reply><blockquote><a href=\\\"https://matrix.to/#/!roomId/\$eventId?via=matrix.org\\\">In reply to</a> " +
                "<a href=\\\"https://matrix.to/#/@alice:matrix.org\\\">@alice:matrix.org</a><br>original message</blockquote></mx-reply>reply",
        ).map { Jsoup.parse(it) }
}
