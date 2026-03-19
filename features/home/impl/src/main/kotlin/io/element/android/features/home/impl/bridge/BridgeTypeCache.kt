/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.bridge

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory cache for bridge type enrichment results.
 * Populated lazily by [BridgeEnrichmentService] via room member inspection.
 */
@Inject
@SingleIn(SessionScope::class)
class BridgeTypeCache {
    private val cacheState = MutableStateFlow<Map<RoomId, BridgeType>>(emptyMap())
    val cacheFlow: StateFlow<Map<RoomId, BridgeType>> = cacheState.asStateFlow()

    fun get(roomId: RoomId): BridgeType? = cacheState.value[roomId]

    fun put(roomId: RoomId, bridgeType: BridgeType) {
        cacheState.value = cacheState.value + (roomId to bridgeType)
    }

    fun markChecked(roomId: RoomId) {
        if (!cacheState.value.containsKey(roomId)) {
            cacheState.value = cacheState.value + (roomId to BridgeType.NONE)
        }
    }

    fun contains(roomId: RoomId): Boolean = cacheState.value.containsKey(roomId)
}
