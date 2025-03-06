/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.api.notifications

import android.graphics.Bitmap
import androidx.core.graphics.drawable.IconCompat
import coil3.ImageLoader

interface NotificationBitmapLoader {
    /**
     * Get icon of a room.
     * @param path mxc url
     * @param imageLoader Coil image loader
     */
    suspend fun getRoomBitmap(path: String?, imageLoader: ImageLoader): Bitmap?

    /**
     * Get icon of a user.
     * Before Android P, this does nothing because the icon won't be used
     * @param path mxc url
     * @param imageLoader Coil image loader
     */
    suspend fun getUserIcon(path: String?, imageLoader: ImageLoader): IconCompat?
}
