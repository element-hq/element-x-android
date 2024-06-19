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

package io.element.android.libraries.push.api.notifications

import android.graphics.Bitmap
import androidx.core.graphics.drawable.IconCompat
import coil.ImageLoader

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
