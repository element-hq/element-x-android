/*
 * Copyright (c) 2025 Ravel.
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
    private val _cache = MutableStateFlow<Map<RoomId, BridgeType>>(emptyMap())
    val cacheFlow: StateFlow<Map<RoomId, BridgeType>> = _cache.asStateFlow()

    fun get(roomId: RoomId): BridgeType? = _cache.value[roomId]

    fun put(roomId: RoomId, bridgeType: BridgeType) {
        _cache.value = _cache.value + (roomId to bridgeType)
    }

    fun markChecked(roomId: RoomId) {
        if (!_cache.value.containsKey(roomId)) {
            _cache.value = _cache.value + (roomId to BridgeType.NONE)
        }
    }

    fun contains(roomId: RoomId): Boolean = _cache.value.containsKey(roomId)
}
