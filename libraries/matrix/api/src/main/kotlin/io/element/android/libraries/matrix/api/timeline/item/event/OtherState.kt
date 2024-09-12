/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.timeline.item.event

import androidx.compose.runtime.Immutable

@Immutable
sealed interface OtherState {
    data object PolicyRuleRoom : OtherState
    data object PolicyRuleServer : OtherState
    data object PolicyRuleUser : OtherState
    data object RoomAliases : OtherState
    data class RoomAvatar(val url: String?) : OtherState
    data object RoomCanonicalAlias : OtherState
    data object RoomCreate : OtherState
    data object RoomEncryption : OtherState
    data object RoomGuestAccess : OtherState
    data object RoomHistoryVisibility : OtherState
    data object RoomJoinRules : OtherState
    data class RoomName(val name: String?) : OtherState
    data class RoomPinnedEvents(val change: Change) : OtherState {
        enum class Change {
            ADDED,
            REMOVED,
            CHANGED
        }
    }

    data class RoomUserPowerLevels(val users: Map<String, Long>) : OtherState
    data object RoomServerAcl : OtherState
    data class RoomThirdPartyInvite(val displayName: String?) : OtherState
    data object RoomTombstone : OtherState
    data class RoomTopic(val topic: String?) : OtherState
    data object SpaceChild : OtherState
    data object SpaceParent : OtherState
    data class Custom(val eventType: String) : OtherState
}
