/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.manageauthorizedspaces

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SpaceSelectionState(
    val selectableSpaces: ImmutableSet<SpaceRoom>,
    val unknownSpaceIds: ImmutableList<RoomId>,
    val selectedSpaceIds: ImmutableList<RoomId>,
    val completion: Completion,
) {
    enum class Completion {
        Initial,
        Completed,
        Cancelled,
    }

    companion object {
        val INITIAL = SpaceSelectionState(
            selectableSpaces = persistentSetOf(),
            unknownSpaceIds = persistentListOf(),
            selectedSpaceIds = persistentListOf(),
            completion = Completion.Initial,
        )
    }
}

@Inject
@SingleIn(RoomScope::class)
class SpaceSelectionStateHolder {
    private val _state = MutableStateFlow(SpaceSelectionState.INITIAL)
    val state: StateFlow<SpaceSelectionState> = _state.asStateFlow()

    fun update(transform: (SpaceSelectionState) -> SpaceSelectionState) {
        _state.update(transform)
    }

    fun updateSelectedSpaceIds(selectedSpaceIds: ImmutableList<RoomId>) {
        update { it.copy(selectedSpaceIds = selectedSpaceIds) }
    }

    fun setCompletion(completion: SpaceSelectionState.Completion) {
        update { it.copy(completion = completion) }
    }
}
