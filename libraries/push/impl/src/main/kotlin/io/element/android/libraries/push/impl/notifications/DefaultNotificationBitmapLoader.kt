/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.core.graphics.drawable.IconCompat
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.transformations
import coil3.toBitmap
import coil3.transform.CircleCropTransformation
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.ui.media.AVATAR_THUMBNAIL_SIZE_IN_PIXEL
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import io.element.android.libraries.push.api.notifications.NotificationBitmapLoader
import io.element.android.services.toolbox.api.sdk.BuildVersionSdkIntProvider
import timber.log.Timber
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultNotificationBitmapLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sdkIntProvider: BuildVersionSdkIntProvider,
) : NotificationBitmapLoader {
    /**
     * Get icon of a room.
     * @param path mxc url
     * @param imageLoader Coil image loader
     */
    override suspend fun getRoomBitmap(path: String?, imageLoader: ImageLoader): Bitmap? {
        if (path == null) {
            return null
        }
        return loadRoomBitmap(path, imageLoader)
    }

    private suspend fun loadRoomBitmap(path: String, imageLoader: ImageLoader): Bitmap? {
        return try {
            val imageRequest = ImageRequest.Builder(context)
                .data(MediaRequestData(MediaSource(path), MediaRequestData.Kind.Thumbnail(AVATAR_THUMBNAIL_SIZE_IN_PIXEL)))
                .transformations(CircleCropTransformation())
                .build()
            val result = imageLoader.execute(imageRequest)
            result.image?.toBitmap()
        } catch (e: Throwable) {
            Timber.e(e, "Unable to load room bitmap")
            null
        }
    }

    /**
     * Get icon of a user.
     * Before Android P, this does nothing because the icon won't be used
     * @param path mxc url
     * @param imageLoader Coil image loader
     */
    override suspend fun getUserIcon(path: String?, imageLoader: ImageLoader): IconCompat? {
        if (path == null || sdkIntProvider.get() < Build.VERSION_CODES.P) {
            return null
        }

        return loadUserIcon(path, imageLoader)
    }

    private suspend fun loadUserIcon(path: String, imageLoader: ImageLoader): IconCompat? {
        return try {
            val imageRequest = ImageRequest.Builder(context)
                .data(MediaRequestData(MediaSource(path), MediaRequestData.Kind.Thumbnail(AVATAR_THUMBNAIL_SIZE_IN_PIXEL)))
                .transformations(CircleCropTransformation())
                .build()
            val result = imageLoader.execute(imageRequest)
            val bitmap = result.image?.toBitmap()
            return bitmap?.let { IconCompat.createWithBitmap(it) }
        } catch (e: Throwable) {
            Timber.e(e, "Unable to load user bitmap")
            null
        }
    }
}
