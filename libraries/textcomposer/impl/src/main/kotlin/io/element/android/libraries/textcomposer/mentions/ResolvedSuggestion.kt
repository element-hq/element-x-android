/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.mentions

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.roomlist.RoomSummary

@Immutable
sealed interface ResolvedSuggestion {
    data object AtRoom : ResolvedSuggestion
    data class Member(val roomMember: RoomMember) : ResolvedSuggestion
    data class Alias(val roomAlias: RoomAlias, val roomSummary: RoomSummary) : ResolvedSuggestion
}
