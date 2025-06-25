/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.media

import kotlinx.coroutines.flow.Flow

interface MediaPreviewService {
    /**
     * Will fetch the media preview config from the server.
     */
    suspend fun fetchMediaPreviewConfig(): Result<MediaPreviewConfig?>

    /**
     * Will emit the media preview config known by the client.
     * This will emit a new value when received from sync.
     */
    fun getMediaPreviewConfigFlow(): Flow<MediaPreviewConfig?>

    /**
     * Get the media preview display policy from the cache. This value is updated through sync.
     */
    suspend fun getMediaPreviewValue(): MediaPreviewValue?

    /**
     * Get the invite avatars display policy from the cache. This value is updated through sync.
     */
    suspend fun getHideInviteAvatars(): Boolean

    /**
     * Set the media preview display policy. This will update the value on the server and update the local value when successful.
     */
    suspend fun setMediaPreviewValue(mediaPreviewValue: MediaPreviewValue): Result<Unit>
    /**
     * Set the invite avatars display policy. This will update the value on the server and update the local value when successful.
     */
    suspend fun setHideInviteAvatars(hide: Boolean): Result<Unit>
}
