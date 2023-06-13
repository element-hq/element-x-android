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

import io.element.android.libraries.eventformatter.impl.isme.IsMe
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import io.element.android.services.toolbox.api.strings.StringProvider
import timber.log.Timber
import javax.inject.Inject

class RoomMembershipContentFormatter @Inject constructor(
    private val isMe: IsMe,
    private val sp: StringProvider,
) {
    fun format(
        membershipContent: RoomMembershipContent,
        senderDisplayName: String,
        senderIsYou: Boolean,
    ): CharSequence? {
        val userId = membershipContent.userId
        val memberIsYou = isMe(userId)
        return when (val change = membershipContent.change) {
            MembershipChange.JOINED -> if (memberIsYou) {
                sp.getString(R.string.state_event_room_join_by_you)
            } else {
                sp.getString(R.string.state_event_room_join, userId.value)
            }
            MembershipChange.LEFT -> if (memberIsYou) {
                sp.getString(R.string.state_event_room_leave_by_you)
            } else {
                sp.getString(R.string.state_event_room_leave, userId.value)
            }
            MembershipChange.BANNED, MembershipChange.KICKED_AND_BANNED -> if (senderIsYou) {
                sp.getString(R.string.state_event_room_ban_by_you, userId.value)
            } else {
                sp.getString(R.string.state_event_room_ban, senderDisplayName, userId.value)
            }
            MembershipChange.UNBANNED -> if (senderIsYou) {
                sp.getString(R.string.state_event_room_unban_by_you, userId.value)
            } else {
                sp.getString(R.string.state_event_room_unban, senderDisplayName, userId.value)
            }
            MembershipChange.KICKED -> if (senderIsYou) {
                sp.getString(R.string.state_event_room_remove_by_you, userId.value)
            } else {
                sp.getString(R.string.state_event_room_remove, senderDisplayName, userId.value)
            }
            MembershipChange.INVITED -> if (senderIsYou) {
                sp.getString(R.string.state_event_room_invite_by_you, userId.value)
            } else if (memberIsYou) {
                sp.getString(R.string.state_event_room_invite_you, senderDisplayName)
            } else {
                sp.getString(R.string.state_event_room_invite, senderDisplayName, userId.value)
            }
            MembershipChange.INVITATION_ACCEPTED -> if (memberIsYou) {
                sp.getString(R.string.state_event_room_invite_accepted_by_you)
            } else {
                sp.getString(R.string.state_event_room_invite_accepted, userId.value)
            }
            MembershipChange.INVITATION_REJECTED -> if (memberIsYou) {
                sp.getString(R.string.state_event_room_reject_by_you)
            } else {
                sp.getString(R.string.state_event_room_reject, userId.value)
            }
            MembershipChange.INVITATION_REVOKED -> if (senderIsYou) {
                sp.getString(R.string.state_event_room_third_party_revoked_invite_by_you, userId.value)
            } else {
                sp.getString(R.string.state_event_room_third_party_revoked_invite, senderDisplayName, userId.value)
            }
            MembershipChange.KNOCKED -> if (memberIsYou) {
                sp.getString(R.string.state_event_room_knock_by_you)
            } else {
                sp.getString(R.string.state_event_room_knock, userId.value)
            }
            MembershipChange.KNOCK_ACCEPTED -> if (senderIsYou) {
                sp.getString(R.string.state_event_room_knock_accepted_by_you, userId.value)
            } else {
                sp.getString(R.string.state_event_room_knock_accepted, senderDisplayName, userId.value)
            }
            MembershipChange.KNOCK_RETRACTED -> if (memberIsYou) {
                sp.getString(R.string.state_event_room_knock_retracted_by_you)
            } else {
                sp.getString(R.string.state_event_room_knock_retracted, userId.value)
            }
            MembershipChange.KNOCK_DENIED -> if (senderIsYou) {
                sp.getString(R.string.state_event_room_knock_denied_by_you, userId.value)
            } else if (memberIsYou) {
                sp.getString(R.string.state_event_room_knock_denied_you, senderDisplayName)
            } else {
                sp.getString(R.string.state_event_room_knock_denied, senderDisplayName, userId.value)
            }
            else -> {
                Timber.v("Filtering timeline item for room membership: $membershipContent")
                null
            }
        }
    }
}
