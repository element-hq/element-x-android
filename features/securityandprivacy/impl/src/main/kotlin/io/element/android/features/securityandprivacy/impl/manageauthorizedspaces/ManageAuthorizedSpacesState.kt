/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.manageauthorizedspaces

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet

data class ManageAuthorizedSpacesState(
    val selectableSpaces: ImmutableSet<SpaceRoom>,
    val unknownSpaceIds: ImmutableList<RoomId>,
    val selectedIds: ImmutableList<RoomId>,
    val eventSink: (ManageAuthorizedSpacesEvent) -> Unit
) {
    val isDoneButtonEnabled = selectedIds.isNotEmpty()
}
