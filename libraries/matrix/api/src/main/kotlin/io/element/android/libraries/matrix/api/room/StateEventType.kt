/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

sealed interface StateEventType {
    data object PolicyRuleRoom : StateEventType
    data object PolicyRuleServer : StateEventType
    data object PolicyRuleUser : StateEventType
    data object CallMember : StateEventType
    data object RoomAliases : StateEventType
    data object RoomAvatar : StateEventType
    data object RoomCanonicalAlias : StateEventType
    data object RoomCreate : StateEventType
    data object RoomEncryption : StateEventType
    data object RoomGuestAccess : StateEventType
    data object RoomHistoryVisibility : StateEventType
    data object RoomJoinRules : StateEventType
    data object RoomMemberEvent : StateEventType
    data object RoomName : StateEventType
    data object RoomPinnedEvents : StateEventType
    data object RoomPowerLevels : StateEventType
    data object RoomServerAcl : StateEventType
    data object RoomThirdPartyInvite : StateEventType
    data object RoomTombstone : StateEventType
    data object RoomTopic : StateEventType
    data object SpaceChild : StateEventType
    data object SpaceParent : StateEventType
    data object BeaconInfo : StateEventType
    data object MemberHints : StateEventType
    data object RoomImagePack : StateEventType
    data object RoomLanguage : StateEventType

    data class Custom(val type: String) : StateEventType
}
