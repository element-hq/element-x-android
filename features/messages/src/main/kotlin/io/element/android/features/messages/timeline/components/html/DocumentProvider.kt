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
            "hello",
            // TODO Find a way to make is work with real HTML data. For now there is an error with
            // jsoup: java.lang.NoSuchMethodError: 'java.lang.String org.jsoup.nodes.Element.normalName()'
            /*
            "<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                "  <p><strong>Bold</strong></p>\n" +
                " </body>\n" +
                "</html>",
            "<html><head></head><body><b>Bold</b></body></html>",
            "<h1>Heading 1</h1>",
            "<h2>Heading 2</h2>",
             */
        ).map { Jsoup.parse(it) }
}
