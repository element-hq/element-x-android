/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.actionlist

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.aTimelineItemReactions
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemAudioContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemFileContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemLocationContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemVoiceContent
import io.element.android.libraries.matrix.api.timeline.item.event.MessageShield
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

open class ActionListStateProvider : PreviewParameterProvider<ActionListState> {
    override val values: Sequence<ActionListState>
        get() {
            val reactionsState = aTimelineItemReactions(1, isHighlighted = true)
            return sequenceOf(
                anActionListState(),
                anActionListState().copy(target = ActionListState.Target.Loading(aTimelineItemEvent())),
                anActionListState().copy(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent().copy(
                            reactionsState = reactionsState
                        ),
                        displayEmojiReactions = true,
                        actions = aTimelineItemActionList(),
                    )
                ),
                anActionListState().copy(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(
                            content = aTimelineItemImageContent(),
                            displayNameAmbiguous = true,
                        ).copy(
                            reactionsState = reactionsState,
                        ),
                        displayEmojiReactions = true,
                        actions = aTimelineItemActionList(),
                    )
                ),
                anActionListState().copy(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(content = aTimelineItemVideoContent()).copy(
                            reactionsState = reactionsState
                        ),
                        displayEmojiReactions = true,
                        actions = aTimelineItemActionList(),
                    )
                ),
                anActionListState().copy(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(content = aTimelineItemFileContent()).copy(
                            reactionsState = reactionsState
                        ),
                        displayEmojiReactions = true,
                        actions = aTimelineItemActionList(),
                    )
                ),
                anActionListState().copy(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(content = aTimelineItemAudioContent()).copy(
                            reactionsState = reactionsState
                        ),
                        displayEmojiReactions = true,
                        actions = aTimelineItemActionList(),
                    )
                ),
                anActionListState().copy(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(content = aTimelineItemVoiceContent()).copy(
                            reactionsState = reactionsState
                        ),
                        displayEmojiReactions = true,
                        actions = aTimelineItemActionList(),
                    )
                ),
                anActionListState().copy(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(content = aTimelineItemLocationContent()).copy(
                            reactionsState = reactionsState
                        ),
                        displayEmojiReactions = true,
                        actions = aTimelineItemActionList(),
                    )
                ),
                anActionListState().copy(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(content = aTimelineItemLocationContent()).copy(
                            reactionsState = reactionsState
                        ),
                        displayEmojiReactions = false,
                        actions = aTimelineItemActionList(),
                    ),
                ),
                anActionListState().copy(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(content = aTimelineItemPollContent()).copy(
                            reactionsState = reactionsState
                        ),
                        displayEmojiReactions = false,
                        actions = aTimelineItemPollActionList(),
                    ),
                ),
                anActionListState().copy(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent().copy(
                            reactionsState = reactionsState,
                            messageShield = MessageShield.UnknownDevice(isCritical = true)
                        ),
                        displayEmojiReactions = true,
                        actions = aTimelineItemActionList(),
                    )
                ),
            )
        }
}

fun anActionListState(
    target: ActionListState.Target = ActionListState.Target.None,
    eventSink: (ActionListEvents) -> Unit = {},
) = ActionListState(
    target = target,
    eventSink = eventSink
)

fun aTimelineItemActionList(): ImmutableList<TimelineItemAction> {
    return persistentListOf(
        TimelineItemAction.Reply,
        TimelineItemAction.Forward,
        TimelineItemAction.Copy,
        TimelineItemAction.CopyLink,
        TimelineItemAction.Edit,
        TimelineItemAction.Redact,
        TimelineItemAction.ReportContent,
        TimelineItemAction.ViewSource,
    )
}

fun aTimelineItemPollActionList(): ImmutableList<TimelineItemAction> {
    return persistentListOf(
        TimelineItemAction.EndPoll,
        TimelineItemAction.Reply,
        TimelineItemAction.Copy,
        TimelineItemAction.CopyLink,
        TimelineItemAction.ViewSource,
        TimelineItemAction.ReportContent,
        TimelineItemAction.Redact,
    )
}
