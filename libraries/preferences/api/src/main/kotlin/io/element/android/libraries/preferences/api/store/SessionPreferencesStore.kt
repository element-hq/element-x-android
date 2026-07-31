/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.api.store

import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.flow.Flow

interface SessionPreferencesStore {
    suspend fun setSharePresence(enabled: Boolean)
    fun isSharePresenceEnabled(): Flow<Boolean>

    suspend fun setSendPublicReadReceipts(enabled: Boolean)
    fun isSendPublicReadReceiptsEnabled(): Flow<Boolean>

    suspend fun setRenderReadReceipts(enabled: Boolean)
    fun isRenderReadReceiptsEnabled(): Flow<Boolean>

    suspend fun setSendTypingNotifications(enabled: Boolean)
    fun isSendTypingNotificationsEnabled(): Flow<Boolean>

    suspend fun setRenderTypingNotifications(enabled: Boolean)
    fun isRenderTypingNotificationsEnabled(): Flow<Boolean>

    suspend fun setSkipSessionVerification(skip: Boolean)
    fun isSessionVerificationSkipped(): Flow<Boolean>

    suspend fun setOptimizeImages(compress: Boolean)
    fun doesOptimizeImages(): Flow<Boolean>

    suspend fun setVideoCompressionPreset(preset: VideoCompressionPreset)
    fun getVideoCompressionPreset(): Flow<VideoCompressionPreset>

    /**
     * Records "now" as the last time [roomId]'s auto-created per-room notification channel
     * handled a notification. Used to retire long-inactive channels.
     */
    suspend fun recordRoomChannelNotified(roomId: RoomId)

    /** Clears [roomId]'s recorded channel last-notified timestamp, if any. */
    suspend fun clearRoomChannelLastNotified(roomId: RoomId)

    /**
     * Every recorded channel last-notified timestamp, keyed by the same room-id hash embedded in
     * that room's channel id. Channel ids only carry a one-way hash of the room id, not the id
     * itself, so a caller enumerating system channels can only look this up by that hash.
     */
    suspend fun getRoomChannelLastNotifiedByHash(): Map<String, Long>

    /** Clears a channel last-notified timestamp by the room-id hash embedded in its channel id. */
    suspend fun clearRoomChannelLastNotifiedByHash(roomHash: String)

    suspend fun clear()
}
