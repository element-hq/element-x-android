/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.actionlist

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
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
import kotlinx.collections.immutable.toPersistentList

open class ActionListStateProvider : PreviewParameterProvider<ActionListState> {
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
                        displayEmojiReactions = true,
                        verifiedUserSendFailure = VerifiedUserSendFailure.None,
                        actions = aTimelineItemActionList(),
                    )
                ),
                anActionListState(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(
                            content = aTimelineItemImageContent(),
                            displayNameAmbiguous = true,
                            timelineItemReactions = reactionsState,
                        ),
                        displayEmojiReactions = true,
                        verifiedUserSendFailure = VerifiedUserSendFailure.None,
                        actions = aTimelineItemActionList(
                            copyAction = TimelineItemAction.CopyCaption,
                        ),
                    )
                ),
                anActionListState(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(
                            content = aTimelineItemVideoContent(),
                            timelineItemReactions = reactionsState
                        ),
                        displayEmojiReactions = true,
                        verifiedUserSendFailure = VerifiedUserSendFailure.None,
                        actions = aTimelineItemActionList(
                            copyAction = TimelineItemAction.CopyCaption,
                        ),
                    )
                ),
                anActionListState(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(
                            content = aTimelineItemFileContent(),
                            timelineItemReactions = reactionsState
                        ),
                        displayEmojiReactions = true,
                        verifiedUserSendFailure = VerifiedUserSendFailure.None,
                        actions = aTimelineItemActionList(
                            copyAction = null,
                        ),
                    )
                ),
                anActionListState(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(
                            content = aTimelineItemAudioContent(),
                            timelineItemReactions = reactionsState
                        ),
                        displayEmojiReactions = true,
                        verifiedUserSendFailure = VerifiedUserSendFailure.None,
                        actions = aTimelineItemActionList(
                            copyAction = TimelineItemAction.CopyCaption,
                        ),
                    )
                ),
                anActionListState(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(
                            content = aTimelineItemVoiceContent(caption = null),
                            timelineItemReactions = reactionsState
                        ),
                        displayEmojiReactions = true,
                        verifiedUserSendFailure = VerifiedUserSendFailure.None,
                        actions = aTimelineItemActionList(
                            copyAction = null,
                        ),
                    )
                ),
                anActionListState(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(
                            content = aTimelineItemLocationContent(),
                            timelineItemReactions = reactionsState
                        ),
                        displayEmojiReactions = true,
                        verifiedUserSendFailure = VerifiedUserSendFailure.None,
                        actions = aTimelineItemActionList(),
                    )
                ),
                anActionListState(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(
                            content = aTimelineItemLocationContent(),
                            timelineItemReactions = reactionsState
                        ),
                        displayEmojiReactions = false,
                        verifiedUserSendFailure = VerifiedUserSendFailure.None,
                        actions = aTimelineItemActionList(),
                    ),
                ),
                anActionListState(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(
                            content = aTimelineItemPollContent(),
                            timelineItemReactions = reactionsState
                        ),
                        displayEmojiReactions = false,
                        verifiedUserSendFailure = VerifiedUserSendFailure.None,
                        actions = aTimelineItemPollActionList(),
                    ),
                ),
                anActionListState(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(
                            timelineItemReactions = reactionsState,
                            messageShield = MessageShield.UnknownDevice(isCritical = true)
                        ),
                        displayEmojiReactions = true,
                        verifiedUserSendFailure = VerifiedUserSendFailure.None,
                        actions = aTimelineItemActionList(),
                    )
                ),
                anActionListState(
                    target = ActionListState.Target.Success(
                        event = aTimelineItemEvent(),
                        displayEmojiReactions = true,
                        verifiedUserSendFailure = anUnsignedDeviceSendFailure(),
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

fun aTimelineItemActionList(
    copyAction: TimelineItemAction? = TimelineItemAction.CopyText
): ImmutableList<TimelineItemAction> {
    return listOfNotNull(
        TimelineItemAction.Reply,
        TimelineItemAction.Forward,
        copyAction,
        TimelineItemAction.CopyLink,
        TimelineItemAction.Edit,
        TimelineItemAction.Redact,
        TimelineItemAction.ReportContent,
        TimelineItemAction.ViewSource,
    ).toPersistentList()
}

fun aTimelineItemPollActionList(): ImmutableList<TimelineItemAction> {
    return persistentListOf(
        TimelineItemAction.EndPoll,
        TimelineItemAction.Reply,
        TimelineItemAction.CopyText,
        TimelineItemAction.CopyLink,
        TimelineItemAction.ViewSource,
        TimelineItemAction.ReportContent,
        TimelineItemAction.Redact,
    )
}
