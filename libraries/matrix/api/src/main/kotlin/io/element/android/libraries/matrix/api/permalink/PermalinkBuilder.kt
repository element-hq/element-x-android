/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.permalink

import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.UserId

/**
 * Builds `matrix.to` permalinks, which are the shareable links to a user or a room; see [PermalinkParser] for the other direction.
 */
interface PermalinkBuilder {
    /**
     * Builds a permalink to a user, failing with [PermalinkBuilderError.InvalidData] when the id is malformed.
     *
     * @param userId the user to link to.
     */
    fun permalinkForUser(userId: UserId): Result<String>

    /**
     * Builds a permalink to a room from its alias, failing with [PermalinkBuilderError.InvalidData] when the alias is malformed.
     *
     * @param roomAlias the room alias to link to.
     */
    fun permalinkForRoomAlias(roomAlias: RoomAlias): Result<String>
}

sealed class PermalinkBuilderError : Throwable() {
    data object InvalidData : PermalinkBuilderError()
}
