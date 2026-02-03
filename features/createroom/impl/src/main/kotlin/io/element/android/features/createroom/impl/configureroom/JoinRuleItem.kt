/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.configureroom

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.join.AllowRule
import io.element.android.libraries.matrix.api.room.join.JoinRule
import kotlinx.collections.immutable.persistentListOf

/**
 * Join rule items to display in UI.
 */
@Immutable
sealed interface JoinRuleItem {
    data object Private : JoinRuleItem

    /**
     * Those join rule items that represent public visibility of the room/space.
     */
    @Immutable
    sealed interface PublicVisibility : JoinRuleItem {
        data object Public : PublicVisibility
        data object AskToJoin : PublicVisibility
        data class Restricted(val parentSpaceId: RoomId) : PublicVisibility
        data class AskToJoinRestricted(val parentSpaceId: RoomId) : PublicVisibility
    }

    /**
     * Transforms a [JoinRuleItem] option into a [JoinRule].
     */
    fun toJoinRule(): JoinRule = when (this) {
        Private -> JoinRule.Invite
        PublicVisibility.Public -> JoinRule.Public
        PublicVisibility.AskToJoin -> JoinRule.Knock
        is PublicVisibility.Restricted -> JoinRule.Restricted(persistentListOf(AllowRule.RoomMembership(parentSpaceId)))
        is PublicVisibility.AskToJoinRestricted -> JoinRule.KnockRestricted(persistentListOf(AllowRule.RoomMembership(parentSpaceId)))
    }
}
