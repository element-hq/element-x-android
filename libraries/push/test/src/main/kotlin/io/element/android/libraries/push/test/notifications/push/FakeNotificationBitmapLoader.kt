/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.test.notifications.push

import android.graphics.Bitmap
import androidx.core.graphics.drawable.IconCompat
import coil3.ImageLoader
import io.element.android.libraries.push.api.notifications.NotificationBitmapLoader

class FakeNotificationBitmapLoader(
    var getRoomBitmapResult: (String?, ImageLoader) -> Bitmap? = { _, _ -> null },
    var getUserIconResult: (String?, ImageLoader) -> IconCompat? = { _, _ -> null },
) : NotificationBitmapLoader {
    override suspend fun getRoomBitmap(path: String?, imageLoader: ImageLoader): Bitmap? {
        return getRoomBitmapResult(path, imageLoader)
    }

    override suspend fun getUserIcon(path: String?, imageLoader: ImageLoader): IconCompat? {
        return getUserIconResult(path, imageLoader)
    }
}
