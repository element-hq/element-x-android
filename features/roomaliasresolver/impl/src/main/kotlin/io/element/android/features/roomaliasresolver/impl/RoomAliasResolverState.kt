/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomaliasresolver.impl

import androidx.compose.runtime.Immutable
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.room.alias.ResolvedRoomAlias

@Immutable
data class RoomAliasResolverState(
    val roomAlias: RoomAlias,
    val resolveState: AsyncData<ResolvedRoomAlias>,
    val eventSink: (RoomAliasResolverEvents) -> Unit
)
