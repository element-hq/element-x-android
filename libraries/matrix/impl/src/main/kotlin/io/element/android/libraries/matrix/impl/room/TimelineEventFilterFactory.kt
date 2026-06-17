/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.api.room.join.JoinRule
import org.matrix.rustcomponents.sdk.FilterTimelineEventCondition
import org.matrix.rustcomponents.sdk.FilterTimelineEventType
import org.matrix.rustcomponents.sdk.TimelineEventFilter
import uniffi.matrix_sdk_ui.MembershipChangeFilter

interface TimelineEventFilterFactory {
    fun create(
        joinRule: JoinRule?,
        isEncrypted: Boolean?,
        excludedStateTypes: List<StateEventType>
    ): TimelineEventFilter?
}

@ContributesBinding(AppScope::class)
class RustTimelineEventFilterFactory : TimelineEventFilterFactory {
    override fun create(
        joinRule: JoinRule?,
        isEncrypted: Boolean?,
        excludedStateTypes: List<StateEventType>
    ): TimelineEventFilter? {
        val excludedEventTypes = excludedStateTypes.map {
            FilterTimelineEventCondition.EventType(FilterTimelineEventType.State(it.map()))
        }
        // If the room is publicly joinable and not encrypted, we also want to exclude membership changes and profile changes,
        // as they will pollute the timelines since they're quite common and not add much value.
        val excludedMembershipChanges = if (joinRule !is JoinRule.Invite && isEncrypted == false) {
            listOf(
                FilterTimelineEventCondition.MembershipChange(MembershipChangeFilter.JOIN),
                FilterTimelineEventCondition.MembershipChange(MembershipChangeFilter.LEAVE),
                FilterTimelineEventCondition.ProfileChange,
            )
        } else {
            emptyList()
        }
        return if (excludedEventTypes.isNotEmpty() || excludedMembershipChanges.isNotEmpty()) {
            TimelineEventFilter.exclude(excludedEventTypes + excludedMembershipChanges)
        } else {
            null
        }
    }
}
