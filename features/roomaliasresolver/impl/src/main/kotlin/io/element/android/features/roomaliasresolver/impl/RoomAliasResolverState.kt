/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomaliasresolver.impl

import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.room.alias.ResolvedRoomAlias

data class RoomAliasResolverState(
    val roomAlias: RoomAlias,
    val resolveState: AsyncData<ResolvedRoomAlias>,
    val eventSink: (RoomAliasResolverEvents) -> Unit
)

sealed class RoomAliasResolverFailures : Exception() {
    data object UnknownAlias : RoomAliasResolverFailures()
}
