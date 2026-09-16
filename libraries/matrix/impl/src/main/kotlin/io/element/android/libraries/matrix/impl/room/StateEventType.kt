/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.matrix.api.room.StateEventType
import uniffi.ruma_events.TimelineEventType
import uniffi.ruma_events.stateEventTypeFromString
import uniffi.ruma_events.timelineEventTypeFromString
import uniffi.ruma_events.StateEventType as RustStateEventType

fun StateEventType.map(): RustStateEventType = when (this) {
    StateEventType.PolicyRuleRoom -> RustStateEventType.PolicyRuleRoom
    StateEventType.PolicyRuleServer -> RustStateEventType.PolicyRuleServer
    StateEventType.PolicyRuleUser -> RustStateEventType.PolicyRuleUser
    StateEventType.CallMember -> RustStateEventType.CallMember
    StateEventType.RoomAvatar -> RustStateEventType.RoomAvatar
    StateEventType.RoomCanonicalAlias -> RustStateEventType.RoomCanonicalAlias
    StateEventType.RoomCreate -> RustStateEventType.RoomCreate
    StateEventType.RoomEncryption -> RustStateEventType.RoomEncryption
    StateEventType.RoomGuestAccess -> RustStateEventType.RoomGuestAccess
    StateEventType.RoomHistoryVisibility -> RustStateEventType.RoomHistoryVisibility
    StateEventType.RoomJoinRules -> RustStateEventType.RoomJoinRules
    StateEventType.RoomMemberEvent -> RustStateEventType.RoomMember
    StateEventType.RoomName -> RustStateEventType.RoomName
    StateEventType.RoomPinnedEvents -> RustStateEventType.RoomPinnedEvents
    StateEventType.RoomPowerLevels -> RustStateEventType.RoomPowerLevels
    StateEventType.RoomServerAcl -> RustStateEventType.RoomServerAcl
    StateEventType.RoomThirdPartyInvite -> RustStateEventType.RoomThirdPartyInvite
    StateEventType.RoomTombstone -> RustStateEventType.RoomTombstone
    StateEventType.RoomTopic -> RustStateEventType.RoomTopic
    StateEventType.SpaceChild -> RustStateEventType.SpaceChild
    StateEventType.SpaceParent -> RustStateEventType.SpaceParent
    StateEventType.BeaconInfo -> RustStateEventType.BeaconInfo
    StateEventType.MemberHints -> RustStateEventType.MemberHints
    StateEventType.RoomImagePack -> RustStateEventType.RoomImagePack
    StateEventType.RoomLanguage -> RustStateEventType.RoomLanguage
    StateEventType.RoomPolicy -> RustStateEventType.RoomPolicy
    StateEventType.RoomRetention -> RustStateEventType.RoomRetention
    is StateEventType.Custom -> stateEventTypeFromString(type)
}

fun RustStateEventType.map(): StateEventType = when (this) {
    RustStateEventType.PolicyRuleRoom -> StateEventType.PolicyRuleRoom
    RustStateEventType.PolicyRuleServer -> StateEventType.PolicyRuleServer
    RustStateEventType.PolicyRuleUser -> StateEventType.PolicyRuleUser
    RustStateEventType.CallMember -> StateEventType.CallMember
    RustStateEventType.RoomAvatar -> StateEventType.RoomAvatar
    RustStateEventType.RoomCanonicalAlias -> StateEventType.RoomCanonicalAlias
    RustStateEventType.RoomCreate -> StateEventType.RoomCreate
    RustStateEventType.RoomEncryption -> StateEventType.RoomEncryption
    RustStateEventType.RoomGuestAccess -> StateEventType.RoomGuestAccess
    RustStateEventType.RoomHistoryVisibility -> StateEventType.RoomHistoryVisibility
    RustStateEventType.RoomJoinRules -> StateEventType.RoomJoinRules
    RustStateEventType.RoomMember -> StateEventType.RoomMemberEvent
    RustStateEventType.RoomName -> StateEventType.RoomName
    RustStateEventType.RoomPinnedEvents -> StateEventType.RoomPinnedEvents
    RustStateEventType.RoomPowerLevels -> StateEventType.RoomPowerLevels
    RustStateEventType.RoomServerAcl -> StateEventType.RoomServerAcl
    RustStateEventType.RoomThirdPartyInvite -> StateEventType.RoomThirdPartyInvite
    RustStateEventType.RoomTombstone -> StateEventType.RoomTombstone
    RustStateEventType.RoomTopic -> StateEventType.RoomTopic
    RustStateEventType.SpaceChild -> StateEventType.SpaceChild
    RustStateEventType.SpaceParent -> StateEventType.SpaceParent
    RustStateEventType.BeaconInfo -> StateEventType.BeaconInfo
    RustStateEventType.MemberHints -> StateEventType.MemberHints
    RustStateEventType.RoomImagePack -> StateEventType.RoomImagePack
    RustStateEventType.RoomLanguage -> StateEventType.RoomLanguage
    RustStateEventType.RoomPolicy -> StateEventType.RoomPolicy
    RustStateEventType.RoomRetention -> StateEventType.RoomRetention
    is RustStateEventType.Custom -> StateEventType.Custom(this.toString())
}

fun TimelineEventType.Companion.fromState(stateEventType: StateEventType): TimelineEventType = when (stateEventType) {
    StateEventType.PolicyRuleRoom -> TimelineEventType.PolicyRuleRoom
    StateEventType.PolicyRuleServer -> TimelineEventType.PolicyRuleServer
    StateEventType.PolicyRuleUser -> TimelineEventType.PolicyRuleUser
    StateEventType.CallMember -> TimelineEventType.CallMember
    StateEventType.RoomAvatar -> TimelineEventType.RoomAvatar
    StateEventType.RoomCanonicalAlias -> TimelineEventType.RoomCanonicalAlias
    StateEventType.RoomCreate -> TimelineEventType.RoomCreate
    StateEventType.RoomEncryption -> TimelineEventType.RoomEncryption
    StateEventType.RoomGuestAccess -> TimelineEventType.RoomGuestAccess
    StateEventType.RoomHistoryVisibility -> TimelineEventType.RoomHistoryVisibility
    StateEventType.RoomJoinRules -> TimelineEventType.RoomJoinRules
    StateEventType.RoomMemberEvent -> TimelineEventType.RoomMember
    StateEventType.RoomName -> TimelineEventType.RoomName
    StateEventType.RoomPinnedEvents -> TimelineEventType.RoomPinnedEvents
    StateEventType.RoomPowerLevels -> TimelineEventType.RoomPowerLevels
    StateEventType.RoomServerAcl -> TimelineEventType.RoomServerAcl
    StateEventType.RoomThirdPartyInvite -> TimelineEventType.RoomThirdPartyInvite
    StateEventType.RoomTombstone -> TimelineEventType.RoomTombstone
    StateEventType.RoomTopic -> TimelineEventType.RoomTopic
    StateEventType.SpaceChild -> TimelineEventType.SpaceChild
    StateEventType.SpaceParent -> TimelineEventType.SpaceParent
    StateEventType.BeaconInfo -> TimelineEventType.BeaconInfo
    StateEventType.MemberHints -> TimelineEventType.MemberHints
    StateEventType.RoomImagePack -> TimelineEventType.RoomImagePack
    StateEventType.RoomLanguage -> TimelineEventType.RoomLanguage
    StateEventType.RoomPolicy -> TimelineEventType.RoomPolicy
    StateEventType.RoomRetention -> TimelineEventType.RoomRetention
    is StateEventType.Custom -> timelineEventTypeFromString(stateEventType.type)
}
