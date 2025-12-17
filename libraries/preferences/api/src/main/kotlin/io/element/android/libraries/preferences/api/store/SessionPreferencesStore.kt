/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.api.store

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

    suspend fun clear()
}
