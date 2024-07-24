/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.matrix.impl.util

import io.element.android.libraries.matrix.api.room.Mention
import io.element.android.libraries.matrix.impl.room.map
import org.matrix.rustcomponents.sdk.RoomMessageEventContentWithoutRelation
import org.matrix.rustcomponents.sdk.messageEventContentFromHtml
import org.matrix.rustcomponents.sdk.messageEventContentFromMarkdown

/**
 * Creates a [RoomMessageEventContentWithoutRelation] from a body, an html body and a list of mentions.
 */
object MessageEventContent {
    fun from(body: String, htmlBody: String?, mentions: List<Mention>): RoomMessageEventContentWithoutRelation {
        return if (htmlBody != null) {
            messageEventContentFromHtml(body, htmlBody)
        } else {
            messageEventContentFromMarkdown(body)
        }.withMentions(mentions.map())
    }
}
