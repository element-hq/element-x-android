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

package io.element.android.libraries.eventformatter.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.eventformatter.api.TimelineEventFormatter
import io.element.android.libraries.eventformatter.impl.mode.RenderingMode
import io.element.android.libraries.matrix.api.timeline.item.event.CallNotifyContent
import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseMessageLikeContent
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseStateContent
import io.element.android.libraries.matrix.api.timeline.item.event.LegacyCallInviteContent
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileChangeContent
import io.element.android.libraries.matrix.api.timeline.item.event.RedactedContent
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import io.element.android.libraries.matrix.api.timeline.item.event.StateContent
import io.element.android.libraries.matrix.api.timeline.item.event.StickerContent
import io.element.android.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import io.element.android.libraries.matrix.api.timeline.item.event.UnknownContent
import io.element.android.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.toolbox.api.strings.StringProvider
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class DefaultTimelineEventFormatter @Inject constructor(
    private val sp: StringProvider,
    private val buildMeta: BuildMeta,
    private val roomMembershipContentFormatter: RoomMembershipContentFormatter,
    private val profileChangeContentFormatter: ProfileChangeContentFormatter,
    private val stateContentFormatter: StateContentFormatter,
) : TimelineEventFormatter {
    override fun format(event: EventTimelineItem): CharSequence? {
        val isOutgoing = event.isOwn
        val senderDisambiguatedDisplayName = event.senderProfile.getDisambiguatedDisplayName(event.sender)
        return when (val content = event.content) {
            is RoomMembershipContent -> {
                roomMembershipContentFormatter.format(content, senderDisambiguatedDisplayName, isOutgoing)
            }
            is ProfileChangeContent -> {
                profileChangeContentFormatter.format(content, event.sender, senderDisambiguatedDisplayName, isOutgoing)
            }
            is StateContent -> {
                stateContentFormatter.format(content, senderDisambiguatedDisplayName, isOutgoing, RenderingMode.Timeline)
            }
            is LegacyCallInviteContent -> {
                sp.getString(CommonStrings.common_call_invite)
            }
            is CallNotifyContent -> {
                sp.getString(CommonStrings.common_call_started)
            }
            RedactedContent,
            is StickerContent,
            is PollContent,
            is UnableToDecryptContent,
            is MessageContent,
            is FailedToParseMessageLikeContent,
            is FailedToParseStateContent,
            is UnknownContent -> {
                if (buildMeta.isDebuggable) {
                    error("You should not use this formatter for this event: $event")
                }
                sp.getString(CommonStrings.common_unsupported_event)
            }
        }
    }
}
