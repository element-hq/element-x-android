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
import dev.zacsweers.metro.Inject
import io.element.android.appnav.widget.RecentChatsWidgetService
import io.element.android.appnav.widget.WidgetRoomData
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.media.MediaSource
import timber.log.Timber
import java.io.File

@ContributesBinding(SessionScope::class)
@Inject
class DefaultRecentChatsWidgetService(
    @ApplicationContext private val context: Context,
    private val matrixClient: MatrixClient,
) : RecentChatsWidgetService {
    override suspend fun updateRecentChats(rooms: List<WidgetRoomData>) {
        Timber.d("Widget: updateRecentChats called with ${rooms.size} rooms")
        val avatarDir = File(context.cacheDir, "widget_avatars").also { it.mkdirs() }

        val widgetInfoList = rooms.map { room ->
            val localAvatarPath = room.avatarUrl?.let { url ->
                Timber.d("Widget: Downloading avatar for ${room.name}, url=$url")
                try {
                    val path = downloadAvatar(url, room.roomId, avatarDir)
                    Timber.d("Widget: Avatar saved to $path")
                    path
                } catch (e: Exception) {
                    Timber.w(e, "Widget: Failed to download avatar for ${room.name}")
                    null
                }
            }

            RoomWidgetInfo(
                sessionId = room.sessionId,
                roomId = room.roomId,
                name = room.name,
                lastMessage = room.lastMessage,
                lastActivityTimestamp = room.lastActivityTimestamp,
                unreadCount = room.unreadCount,
                senderName = room.senderName,
                avatarUrl = localAvatarPath,
                isFavorite = room.isFavorite,
            )
        }
        RecentChatsWidgetUpdater.updateWidget(context, widgetInfoList)
    }

    private suspend fun downloadAvatar(
        mxcUrl: String,
        roomId: String,
        avatarDir: File,
    ): String? {
        val fileName = "${roomId.hashCode().toUInt()}.png"
        val file = File(avatarDir, fileName)
        // Use cached file if it exists and is recent (less than 1 hour old)
        if (file.exists() && System.currentTimeMillis() - file.lastModified() < 3_600_000) {
            return file.absolutePath
        }
        val source = MediaSource(mxcUrl)
        val result = matrixClient.matrixMediaLoader.loadMediaThumbnail(
            source = source,
            width = 96L,
            height = 96L,
        )
        val bytes = result.getOrNull() ?: return null
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file.absolutePath
    }
}
