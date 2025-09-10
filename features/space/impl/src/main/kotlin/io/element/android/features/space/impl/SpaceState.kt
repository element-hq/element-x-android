/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet

data class SpaceState(
    val currentSpace: SpaceRoom?,
    val children: ImmutableList<SpaceRoom>,
    val seenSpaceInvites: ImmutableSet<RoomId>,
    val hideInvitesAvatar: Boolean,
    val hasMoreToLoad: Boolean,
    val eventSink: (SpaceEvents) -> Unit
)
