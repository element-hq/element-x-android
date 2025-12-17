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

interface PermalinkBuilder {
    fun permalinkForUser(userId: UserId): Result<String>
    fun permalinkForRoomAlias(roomAlias: RoomAlias): Result<String>
}

sealed class PermalinkBuilderError : Throwable() {
    data object InvalidData : PermalinkBuilderError()
}
