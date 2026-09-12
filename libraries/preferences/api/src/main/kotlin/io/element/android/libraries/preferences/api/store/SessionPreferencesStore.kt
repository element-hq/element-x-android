/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.api.store

import kotlinx.coroutines.flow.Flow

/**
 * Local, per-session user preferences, stored on the device only and never synced to the homeserver.
 *
 * Each getter returns a flow that emits the current value straight away and then again on every change.
 * The default each preference falls back to when it has never been set is given on the getter.
 */
interface SessionPreferencesStore {
    /**
     * @param enabled true to let the homeserver share this user's presence with others.
     */
    suspend fun setSharePresence(enabled: Boolean)

    /** Whether presence is shared; defaults to `true`. */
    fun isSharePresenceEnabled(): Flow<Boolean>

    /**
     * @param enabled true to send read receipts that other members can see.
     */
    suspend fun setSendPublicReadReceipts(enabled: Boolean)

    /** Whether read receipts are sent publicly rather than privately; defaults to `true`. */
    fun isSendPublicReadReceiptsEnabled(): Flow<Boolean>

    /**
     * @param enabled true to display the read receipts of other members in the timeline.
     */
    suspend fun setRenderReadReceipts(enabled: Boolean)

    /** Whether other members' read receipts are displayed; defaults to `true`. */
    fun isRenderReadReceiptsEnabled(): Flow<Boolean>

    /**
     * @param enabled true to tell other members when this user is typing.
     */
    suspend fun setSendTypingNotifications(enabled: Boolean)

    /** Whether typing notifications are sent; defaults to `true`. */
    fun isSendTypingNotificationsEnabled(): Flow<Boolean>

    /**
     * @param enabled true to display which other members are typing.
     */
    suspend fun setRenderTypingNotifications(enabled: Boolean)

    /** Whether other members' typing notifications are displayed; defaults to `true`. */
    fun isRenderTypingNotificationsEnabled(): Flow<Boolean>

    /**
     * @param enabled true to display messages that have been removed in the timeline.
     */
    suspend fun setRenderRedactedMessages(enabled: Boolean)

    /** Whether removed messages are displayed in the timeline; defaults to `true`. */
    fun isRenderRedactedMessagesEnabled(): Flow<Boolean>

    /**
     * @param skip true to remember that the user dismissed the session verification prompt.
     */
    suspend fun setSkipSessionVerification(skip: Boolean)

    /** Whether the user chose to skip verifying this session, so they are not asked again; defaults to `false`. */
    fun isSessionVerificationSkipped(): Flow<Boolean>

    /**
     * @param compress true to downscale images before uploading them.
     */
    suspend fun setOptimizeImages(compress: Boolean)

    /** Whether images are downscaled before upload; defaults to `true`. */
    fun doesOptimizeImages(): Flow<Boolean>

    /**
     * @param preset the quality and size trade-off to apply when compressing videos.
     */
    suspend fun setVideoCompressionPreset(preset: VideoCompressionPreset)

    /** The video compression preset; defaults to [VideoCompressionPreset.STANDARD], including when the stored value is unreadable. */
    fun getVideoCompressionPreset(): Flow<VideoCompressionPreset>

    /** Erases every preference of this session, so they all fall back to their defaults. */
    suspend fun clear()
}
