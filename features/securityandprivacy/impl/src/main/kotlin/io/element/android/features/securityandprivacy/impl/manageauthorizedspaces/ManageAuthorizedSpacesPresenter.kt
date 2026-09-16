/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.manageauthorizedspaces

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter
import kotlinx.collections.immutable.toImmutableList

@Inject
class ManageAuthorizedSpacesPresenter(
    private val spaceSelectionStateHolder: SpaceSelectionStateHolder,
) : Presenter<ManageAuthorizedSpacesState> {
    @Composable
    override fun present(): ManageAuthorizedSpacesState {
        val spaceSelectionState by spaceSelectionStateHolder.state.collectAsState()
        fun handleEvent(event: ManageAuthorizedSpacesEvent) {
            when (event) {
                is ManageAuthorizedSpacesEvent.ToggleSpace -> {
                    val currentSelectedIds = spaceSelectionState.selectedSpaceIds
                    val newSelectedIds = if (currentSelectedIds.contains(event.roomId)) {
                        currentSelectedIds.minus(event.roomId).toImmutableList()
                    } else {
                        currentSelectedIds.plus(event.roomId).toImmutableList()
                    }
                    spaceSelectionStateHolder.updateSelectedSpaceIds(newSelectedIds)
                }
                ManageAuthorizedSpacesEvent.Done -> {
                    spaceSelectionStateHolder.setCompletion(SpaceSelectionState.Completion.Completed)
                }
                ManageAuthorizedSpacesEvent.Cancel -> {
                    spaceSelectionStateHolder.setCompletion(SpaceSelectionState.Completion.Cancelled)
                }
            }
        }

        return ManageAuthorizedSpacesState(
            selectableSpaces = spaceSelectionState.selectableSpaces,
            unknownSpaceIds = spaceSelectionState.unknownSpaceIds,
            selectedIds = spaceSelectionState.selectedSpaceIds,
            eventSink = ::handleEvent,
        )
    }
}
