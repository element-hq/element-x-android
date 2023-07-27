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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddReaction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.timeline.aTimelineItemReactions
import io.element.android.features.messages.impl.timeline.model.AggregatedReaction
import io.element.android.features.messages.impl.timeline.model.TimelineItemReactions
import io.element.android.libraries.designsystem.preview.DayNightPreviews
import io.element.android.libraries.designsystem.preview.ElementPreview
import kotlinx.collections.immutable.ImmutableList

@Composable
fun TimelineItemReactions(
    reactionsState: TimelineItemReactions,
    isOutgoing: Boolean,
    onReactionClicked: (emoji: String) -> Unit,
    onReactionLongClicked: (emoji: String) -> Unit,
    onMoreReactionsClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded: Boolean by rememberSaveable { mutableStateOf(false) }

    // In LTR languages we want an incoming message's reactions to be LRT and outgoing to be RTL.
    // For RTL languages it should be the opposite.
    val reactionsLayoutDirection = if (!isOutgoing) LocalLayoutDirection.current
    else if (LocalLayoutDirection.current == LayoutDirection.Ltr)
        LayoutDirection.Rtl
    else
        LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides reactionsLayoutDirection) {
        TimelineItemReactionsView(
            modifier = modifier,
            reactions = reactionsState.reactions,
            expanded = expanded,
            onReactionClick = onReactionClicked,
            onReactionLongClick = onReactionLongClicked,
            onMoreReactionsClick = onMoreReactionsClicked,
            onToggleExpandClick = { expanded = !expanded },
        )
    }
}

@Composable
private fun TimelineItemReactionsView(
    reactions: ImmutableList<AggregatedReaction>,
    expanded: Boolean,
    onReactionClick: (emoji: String) -> Unit,
    onReactionLongClick: (emoji: String) -> Unit,
    onMoreReactionsClick: () -> Unit,
    onToggleExpandClick: () -> Unit,
    modifier: Modifier = Modifier
) = TimelineItemReactionsLayout(
    modifier = modifier,
    itemSpacing = 4.dp,
    rowSpacing = 4.dp,
    expanded = expanded,
    expandButton = {
        MessagesReactionButton(
            content = MessagesReactionsButtonContent.Text(
                text = stringResource(id = if (expanded) R.string.screen_room_reactions_show_less else R.string.screen_room_reactions_show_more)
            ),
            onClick = onToggleExpandClick,
            onLongClick = {}
        )
    },
    addMoreButton = {
        MessagesReactionButton(
            content = MessagesReactionsButtonContent.Icon(Icons.Outlined.AddReaction),
            onClick = onMoreReactionsClick,
            onLongClick = {}
        )
    },
    reactions = {
        reactions.forEach { reaction ->
            MessagesReactionButton(
                content = MessagesReactionsButtonContent.Reaction(reaction = reaction),
                onClick = { onReactionClick(reaction.key) },
                onLongClick = { onReactionLongClick(reaction.key) }
            )
        }
    }
)

@DayNightPreviews
@Composable
fun TimelineItemReactionsViewPreview() = ElementPreview {
    ContentToPreview(
        reactions = aTimelineItemReactions(count = 1).reactions
    )
}

@DayNightPreviews
@Composable
fun TimelineItemReactionsViewFewPreview() = ElementPreview {
    ContentToPreview(
        reactions = aTimelineItemReactions(count = 3).reactions
    )
}

@DayNightPreviews
@Composable
fun TimelineItemReactionsViewIncomingPreview() = ElementPreview {
    ContentToPreview(
        reactions = aTimelineItemReactions(count = 18).reactions
    )
}

@DayNightPreviews
@Composable
fun TimelineItemReactionsViewOutgoingPreview() = ElementPreview {
    ContentToPreview(
        reactions = aTimelineItemReactions(count = 18).reactions,
        isOutgoing = true
    )
}

@Composable
private fun ContentToPreview(
    reactions: ImmutableList<AggregatedReaction>,
    isOutgoing: Boolean = false
) {
    TimelineItemReactions(
        reactionsState = TimelineItemReactions(
            reactions
        ),
        isOutgoing = isOutgoing,
        onReactionClicked = {},
        onReactionLongClicked = {},
        onMoreReactionsClicked = {},
    )
}

