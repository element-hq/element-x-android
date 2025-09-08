/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

open class SpaceStateProvider : PreviewParameterProvider<SpaceState> {
    override val values: Sequence<SpaceState>
        get() = sequenceOf(
            aSpaceState(),
            // Add other states here
        )
}

fun aSpaceState() = SpaceState(
    parentSpace = null,
    children = persistentListOf(),
    seenSpaceInvites = persistentSetOf(),
    hideInvitesAvatar = false,
    eventSink = {}
)
