/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.root

import io.element.android.libraries.matrix.api.spaces.SpaceRoom

sealed interface SpaceEvents {
    data object LoadMore : SpaceEvents
    data class Join(val spaceRoom: SpaceRoom) : SpaceEvents
    data object ClearFailures : SpaceEvents
    data class AcceptInvite(val spaceRoom: SpaceRoom) : SpaceEvents
    data class DeclineInvite(val spaceRoom: SpaceRoom) : SpaceEvents

    data class ShowTopicViewer(val topic: String) : SpaceEvents
    data object HideTopicViewer : SpaceEvents
}
