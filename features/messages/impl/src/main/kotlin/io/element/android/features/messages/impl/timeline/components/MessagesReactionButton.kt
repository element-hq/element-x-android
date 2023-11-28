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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.timeline.model.AggregatedReaction
import io.element.android.features.messages.impl.timeline.model.AggregatedReactionProvider
import io.element.android.features.messages.impl.timeline.model.aTimelineItemReactions
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toDp
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.compound.theme.ElementTheme

@Composable
@OptIn(ExperimentalFoundationApi::class)
@Suppress("ModifierClickableOrder") // This is needed to display the right ripple shape
fun MessagesReactionButton(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    content: MessagesReactionsButtonContent,
    modifier: Modifier = Modifier,
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
            is MessagesReactionsButtonContent.Icon -> IconContent(resourceId = content.resourceId)
            is MessagesReactionsButtonContent.Text -> TextContent(text = content.text)
            is MessagesReactionsButtonContent.Reaction -> ReactionContent(reaction = content.reaction)
        }
    }
}

@Immutable
sealed interface MessagesReactionsButtonContent {
    data class Text(val text: String) : MessagesReactionsButtonContent
    data class Icon(@DrawableRes val resourceId: Int) : MessagesReactionsButtonContent

    data class Reaction(val reaction: AggregatedReaction) : MessagesReactionsButtonContent

    val isHighlighted get() = this is Reaction && reaction.isHighlighted
}

private val reactionEmojiLineHeight = 20.sp
private val addEmojiSize = 16.dp

@Composable
private fun TextContent(
    text: String,
    modifier: Modifier = Modifier,
) = Text(
    modifier = modifier
        .height(reactionEmojiLineHeight.toDp()),
    text = text,
    style = ElementTheme.typography.fontBodyMdRegular,
    color = ElementTheme.materialColors.primary
)

@Composable
private fun IconContent(
    @DrawableRes resourceId: Int,
    modifier: Modifier = Modifier
) = Icon(
    resourceId = resourceId,
    contentDescription = stringResource(id = R.string.screen_room_timeline_add_reaction),
    tint = ElementTheme.materialColors.secondary,
    modifier = modifier
        .size(addEmojiSize)

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

@PreviewsDayNight
@Composable
internal fun MessagesReactionButtonPreview(@PreviewParameter(AggregatedReactionProvider::class) reaction: AggregatedReaction) = ElementPreview {
    MessagesReactionButton(
        content = MessagesReactionsButtonContent.Reaction(reaction),
        onClick = {},
        onLongClick = {}
    )
}

@PreviewsDayNight
@Composable
internal fun MessagesAddReactionButtonPreview() = ElementPreview {
    MessagesReactionButton(
        content = MessagesReactionsButtonContent.Icon(CommonDrawables.ic_add_reaction),
        onClick = {},
        onLongClick = {}
    )
}

@PreviewsDayNight
@Composable
internal fun MessagesReactionExtraButtonsPreview() = ElementPreview {
    Row {
        MessagesReactionButton(
            content = MessagesReactionsButtonContent.Text("12 more"),
            onClick = {},
            onLongClick = {}
        )
        MessagesReactionButton(
            content = MessagesReactionsButtonContent.Reaction(
                aTimelineItemReactions().reactions.first().copy(
                    key = "A very long reaction with many characters that should be truncated"
                )
            ),
            onClick = {},
            onLongClick = {}
        )
    }
}

