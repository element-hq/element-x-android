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

/**
 * Joins a room and reports the join to analytics, so that callers do not have to do both.
 */
interface JoinRoom {
    /**
     * Joins the room, picking the right SDK call depending on whether an id or an alias was given, and captures an analytics event on success.
     * A `Forbidden` answer from the server is translated into [Failures.UnauthorizedJoin] so callers can tell it apart from other errors.
     *
     * @param roomIdOrAlias the id or the alias of the room to join.
     * @param serverNames servers to ask about the room, ignored when joining by alias.
     * @param trigger what made the user join, recorded in the analytics event.
     */
    suspend operator fun invoke(
        roomIdOrAlias: RoomIdOrAlias,
        serverNames: List<String>,
        trigger: JoinedRoom.Trigger,
    ): Result<Unit>

    sealed class Failures : Exception() {
        data object UnauthorizedJoin : Failures()
    }
}
