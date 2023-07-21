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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.timeline.aTimelineItemReactions
import io.element.android.features.messages.impl.timeline.model.AggregatedReaction
import io.element.android.features.messages.impl.timeline.model.TimelineItemReactions
import io.element.android.libraries.designsystem.preview.DayNightPreviews
import io.element.android.libraries.designsystem.preview.ElementPreview
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

/**
 * The maximum number of items that can be displayed before some items will be hidden
 *
 * TODO The threshold should be based on the number of rows, rather than items.
 *       Once items would spill onto a third row, they should be hidden.
 *       Note this could be particularly worthwhile to handle reactions that are
 *       longer than a single character (as annotation keys are free text).
 */
private const val COLLAPSE_ITEMS_THRESHOLD = 8

@Composable
fun TimelineItemReactions(
    reactionsState: TimelineItemReactions,
    mainAxisAlignment: FlowMainAxisAlignment,
    onReactionClicked: (emoji: String) -> Unit,
    onReactionLongClicked: (emoji: String) -> Unit,
    onMoreReactionsClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded: Boolean by rememberSaveable { mutableStateOf(false) }

    val reactions by remember(reactionsState, expanded) {
        derivedStateOf {
            val numToDisplay = if (expanded) {
                reactionsState.reactions.count()
            } else {
                COLLAPSE_ITEMS_THRESHOLD
            }
            reactionsState.reactions.take(numToDisplay).toPersistentList()
        }
    }

    val expandableState by remember {
        derivedStateOf {
            if (expanded) {
                ExpandableState.Expanded
            } else {
                val hiddenItems = reactionsState.reactions.count() - reactions.count()
                if (hiddenItems > 0) {
                    ExpandableState.Collapsed(hidden = hiddenItems)
                } else {
                    ExpandableState.None
                }
            }
        }
    }

    TimelineItemReactionsView(
        modifier = modifier,
        reactions = reactions,
        expandableState = expandableState,
        mainAxisAlignment = mainAxisAlignment,
        onReactionClick = onReactionClicked,
        onReactionLongClick = onReactionLongClicked,
        onMoreReactionsClick = onMoreReactionsClicked,
        onExpandClick = { expanded = true },
        onCollapseClick = { expanded = false }
    )
}

private sealed class ExpandableState {
    object None: ExpandableState()
    data class Collapsed(val hidden: Int): ExpandableState()
    object Expanded : ExpandableState()
}

@Composable
private fun TimelineItemReactionsView(
    reactions: ImmutableList<AggregatedReaction>,
    expandableState: ExpandableState,
    mainAxisAlignment: FlowMainAxisAlignment,
    onReactionClick: (emoji: String) -> Unit,
    onReactionLongClick: (emoji: String) -> Unit,
    onMoreReactionsClick: () -> Unit,
    onExpandClick: () -> Unit,
    onCollapseClick: () -> Unit,
    modifier: Modifier = Modifier
) =
    FlowRow(
        modifier = modifier,
        mainAxisSpacing = 4.dp,
        crossAxisSpacing = 4.dp,
        mainAxisAlignment = mainAxisAlignment,
    ) {
        reactions.forEach { reaction ->
            MessagesReactionButton(
                content = MessagesReactionsButtonContent.Reaction(reaction = reaction),
                onClick = { onReactionClick(reaction.key) },
                onLongClick = { onReactionLongClick(reaction.key) }
            )
        }
        when (expandableState) {
            ExpandableState.Expanded ->
                MessagesReactionButton(
                    content = MessagesReactionsButtonContent.Text(
                        text = stringResource(id = R.string.screen_room_timeline_less_reactions)
                    ),
                    onClick = onCollapseClick,
                )
            is ExpandableState.Collapsed -> {
                val hidden = expandableState.hidden
                MessagesReactionButton(
                    content = MessagesReactionsButtonContent.Text(
                        text = pluralStringResource(id = R.plurals.screen_room_timeline_more_reactions, hidden, hidden)
                    ),
                    onClick = onExpandClick,
                )
            }
            ExpandableState.None -> {
                // No expand or collapse action available
            }
        }
        MessagesReactionButton(
            content = MessagesReactionsButtonContent.Icon(Icons.Outlined.AddReaction),
            onClick = onMoreReactionsClick
        )
    }

@DayNightPreviews
@Composable
fun TimelineItemReactionsViewPreview() = ElementPreview {
    ContentToPreview(
        reactions = aTimelineItemReactions(count = 1).reactions,
        expandableState = ExpandableState.None,
    )
}

@DayNightPreviews
@Composable
fun TimelineItemReactionsViewCollapsedPreview() = ElementPreview {
    ContentToPreview(
        reactions = aTimelineItemReactions(count = 3).reactions,
        expandableState = ExpandableState.Collapsed(hidden = 7),
    )
}

@DayNightPreviews
@Composable
fun TimelineItemReactionsViewExpandedPreview() = ElementPreview {
    ContentToPreview(
        reactions = aTimelineItemReactions(count = 10).reactions,
        expandableState = ExpandableState.Expanded,
    )
}

@Composable
private fun ContentToPreview(
    reactions: ImmutableList<AggregatedReaction>,
    expandableState: ExpandableState
) {
    TimelineItemReactionsView(
        reactions = reactions,
        expandableState = expandableState,
        mainAxisAlignment = FlowMainAxisAlignment.Center,
        onReactionClick = {},
        onReactionLongClick = { },
        onMoreReactionsClick = {},
        onExpandClick = {},
        onCollapseClick = {}
    )
}

