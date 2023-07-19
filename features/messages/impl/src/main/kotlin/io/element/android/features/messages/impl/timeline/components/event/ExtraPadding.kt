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

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings

// Allow to not overlap the timestamp with the text, in the message bubble.
// Compute the size of the worst case.
data class ExtraPadding(val nbChars: Int)

val noExtraPadding = ExtraPadding(0)

/**
 * See [io.element.android.features.messages.impl.timeline.components.TimelineEventTimestampView] for the related View.
 * And https://www.figma.com/file/0MMNu7cTOzLOlWb7ctTkv3/Element-X?node-id=1819%253A99506 for the design.
 */
@Composable
fun TimelineItem.Event.toExtraPadding(): ExtraPadding {
    val formattedTime = sentTime
    val hasMessageSendingFailed = localSendState is LocalEventSendState.SendingFailed
    val isMessageEdited = (content as? TimelineItemTextBasedContent)?.isEdited.orFalse()

    var strLen = 6
    if (isMessageEdited) {
        strLen += stringResource(id = CommonStrings.common_edited_suffix).length + 3
    }
    strLen += formattedTime.length
    if (hasMessageSendingFailed) {
        strLen += 5
        if (isMessageEdited) {
            // I do not know why, but adding 2 more chars avoid overlapping when the
            // message is edited and in error.
            strLen += 2
        }
    }
    return ExtraPadding(strLen)
}

/**
 * Get a string to add to the content of the message to avoid overlapping the timestamp.
 * @param fontSize the font size of the message content, to be able to add more space char if the font is small.
 */
fun ExtraPadding.getStr(fontSize: TextUnit): String {
    if (nbChars == 0) return ""
    val timestampFontSize = ElementTheme.typography.fontBodyXsRegular.fontSize // 11.sp
    val nbOfSpaces = ((timestampFontSize.value / fontSize.value) * nbChars).toInt() + 1
    // A space and some unbreakable spaces
    return " " + "\u00A0".repeat(nbOfSpaces)
}
