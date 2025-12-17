/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.timeline.a11y.a11yReactionAction
import io.element.android.features.messages.impl.timeline.a11y.a11yReactionDetails
import io.element.android.features.messages.impl.timeline.model.AggregatedReaction
import io.element.android.features.messages.impl.timeline.model.AggregatedReactionProvider
import io.element.android.features.messages.impl.timeline.model.aTimelineItemReactions
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import io.element.android.libraries.designsystem.modifiers.onKeyboardContextMenuAction
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toDp
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
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

    val a11yText = when (content) {
        is MessagesReactionsButtonContent.Icon -> stringResource(id = R.string.screen_room_timeline_add_reaction)
        is MessagesReactionsButtonContent.Text -> content.text
        is MessagesReactionsButtonContent.Reaction -> {
            a11yReactionDetails(
                emoji = content.reaction.key,
                userAlreadyReacted = content.isHighlighted,
                reactionCount = content.reaction.count,
            )
        }
    }

    Surface(
        modifier = modifier
            .background(Color.Transparent)
            // Outer border, same colour as background
            .border(
                BorderStroke(2.dp, ElementTheme.colors.bgCanvasDefault),
                shape = RoundedCornerShape(corner = CornerSize(14.dp))
            )
            .padding(vertical = 2.dp, horizontal = 2.dp)
            // Clip click indicator inside the outer border
            .clip(RoundedCornerShape(corner = CornerSize(12.dp)))
            .combinedClickable(
                onClick = onClick,
                onClickLabel = (content as? MessagesReactionsButtonContent.Reaction)?.let {
                    a11yReactionAction(
                        emoji = content.reaction.key,
                        userAlreadyReacted = content.isHighlighted
                    )
                },
                onLongClickLabel = stringResource(CommonStrings.action_open_context_menu),
                onLongClick = onLongClick
            )
            .onKeyboardContextMenuAction(onLongClick)
            // Inner border, to highlight when selected
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(corner = CornerSize(12.dp)))
            .background(buttonColor, RoundedCornerShape(corner = CornerSize(12.dp)))
            .padding(vertical = 4.dp, horizontal = 10.dp)
            .clearAndSetSemantics {
                contentDescription = a11yText
            },
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

internal val REACTION_EMOJI_LINE_HEIGHT = 20.sp
internal const val REACTION_IMAGE_ASPECT_RATIO = 1.0f
private val ADD_EMOJI_SIZE = 16.dp

@Composable
private fun TextContent(
    text: String,
) = Text(
    modifier = Modifier
        .height(REACTION_EMOJI_LINE_HEIGHT.toDp()),
    text = text,
    style = ElementTheme.typography.fontBodyMdRegular,
    color = ElementTheme.colors.textPrimary
)

@Composable
private fun IconContent(
    @DrawableRes resourceId: Int,
) = Icon(
    resourceId = resourceId,
    contentDescription = stringResource(id = R.string.screen_room_timeline_add_reaction),
    tint = ElementTheme.colors.iconSecondary,
    modifier = Modifier
        .size(ADD_EMOJI_SIZE)
)

@Composable
private fun ReactionContent(
    reaction: AggregatedReaction,
) = Row(
    verticalAlignment = Alignment.CenterVertically,
) {
    // Check if this is a custom reaction (MSC4027)
    if (reaction.key.startsWith("mxc://")) {
        AsyncImage(
            modifier = Modifier
                .heightIn(min = REACTION_EMOJI_LINE_HEIGHT.toDp(), max = REACTION_EMOJI_LINE_HEIGHT.toDp())
                .aspectRatio(REACTION_IMAGE_ASPECT_RATIO, false),
            model = MediaRequestData(MediaSource(reaction.key), MediaRequestData.Kind.Content),
            contentDescription = null
        )
    } else {
        Text(
            text = reaction.displayKey,
            style = ElementTheme.typography.fontBodyMdRegular.copy(
                fontSize = 15.sp,
                lineHeight = REACTION_EMOJI_LINE_HEIGHT,
            ),
        )
    }
    if (reaction.count > 1) {
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = reaction.count.toString(),
            color = if (reaction.isHighlighted) ElementTheme.colors.textPrimary else ElementTheme.colors.textSecondary,
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
internal fun MessagesReactionButtonAddPreview() = ElementPreview {
    MessagesReactionButton(
        content = MessagesReactionsButtonContent.Icon(CompoundDrawables.ic_compound_reaction_add),
        onClick = {},
        onLongClick = {}
    )
}

@PreviewsDayNight
@Composable
internal fun MessagesReactionButtonExtraPreview() = ElementPreview {
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
