/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.libraries.push.impl.notifications

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.media.MediaResolver
import timber.log.Timber
import javax.inject.Inject

class NotificationBitmapLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Get icon of a room.
     * @param path mxc url
     */
    suspend fun getRoomBitmap(path: String?): Bitmap? {
        if (path == null) {
            return null
        }
        return loadRoomBitmap(path)
    }

    private suspend fun loadRoomBitmap(path: String): Bitmap? {
        return try {
            val imageRequest = ImageRequest.Builder(context)
                .data(MediaResolver.Meta(path, MediaResolver.Kind.Thumbnail(1024)))
                .build()
            val result = context.imageLoader.execute(imageRequest)
            result.drawable?.toBitmap()
        } catch (e: Throwable) {
            Timber.e(e, "Unable to load room bitmap")
            null
        }
    }

    /**
     * Get icon of a user.
     * Before Android P, this does nothing because the icon won't be used
     * @param path mxc url
     */
    suspend fun getUserIcon(path: String?): IconCompat? {
        if (path == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return null
        }

        return loadUserIcon(path)
    }

    private suspend fun loadUserIcon(path: String): IconCompat? {
        return try {
            val imageRequest = ImageRequest.Builder(context)
                .data(MediaResolver.Meta(path, MediaResolver.Kind.Thumbnail(1024)))
                .transformations(CircleCropTransformation())
                .build()
            val result = context.imageLoader.execute(imageRequest)
            val bitmap = result.drawable?.toBitmap()
            return bitmap?.let { IconCompat.createWithBitmap(it) }
        } catch (e: Throwable) {
            Timber.e(e, "Unable to load user bitmap")
            null
        }
    }
}
