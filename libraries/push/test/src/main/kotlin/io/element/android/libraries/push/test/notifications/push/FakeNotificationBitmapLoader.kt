/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.push.test.notifications.push

import android.graphics.Bitmap
import androidx.core.graphics.drawable.IconCompat
import coil.ImageLoader
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
