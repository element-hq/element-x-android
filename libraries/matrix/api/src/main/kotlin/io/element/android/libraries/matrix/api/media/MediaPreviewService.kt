/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.media

import kotlinx.coroutines.flow.StateFlow

/**
 * Reads and writes the account settings that decide whether media previews and invite avatars are shown.
 *
 * These are account data settings shared across clients, so the local value is only updated once the server has accepted the change.
 */
interface MediaPreviewService {
    /**
     * Will fetch the media preview config from the server.
     */
    suspend fun fetchMediaPreviewConfig(): Result<MediaPreviewConfig?>

    /**
     * Will emit the media preview config known by the client.
     * This will emit a new value when received from sync.
     */
    val mediaPreviewConfigFlow: StateFlow<MediaPreviewConfig>

    /**
     * Set the media preview display policy. This will update the value on the server and update the local value when successful.
     *
     * @param mediaPreviewValue whether media previews are shown always, never, or only in private rooms.
     */
    suspend fun setMediaPreviewValue(mediaPreviewValue: MediaPreviewValue): Result<Unit>

    /**
     * Set the invite avatars display policy. This will update the value on the server and update the local value when successful.
     *
     * @param hide true to hide the avatars of users and rooms the user has been invited by.
     */
    suspend fun setHideInviteAvatars(hide: Boolean): Result<Unit>
}

fun MediaPreviewService.getMediaPreviewValue() = mediaPreviewConfigFlow.value.mediaPreviewValue
