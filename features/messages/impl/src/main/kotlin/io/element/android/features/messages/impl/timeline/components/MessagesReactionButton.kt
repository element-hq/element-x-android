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

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.features.messages.impl.timeline.model.AggregatedReaction
import io.element.android.features.messages.impl.timeline.model.AggregatedReactionProvider
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.theme.ElementTheme

@Composable
fun MessagesReactionButton(reaction: AggregatedReaction, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val buttonColor = if(reaction.isHighlighted) {
        // TODO Should use compound.bgSubtlePrimary
        ElementTheme.legacyColors.gray400
    } else {
        // TODO Should use compound.bgSubtleSecondary
        ElementTheme.legacyColors.gray300
    }
    val borderColor = if (reaction.isHighlighted) {
        // TODO Check the color, should use compound.borderInteractivePrimary
        Color(0xFF808994)
    } else {
        buttonColor
    }
    Surface(
        modifier = modifier
            .background(Color.Transparent)
            // Outer border, same colour as background
            .border(
                BorderStroke(2.dp, MaterialTheme.colorScheme.background),
                shape = RoundedCornerShape(corner = CornerSize(14.dp))
            )
            .padding(vertical = 2.dp, horizontal = 2.dp)
            // Clip click indicator inside the outer border
            .clip(RoundedCornerShape(corner = CornerSize(12.dp)))
            .clickable(onClick = onClick)
            // Inner border, to highlight when selected
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(corner = CornerSize(12.dp)))
            .background(buttonColor, RoundedCornerShape(corner = CornerSize(12.dp)))
            .padding(vertical = 4.dp, horizontal = 10.dp),
        color = buttonColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = reaction.key, fontSize = 15.sp, lineHeight = 20.sp
            )
            if (reaction.count > 1) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = reaction.count.toString(),
                    color = if (reaction.isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview
@Composable
internal fun MessagesReactionButtonLightPreview(@PreviewParameter(AggregatedReactionProvider::class) reaction: AggregatedReaction) =
    ElementPreviewLight { ContentToPreview(reaction) }

@Preview
@Composable
internal fun MessagesReactionButtonDarkPreview(@PreviewParameter(AggregatedReactionProvider::class) reaction: AggregatedReaction) =
    ElementPreviewDark { ContentToPreview(reaction) }

@Composable
private fun ContentToPreview(reaction: AggregatedReaction) {
    MessagesReactionButton(reaction, onClick = { })
}
