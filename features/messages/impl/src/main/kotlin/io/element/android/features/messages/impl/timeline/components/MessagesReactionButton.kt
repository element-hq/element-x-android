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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddReaction
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.timeline.model.AggregatedReaction
import io.element.android.features.messages.impl.timeline.model.AggregatedReactionProvider
import io.element.android.features.messages.impl.timeline.model.aTimelineItemReactions
import io.element.android.libraries.designsystem.preview.DayNightPreviews
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.text.toDp
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.theme.ElementTheme
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun MessagesReactionButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    content: MessagesReactionsButtonContent,
) {
    val buttonColor = if (content.isHighlighted) {
        ElementTheme.colors.bgSubtlePrimary
    } else {
        ElementTheme.colors.bgSubtleSecondary
    }

    val borderColor = if (content.isHighlighted) {
        ElementTheme.colors.borderInteractivePrimary
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
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            // Inner border, to highlight when selected
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(corner = CornerSize(12.dp)))
            .background(buttonColor, RoundedCornerShape(corner = CornerSize(12.dp)))
            .padding(vertical = 4.dp, horizontal = 10.dp),
        color = buttonColor
    ) {
        when (content) {
            is MessagesReactionsButtonContent.Icon -> IconContent(imageVector = content.imageVector)
            is MessagesReactionsButtonContent.Text -> TextContent(text = content.text)
            is MessagesReactionsButtonContent.Reaction -> ReactionContent(reaction = content.reaction)
        }
    }
}

sealed class MessagesReactionsButtonContent {
    data class Text(val text: String) : MessagesReactionsButtonContent()
    data class Icon(val imageVector: ImageVector) : MessagesReactionsButtonContent()

    data class Reaction(val reaction: AggregatedReaction) : MessagesReactionsButtonContent()

    val isHighlighted get() = this is Reaction && reaction.isHighlighted
}

private val reactionEmojiLineHeight = 20.sp

@Composable
private fun TextContent(
    text: String,
    modifier: Modifier = Modifier,
) = Text(
    modifier = modifier
        .height(reactionEmojiLineHeight.toDp()),
    text = text,
    style = ElementTheme.typography.fontBodyMdRegular,
)

@Composable
private fun IconContent(
    imageVector: ImageVector,
    modifier: Modifier = Modifier
) = Icon(
    imageVector = imageVector,
    contentDescription = stringResource(id = R.string.screen_room_timeline_add_reaction),
    tint = MaterialTheme.colorScheme.secondary,
    modifier = modifier
        .size(reactionEmojiLineHeight.toDp())
)

@Composable
private fun ReactionContent(
    reaction: AggregatedReaction,
    modifier: Modifier = Modifier,
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier,
) {
    Text(
        text = reaction.displayKey,
        style = ElementTheme.typography.fontBodyMdRegular.copy(
            fontSize = 15.sp,
            lineHeight = reactionEmojiLineHeight,
        ),
    )
    if (reaction.count > 1) {
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = reaction.count.toString(),
            color = if (reaction.isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            style = ElementTheme.typography.fontBodyMdRegular,
        )
    }
}

@DayNightPreviews
@Composable
internal fun MessagesReactionButtonPreview(@PreviewParameter(AggregatedReactionProvider::class) reaction: AggregatedReaction) = ElementPreview {
    MessagesReactionButton(
        content = MessagesReactionsButtonContent.Reaction(reaction),
        onClick = {}
    )
}

@DayNightPreviews
@Composable
internal fun MessagesReactionExtraButtonsPreview() = ElementPreview {
    Row {
        MessagesReactionButton(
            content = MessagesReactionsButtonContent.Icon(Icons.Outlined.AddReaction),
            onClick = {}
        )
        MessagesReactionButton(
            content = MessagesReactionsButtonContent.Text("12 more"),
            onClick = {}
        )
        MessagesReactionButton(
            content = MessagesReactionsButtonContent.Reaction(
                aTimelineItemReactions().reactions.first().copy(
                    key = "A very long reaction with many characters that should be truncated"
                )
            ),
            onClick = {}
        )
    }
}

