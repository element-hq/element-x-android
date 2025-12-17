/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.actionlist

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.actionlist.model.TimelineItemActionComparator
import io.element.android.features.messages.impl.crypto.sendfailure.VerifiedUserSendFailure
import io.element.android.features.messages.impl.crypto.sendfailure.resolve.anUnsignedDeviceSendFailure
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
import kotlinx.collections.immutable.toImmutableList

open class ActionListStateProvider : PreviewParameterProvider<ActionListState> {
    private val suggestedEmojis = persistentListOf("👍️", "👎️", "🔥", "❤️", "👏")

    override val values: Sequence<ActionListState>
        get() {
            val reactionsState = aTimelineItemReactions(1, isHighlighted = true)
            return sequenceOf(
                anActionListState(),
                anActionListState().copy(target = ActionListState.Target.Loading(aTimelineItemEvent())),
                anActionListState(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(
                            timelineItemReactions = reactionsState
                        ),
                        sentTimeFull = "January 1, 1970 at 12:00 AM",
                        displayEmojiReactions = true,
                        verifiedUserSendFailure = VerifiedUserSendFailure.None,
                        actions = aTimelineItemActionList(),
                        recentEmojis = suggestedEmojis,
                    )
                ),
                anActionListState(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(
                            content = aTimelineItemImageContent(),
                            displayNameAmbiguous = true,
                            timelineItemReactions = reactionsState,
                        ),
                        sentTimeFull = "January 1, 1970 at 12:00 AM",
                        displayEmojiReactions = true,
                        verifiedUserSendFailure = VerifiedUserSendFailure.None,
                        actions = aTimelineItemActionList(
                            copyAction = TimelineItemAction.CopyCaption,
                        ),
                        recentEmojis = suggestedEmojis,
                    )
                ),
                anActionListState(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(
                            content = aTimelineItemVideoContent(),
                            timelineItemReactions = reactionsState
                        ),
                        sentTimeFull = "January 1, 1970 at 12:00 AM",
                        displayEmojiReactions = true,
                        verifiedUserSendFailure = VerifiedUserSendFailure.None,
                        actions = aTimelineItemActionList(
                            copyAction = TimelineItemAction.CopyCaption,
                        ),
                        recentEmojis = suggestedEmojis,
                    )
                ),
                anActionListState(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(
                            content = aTimelineItemFileContent(),
                            timelineItemReactions = reactionsState
                        ),
                        sentTimeFull = "January 1, 1970 at 12:00 AM",
                        displayEmojiReactions = true,
                        verifiedUserSendFailure = VerifiedUserSendFailure.None,
                        actions = aTimelineItemActionList(
                            copyAction = null,
                        ),
                        recentEmojis = suggestedEmojis,
                    )
                ),
                anActionListState(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(
                            content = aTimelineItemAudioContent(),
                            timelineItemReactions = reactionsState
                        ),
                        sentTimeFull = "January 1, 1970 at 12:00 AM",
                        displayEmojiReactions = true,
                        verifiedUserSendFailure = VerifiedUserSendFailure.None,
                        actions = aTimelineItemActionList(
                            copyAction = TimelineItemAction.CopyCaption,
                        ),
                        recentEmojis = suggestedEmojis,
                    )
                ),
                anActionListState(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(
                            content = aTimelineItemVoiceContent(caption = null),
                            timelineItemReactions = reactionsState
                        ),
                        sentTimeFull = "January 1, 1970 at 12:00 AM",
                        displayEmojiReactions = true,
                        verifiedUserSendFailure = VerifiedUserSendFailure.None,
                        actions = aTimelineItemActionList(
                            copyAction = null,
                        ),
                        recentEmojis = suggestedEmojis,
                    )
                ),
                anActionListState(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(
                            content = aTimelineItemLocationContent(),
                            timelineItemReactions = reactionsState
                        ),
                        sentTimeFull = "January 1, 1970 at 12:00 AM",
                        displayEmojiReactions = true,
                        verifiedUserSendFailure = VerifiedUserSendFailure.None,
                        actions = aTimelineItemActionList(),
                        recentEmojis = suggestedEmojis,
                    )
                ),
                anActionListState(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(
                            content = aTimelineItemLocationContent(),
                            timelineItemReactions = reactionsState
                        ),
                        sentTimeFull = "January 1, 1970 at 12:00 AM",
                        displayEmojiReactions = false,
                        verifiedUserSendFailure = VerifiedUserSendFailure.None,
                        actions = aTimelineItemActionList(),
                        recentEmojis = suggestedEmojis,
                    ),
                ),
                anActionListState(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(
                            content = aTimelineItemPollContent(),
                            timelineItemReactions = reactionsState
                        ),
                        sentTimeFull = "January 1, 1970 at 12:00 AM",
                        displayEmojiReactions = false,
                        verifiedUserSendFailure = VerifiedUserSendFailure.None,
                        actions = aTimelineItemPollActionList(),
                        recentEmojis = suggestedEmojis,
                    ),
                ),
                anActionListState(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(
                            timelineItemReactions = reactionsState,
                            messageShield = MessageShield.UnknownDevice(isCritical = true)
                        ),
                        sentTimeFull = "January 1, 1970 at 12:00 AM",
                        displayEmojiReactions = true,
                        verifiedUserSendFailure = VerifiedUserSendFailure.None,
                        actions = aTimelineItemActionList(),
                        recentEmojis = suggestedEmojis,
                    )
                ),
                anActionListState(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(),
                        sentTimeFull = "January 1, 1970 at 12:00 AM",
                        displayEmojiReactions = true,
                        verifiedUserSendFailure = anUnsignedDeviceSendFailure(),
                        actions = aTimelineItemActionList(),
                        recentEmojis = suggestedEmojis,
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

fun aTimelineItemActionList(
    copyAction: TimelineItemAction? = TimelineItemAction.CopyText
): ImmutableList<TimelineItemAction> {
    return setOfNotNull(
        TimelineItemAction.Reply,
        TimelineItemAction.Forward,
        copyAction,
        TimelineItemAction.CopyLink,
        TimelineItemAction.Edit,
        TimelineItemAction.Redact,
        TimelineItemAction.ReportContent,
        TimelineItemAction.ViewSource,
    )
        .sortedWith(TimelineItemActionComparator())
        .toImmutableList()
}

fun aTimelineItemPollActionList(): ImmutableList<TimelineItemAction> {
    return setOf(
        TimelineItemAction.EndPoll,
        TimelineItemAction.EditPoll,
        TimelineItemAction.Reply,
        TimelineItemAction.Pin,
        TimelineItemAction.CopyLink,
        TimelineItemAction.Redact,
    )
        .sortedWith(TimelineItemActionComparator())
        .toImmutableList()
}
