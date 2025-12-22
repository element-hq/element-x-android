/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appconfig

import io.element.android.libraries.matrix.api.room.StateEventType

object TimelineConfig {
    const val MAX_READ_RECEIPT_TO_DISPLAY = 3

    /**
     * Event types that will be filtered out from the timeline (i.e. not displayed).
     */
    val excludedEvents = listOf(
        StateEventType.CallMember,
        StateEventType.RoomAliases,
        StateEventType.RoomCanonicalAlias,
        StateEventType.RoomGuestAccess,
        StateEventType.RoomHistoryVisibility,
        StateEventType.RoomJoinRules,
        StateEventType.RoomPowerLevels,
        StateEventType.RoomServerAcl,
        StateEventType.RoomTombstone,
        StateEventType.SpaceChild,
        StateEventType.SpaceParent,
        StateEventType.PolicyRuleRoom,
        StateEventType.PolicyRuleServer,
        StateEventType.PolicyRuleUser,
    )
}
