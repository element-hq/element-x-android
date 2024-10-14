/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

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
import io.element.android.libraries.architecture.coverage.ExcludeFromCoverage
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import kotlinx.collections.immutable.ImmutableList

@Composable
fun TimelineItemReactionsView(
    reactionsState: TimelineItemReactions,
    isOutgoing: Boolean,
    userCanSendReaction: Boolean,
    onReactionClick: (emoji: String) -> Unit,
    onReactionLongClick: (emoji: String) -> Unit,
    onMoreReactionsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded: Boolean by rememberSaveable { mutableStateOf(false) }
    TimelineItemReactionsView(
        modifier = modifier,
        reactions = reactionsState.reactions,
        userCanSendReaction = userCanSendReaction,
        expanded = expanded,
        isOutgoing = isOutgoing,
        onReactionClick = onReactionClick,
        onReactionLongClick = onReactionLongClick,
        onMoreReactionsClick = onMoreReactionsClick,
        onToggleExpandClick = { expanded = !expanded },
    )
}

@Composable
private fun TimelineItemReactionsView(
    reactions: ImmutableList<AggregatedReaction>,
    userCanSendReaction: Boolean,
    isOutgoing: Boolean,
    expanded: Boolean,
    onReactionClick: (emoji: String) -> Unit,
    onReactionLongClick: (emoji: String) -> Unit,
    onMoreReactionsClick: () -> Unit,
    onToggleExpandClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // In LTR languages we want an incoming message's reactions to be LTR and outgoing to be RTL.
    // For RTL languages it should be the opposite.
    val currentLayout = LocalLayoutDirection.current
    val reactionsLayoutDirection = when {
        !isOutgoing -> currentLayout
        currentLayout == LayoutDirection.Ltr -> LayoutDirection.Rtl
        else -> LayoutDirection.Ltr
    }

    CompositionLocalProvider(LocalLayoutDirection provides reactionsLayoutDirection) {
        TimelineItemReactionsLayout(
            modifier = modifier,
            itemSpacing = 4.dp,
            rowSpacing = 4.dp,
            expanded = expanded,
            expandButton = {
                MessagesReactionButton(
                    content = MessagesReactionsButtonContent.Text(
                        text = stringResource(
                            id = if (expanded) {
                                R.string.screen_room_timeline_reactions_show_less
                            } else {
                                R.string.screen_room_timeline_reactions_show_more
                            }
                        )
                    ),
                    onClick = onToggleExpandClick,
                    onLongClick = {}
                )
            },
            addMoreButton = if (userCanSendReaction) {
                {
                    CompositionLocalProvider(LocalLayoutDirection provides currentLayout) {
                        MessagesReactionButton(
                            content = MessagesReactionsButtonContent.Icon(CompoundDrawables.ic_compound_reaction_add),
                            onClick = onMoreReactionsClick,
                            onLongClick = {}
                        )
                    }
                }
            } else {
                null
            },
            reactions = {
                reactions.forEach { reaction ->
                    CompositionLocalProvider(LocalLayoutDirection provides currentLayout) {
                        MessagesReactionButton(
                            content = MessagesReactionsButtonContent.Reaction(reaction = reaction),
                            onClick = {
                                // Always allow user to redact their own reactions
                                if (reaction.isHighlighted || userCanSendReaction) {
                                    onReactionClick(reaction.key)
                                }
                            },
                            onLongClick = { onReactionLongClick(reaction.key) }
                        )
                    }
                }
            }
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemReactionsViewPreview() = ElementPreview {
    ContentToPreview(
        reactions = aTimelineItemReactions(count = 1).reactions
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemReactionsViewFewPreview() = ElementPreview {
    ContentToPreview(
        reactions = aTimelineItemReactions(count = 3).reactions
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemReactionsViewIncomingPreview() = ElementPreview {
    ContentToPreview(
        reactions = aTimelineItemReactions(count = 18).reactions
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemReactionsViewOutgoingPreview() = ElementPreview {
    ContentToPreview(
        reactions = aTimelineItemReactions(count = 18).reactions,
        isOutgoing = true
    )
}

@ExcludeFromCoverage
@Composable
private fun ContentToPreview(
    reactions: ImmutableList<AggregatedReaction>,
    isOutgoing: Boolean = false
) {
    TimelineItemReactionsView(
        reactionsState = TimelineItemReactions(
            reactions
        ),
        userCanSendReaction = true,
        isOutgoing = isOutgoing,
        onReactionClick = {},
        onReactionLongClick = {},
        onMoreReactionsClick = {},
    )
}
