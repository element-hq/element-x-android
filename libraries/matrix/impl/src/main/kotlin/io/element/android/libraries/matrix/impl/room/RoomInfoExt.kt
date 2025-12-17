/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.matrix.api.user.MatrixUser
import org.matrix.rustcomponents.sdk.RoomInfo

/**
 * Extract the heroes from the room info.
 * For now we only use heroes for direct rooms with 2 members.
 * Also we keep the heroes only if there is one single hero.
 */
fun RoomInfo.elementHeroes(): List<MatrixUser> {
    return heroes
        .takeIf { isDirect && activeMembersCount.toLong() == 2L }
        ?.takeIf { it.size == 1 }
        ?.map { it.map() }
        .orEmpty()
}
