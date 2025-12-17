/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.eventformatter.impl

import dev.zacsweers.metro.Inject
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import io.element.android.services.toolbox.api.strings.StringProvider
import timber.log.Timber

@Inject
class RoomMembershipContentFormatter(
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
        val reason = membershipContent.reason?.takeIf { it.isNotBlank() }
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
                if (reason != null) {
                    sp.getString(R.string.state_event_room_ban_by_you_with_reason, userDisplayNameOrId, reason)
                } else {
                    sp.getString(R.string.state_event_room_ban_by_you, userDisplayNameOrId)
                }
            } else {
                if (reason != null) {
                    sp.getString(R.string.state_event_room_ban_with_reason, senderDisambiguatedDisplayName, userDisplayNameOrId, reason)
                } else {
                    sp.getString(R.string.state_event_room_ban, senderDisambiguatedDisplayName, userDisplayNameOrId)
                }
            }
            MembershipChange.UNBANNED -> if (senderIsYou) {
                sp.getString(R.string.state_event_room_unban_by_you, userDisplayNameOrId)
            } else {
                sp.getString(R.string.state_event_room_unban, senderDisambiguatedDisplayName, userDisplayNameOrId)
            }
            MembershipChange.KICKED -> if (senderIsYou) {
                if (reason != null) {
                    sp.getString(R.string.state_event_room_remove_by_you_with_reason, userDisplayNameOrId, reason)
                } else {
                    sp.getString(R.string.state_event_room_remove_by_you, userDisplayNameOrId)
                }
            } else {
                if (reason != null) {
                    sp.getString(R.string.state_event_room_remove_with_reason, senderDisambiguatedDisplayName, userDisplayNameOrId, reason)
                } else {
                    sp.getString(R.string.state_event_room_remove, senderDisambiguatedDisplayName, userDisplayNameOrId)
                }
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
                sp.getString(R.string.state_event_room_knock_accepted, senderDisambiguatedDisplayName, userDisplayNameOrId)
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
                sp.getString(R.string.state_event_room_knock_denied, senderDisambiguatedDisplayName, userDisplayNameOrId)
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
