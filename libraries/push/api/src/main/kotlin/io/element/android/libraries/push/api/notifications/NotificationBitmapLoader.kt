/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.api.notifications

import android.graphics.Bitmap
import androidx.core.graphics.drawable.IconCompat
import coil3.ImageLoader
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.ui.media.AVATAR_THUMBNAIL_SIZE_IN_PIXEL

interface NotificationBitmapLoader {
    /**
     * Get icon of a room.
     * @param avatarData the data related to the Avatar
     * @param imageLoader Coil image loader
     * @param targetSize The size we want the bitmap to be resized to
     */
    suspend fun getRoomBitmap(
        avatarData: AvatarData,
        imageLoader: ImageLoader,
        targetSize: Long = AVATAR_THUMBNAIL_SIZE_IN_PIXEL,
    ): Bitmap?

    /**
     * Get icon of a user.
     * Before Android P, this does nothing because the icon won't be used
     * @param avatarData the data related to the Avatar
     * @param imageLoader Coil image loader
     */
    suspend fun getUserIcon(
        avatarData: AvatarData,
        imageLoader: ImageLoader,
    ): IconCompat?
}
