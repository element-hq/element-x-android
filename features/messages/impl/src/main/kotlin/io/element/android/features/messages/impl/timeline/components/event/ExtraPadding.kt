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

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import io.element.android.libraries.ui.strings.CommonStrings
import kotlin.math.ceil

// Allow to not overlap the timestamp with the text, in the message bubble.
// Compute the size of the worst case.
data class ExtraPadding(val extraWidth: Dp)

val noExtraPadding = ExtraPadding(0.dp)

/**
 * See [io.element.android.features.messages.impl.timeline.components.TimelineEventTimestampView] for the related View.
 * And https://www.figma.com/file/0MMNu7cTOzLOlWb7ctTkv3/Element-X?node-id=1819%253A99506 for the design.
 */
@Composable
fun TimelineItem.Event.toExtraPadding(): ExtraPadding {
    val formattedTime = sentTime
    val hasMessageSendingFailed = localSendState is LocalEventSendState.SendingFailed
    val isMessageEdited = (content as? TimelineItemTextBasedContent)?.isEdited.orFalse()

    val textMeasurer = rememberTextMeasurer(cacheSize = 128)
    val density = LocalDensity.current

    var strLen = 2.dp // Extra space char
    if (isMessageEdited) {
        val editedText = stringResource(id = CommonStrings.common_edited_suffix)
        val extraLen = remember(editedText, density) { textMeasurer.getExtraPadding(editedText, density) } + 10.dp // Text + spacing
        strLen += extraLen
    }
    strLen += remember(formattedTime, density) { textMeasurer.getExtraPadding(formattedTime, density) }
    if (hasMessageSendingFailed) {
        strLen += 19.dp // Image + spacing
        // I do not know why, but adding extra widths avoid overlapping when the
        // message is edited and in error.
        if (isMessageEdited) {
            strLen += 2.dp
        }
    }
    return ExtraPadding(strLen)
}

private fun TextMeasurer.getExtraPadding(text: String, density: Density): Dp {
    val timestampTextStyle = ElementTheme.typography.fontBodyXsRegular
    val textWidth = measure(text = text, style = timestampTextStyle).size.width
    return (textWidth / density.density).dp
}

/**
 * Get a string to add to the content of the message to avoid overlapping the timestamp.
 */
@Composable
fun ExtraPadding.getStr(textStyle: TextStyle = LocalTextStyle.current): String {
    if (extraWidth == 0.dp) return ""
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer(128)
    val charWidth = remember(textStyle) { textMeasurer.measure(text = "\u00A0", style = textStyle).size.width }
    val widthPx = remember(density, extraWidth) { with(density) { extraWidth.toPx() } }
    // A space and some unbreakable spaces, always rounding the result to the next value if not a integer
    return " " + "\u00A0".repeat(ceil(widthPx / charWidth).toInt())
}

@Composable
fun ExtraPadding.getDpSize(): Dp {
    return extraWidth
}
