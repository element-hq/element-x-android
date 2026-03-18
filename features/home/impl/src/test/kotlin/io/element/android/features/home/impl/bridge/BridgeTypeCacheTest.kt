/*
 * Copyright (c) 2025 Ravel.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.bridge

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.RoomId
import org.junit.Test

class BridgeTypeCacheTest {

    private val roomId = RoomId("!room1:matrix.org")
    private val roomId2 = RoomId("!room2:matrix.org")

    @Test
    fun `returns null for unknown room`() {
        val cache = BridgeTypeCache()
        assertThat(cache.get(roomId)).isNull()
    }

    @Test
    fun `stores and retrieves bridge type`() {
        val cache = BridgeTypeCache()
        cache.put(roomId, BridgeType.DISCORD)
        assertThat(cache.get(roomId)).isEqualTo(BridgeType.DISCORD)
    }

    @Test
    fun `contains returns false for unknown room`() {
        val cache = BridgeTypeCache()
        assertThat(cache.contains(roomId)).isFalse()
    }

    @Test
    fun `contains returns true after put`() {
        val cache = BridgeTypeCache()
        cache.put(roomId, BridgeType.SIGNAL)
        assertThat(cache.contains(roomId)).isTrue()
    }

    @Test
    fun `markChecked stores NONE sentinel`() {
        val cache = BridgeTypeCache()
        cache.markChecked(roomId)
        assertThat(cache.contains(roomId)).isTrue()
        assertThat(cache.get(roomId)).isEqualTo(BridgeType.NONE)
    }

    @Test
    fun `markChecked does not overwrite existing entry`() {
        val cache = BridgeTypeCache()
        cache.put(roomId, BridgeType.DISCORD)
        cache.markChecked(roomId) // should not overwrite
        assertThat(cache.get(roomId)).isEqualTo(BridgeType.DISCORD)
    }

    @Test
    fun `stores multiple rooms independently`() {
        val cache = BridgeTypeCache()
        cache.put(roomId, BridgeType.DISCORD)
        cache.put(roomId2, BridgeType.SIGNAL)
        assertThat(cache.get(roomId)).isEqualTo(BridgeType.DISCORD)
        assertThat(cache.get(roomId2)).isEqualTo(BridgeType.SIGNAL)
    }

    @Test
    fun `cacheFlow emits updated map after put`() {
        val cache = BridgeTypeCache()
        assertThat(cache.cacheFlow.value).isEmpty()
        cache.put(roomId, BridgeType.WHATSAPP)
        assertThat(cache.cacheFlow.value).containsEntry(roomId, BridgeType.WHATSAPP)
    }
}
