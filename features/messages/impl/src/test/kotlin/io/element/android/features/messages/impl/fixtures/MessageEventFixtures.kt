/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.messages.impl.fixtures

import io.element.android.features.messages.impl.timeline.aTimelineItemDebugInfo
import io.element.android.features.messages.impl.timeline.aTimelineItemReactions
import io.element.android.features.messages.impl.timeline.model.ReadReceiptData
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.TimelineItemReadReceipts
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextContent
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import io.element.android.libraries.matrix.api.timeline.item.event.MessageShield
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_MESSAGE
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_NAME
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetails
import io.element.android.libraries.matrix.ui.messages.reply.aProfileTimelineDetailsReady
import kotlinx.collections.immutable.toImmutableList

internal fun aMessageEvent(
    eventId: EventId? = AN_EVENT_ID,
    transactionId: TransactionId? = null,
    isMine: Boolean = true,
    isEditable: Boolean = true,
    canBeRepliedTo: Boolean = true,
    content: TimelineItemEventContent = TimelineItemTextContent(body = A_MESSAGE, htmlDocument = null, formattedBody = null, isEdited = false),
    inReplyTo: InReplyToDetails? = null,
    isThreaded: Boolean = false,
    isPinned: Boolean = false,
    debugInfo: TimelineItemDebugInfo = aTimelineItemDebugInfo(),
    sendState: LocalEventSendState = LocalEventSendState.Sent(AN_EVENT_ID),
    messageShield: MessageShield? = null,
) = TimelineItem.Event(
    id = eventId?.value.orEmpty(),
    eventId = eventId,
    transactionId = transactionId,
    senderId = A_USER_ID,
    senderProfile = aProfileTimelineDetailsReady(displayName = A_USER_NAME),
    senderAvatar = AvatarData(A_USER_ID.value, A_USER_NAME, size = AvatarSize.TimelineSender),
    content = content,
    sentTime = "",
    isMine = isMine,
    isEditable = isEditable,
    canBeRepliedTo = canBeRepliedTo,
    reactionsState = aTimelineItemReactions(count = 0),
    readReceiptState = TimelineItemReadReceipts(emptyList<ReadReceiptData>().toImmutableList()),
    localSendState = sendState,
    inReplyTo = inReplyTo,
    debugInfo = debugInfo,
    isThreaded = isThreaded,
    isPinned = isPinned,
    origin = null,
    messageShield = messageShield,
)
