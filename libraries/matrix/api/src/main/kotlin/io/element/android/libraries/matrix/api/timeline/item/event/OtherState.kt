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
    data object RoomPinnedEvents : OtherState
    data object RoomPowerLevels : OtherState
    data object RoomServerAcl : OtherState
    data class RoomThirdPartyInvite(val displayName: String?) : OtherState
    data object RoomTombstone : OtherState
    data class RoomTopic(val topic: String?) : OtherState
    data object SpaceChild : OtherState
    data object SpaceParent : OtherState
    data class Custom(val eventType: String) : OtherState

    fun isVisibleInTimeline() = when (this) {
        // Visible
        is RoomAvatar,
        is RoomName,
        is RoomTopic,
        is RoomThirdPartyInvite,
        is RoomCreate,
        is RoomEncryption,
        is Custom -> true
        // Hidden
        is RoomAliases,
        is RoomCanonicalAlias,
        is RoomGuestAccess,
        is RoomHistoryVisibility,
        is RoomJoinRules,
        is RoomPinnedEvents,
        is RoomPowerLevels,
        is RoomServerAcl,
        is RoomTombstone,
        is SpaceChild,
        is SpaceParent,
        is PolicyRuleRoom,
        is PolicyRuleServer,
        is PolicyRuleUser -> false
    }
}
