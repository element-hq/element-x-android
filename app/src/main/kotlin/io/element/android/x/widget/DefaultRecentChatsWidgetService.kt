/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import dev.zacsweers.metro.ContributesBinding
import io.element.android.appnav.widget.RecentChatsWidgetService
import io.element.android.appnav.widget.WidgetRoomData
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.media.MediaSource
import timber.log.Timber
import java.io.File

@ContributesBinding(SessionScope::class)
class DefaultRecentChatsWidgetService(
    @ApplicationContext private val context: Context,
    private val matrixClient: MatrixClient,
) : RecentChatsWidgetService {
    override suspend fun updateRecentChats(rooms: List<WidgetRoomData>) {
        Timber.d("Widget: updateRecentChats called with ${rooms.size} rooms")
        val avatarDir = File(context.cacheDir, "widget_avatars").also { it.mkdirs() }

        val resolvedRooms = rooms.map { room ->
            val localAvatarPath = room.avatarUrl?.let { url ->
                try {
                    downloadAvatar(url, room.roomId, avatarDir)
                } catch (e: Exception) {
                    Timber.w(e, "Widget: Failed to download avatar for ${room.name}")
                    null
                }
            }
            room.copy(avatarUrl = localAvatarPath)
        }
        RecentChatsWidgetUpdater.updateWidget(context, resolvedRooms)
    }

    private suspend fun downloadAvatar(mxcUrl: String, roomId: String, avatarDir: File): String? {
        val fileName = "${roomId.hashCode().toUInt()}.png"
        val file = File(avatarDir, fileName)
        if (file.exists() && System.currentTimeMillis() - file.lastModified() < 3_600_000) {
            return file.absolutePath
        }
        val bytes = matrixClient.matrixMediaLoader.loadMediaThumbnail(
            source = MediaSource(mxcUrl),
            width = 96L,
            height = 96L,
        ).getOrNull() ?: return null
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return file.absolutePath
    }
}
