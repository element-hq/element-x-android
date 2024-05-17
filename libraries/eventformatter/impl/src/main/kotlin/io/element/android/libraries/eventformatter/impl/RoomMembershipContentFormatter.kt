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

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import io.element.android.services.toolbox.api.strings.StringProvider
import timber.log.Timber
import javax.inject.Inject

class RoomMembershipContentFormatter @Inject constructor(
    private val matrixClient: MatrixClient,
    private val sp: StringProvider,
) {
    fun format(
        membershipContent: RoomMembershipContent,
        senderDisambiguatedDisplayName: String,
        senderIsYou: Boolean,
    ): CharSequence? {
        val userId = membershipContent.userId
        val memberIsYou = matrixClient.isMe(userId)
        val userDisplayNameOrId = membershipContent.userDisplayName ?: userId.value
        return when (membershipContent.change) {
            MembershipChange.JOINED -> if (memberIsYou) {
                sp.getString(R.string.state_event_room_join_by_you)
            } else {
                sp.getString(R.string.state_event_room_join, senderDisambiguatedDisplayName)
            }
            MembershipChange.LEFT -> if (memberIsYou) {
                sp.getString(R.string.state_event_room_leave_by_you)
            } else {
                sp.getString(R.string.state_event_room_leave, senderDisambiguatedDisplayName)
            }
            MembershipChange.BANNED, MembershipChange.KICKED_AND_BANNED -> if (senderIsYou) {
                sp.getString(R.string.state_event_room_ban_by_you, userDisplayNameOrId)
            } else {
                sp.getString(R.string.state_event_room_ban, senderDisambiguatedDisplayName, userId.value)
            }
            MembershipChange.UNBANNED -> if (senderIsYou) {
                sp.getString(R.string.state_event_room_unban_by_you, userId.value)
            } else {
                sp.getString(R.string.state_event_room_unban, senderDisambiguatedDisplayName, userDisplayNameOrId)
            }
            MembershipChange.KICKED -> if (senderIsYou) {
                sp.getString(R.string.state_event_room_remove_by_you, userId.value)
            } else {
                sp.getString(R.string.state_event_room_remove, senderDisambiguatedDisplayName, userDisplayNameOrId)
            }
            MembershipChange.INVITED -> if (senderIsYou) {
                sp.getString(R.string.state_event_room_invite_by_you, userDisplayNameOrId)
            } else if (memberIsYou) {
                sp.getString(R.string.state_event_room_invite_you, senderDisambiguatedDisplayName)
            } else {
                sp.getString(R.string.state_event_room_invite, senderDisambiguatedDisplayName, userDisplayNameOrId)
            }
            MembershipChange.INVITATION_ACCEPTED -> if (memberIsYou) {
                sp.getString(R.string.state_event_room_invite_accepted_by_you)
            } else {
                sp.getString(R.string.state_event_room_invite_accepted, userDisplayNameOrId)
            }
            MembershipChange.INVITATION_REJECTED -> if (memberIsYou) {
                sp.getString(R.string.state_event_room_reject_by_you)
            } else {
                sp.getString(R.string.state_event_room_reject, userDisplayNameOrId)
            }
            MembershipChange.INVITATION_REVOKED -> if (senderIsYou) {
                sp.getString(R.string.state_event_room_third_party_revoked_invite_by_you, userDisplayNameOrId)
            } else {
                sp.getString(R.string.state_event_room_third_party_revoked_invite, senderDisambiguatedDisplayName, userDisplayNameOrId)
            }
            MembershipChange.KNOCKED -> if (memberIsYou) {
                sp.getString(R.string.state_event_room_knock_by_you)
            } else {
                sp.getString(R.string.state_event_room_knock, senderDisambiguatedDisplayName)
            }
            MembershipChange.KNOCK_ACCEPTED -> if (senderIsYou) {
                sp.getString(R.string.state_event_room_knock_accepted_by_you, userDisplayNameOrId)
            } else {
                sp.getString(R.string.state_event_room_knock_accepted, senderDisambiguatedDisplayName, userId.value)
            }
            MembershipChange.KNOCK_RETRACTED -> if (memberIsYou) {
                sp.getString(R.string.state_event_room_knock_retracted_by_you)
            } else {
                sp.getString(R.string.state_event_room_knock_retracted, senderDisambiguatedDisplayName)
            }
            MembershipChange.KNOCK_DENIED -> if (senderIsYou) {
                sp.getString(R.string.state_event_room_knock_denied_by_you, userDisplayNameOrId)
            } else if (memberIsYou) {
                sp.getString(R.string.state_event_room_knock_denied_you, senderDisambiguatedDisplayName)
            } else {
                sp.getString(R.string.state_event_room_knock_denied, senderDisambiguatedDisplayName, userId.value)
            }
            MembershipChange.NONE -> if (senderIsYou) {
                sp.getString(R.string.state_event_room_none_by_you)
            } else {
                sp.getString(R.string.state_event_room_none, senderDisambiguatedDisplayName)
            }
            MembershipChange.ERROR -> {
                Timber.v("Filtering timeline item for room membership: $membershipContent")
                null
            }
            MembershipChange.NOT_IMPLEMENTED -> {
                Timber.v("Filtering timeline item for room membership: $membershipContent")
                null
            }
            null -> {
                Timber.v("Filtering timeline item for room membership: $membershipContent")
                null
            }
        }
    }
}
