/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.test.notifications.push

import android.graphics.Bitmap
import androidx.core.graphics.drawable.IconCompat
import coil3.ImageLoader
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.push.api.notifications.NotificationBitmapLoader

class FakeNotificationBitmapLoader(
    var getRoomBitmapResult: (AvatarData, ImageLoader, Long) -> Bitmap? = { _, _, _ -> null },
    var getUserIconResult: (AvatarData, ImageLoader) -> IconCompat? = { _, _ -> null },
) : NotificationBitmapLoader {
    override suspend fun getRoomBitmap(avatarData: AvatarData, imageLoader: ImageLoader, targetSize: Long): Bitmap? {
        return getRoomBitmapResult(avatarData, imageLoader, targetSize)
    }

    override suspend fun getUserIcon(avatarData: AvatarData, imageLoader: ImageLoader): IconCompat? {
        return getUserIconResult(avatarData, imageLoader)
    }
}
