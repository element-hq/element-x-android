/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.join

import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias

interface JoinRoom {
    suspend operator fun invoke(
        roomIdOrAlias: RoomIdOrAlias,
        serverNames: List<String>,
        trigger: JoinedRoom.Trigger,
    ): Result<Unit>

    sealed class Failures : Exception() {
        data object UnauthorizedJoin : Failures()
    }
}
