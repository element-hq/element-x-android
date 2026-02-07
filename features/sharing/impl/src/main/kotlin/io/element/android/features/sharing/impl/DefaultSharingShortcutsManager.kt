/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.sharing.impl

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import coil3.ImageLoader
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.toBitmap
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.Inject
import io.element.android.features.sharing.api.SharingConstants
import io.element.android.features.sharing.api.SharingRoomInfo
import io.element.android.features.sharing.api.SharingShortcutsManager
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.di.annotations.ApplicationContext
import android.util.Base64
import java.nio.charset.Charset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@SingleIn(AppScope::class)
class DefaultSharingShortcutsManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : SharingShortcutsManager {
    private val imageLoader: ImageLoader get() = context.imageLoader

    companion object {
        const val SHARE_CATEGORY = "io.element.android.x.SHARE_TARGET"
    }

    override suspend fun publishShortcutsForRooms(rooms: List<SharingRoomInfo>) {
        withContext(Dispatchers.IO) {
            val shortcuts = rooms.mapNotNull { buildShortcutForRoom(it) }
            ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
        }
    }

    private suspend fun buildShortcutForRoom(room: SharingRoomInfo): ShortcutInfoCompat? {
        val id = shortcutIdForRoom(room.sessionId.value, room.roomId.value)

        val baseIntent = Intent(context, ShareReceiverActivity::class.java).apply {
            action = Intent.ACTION_SEND
            putExtra(SharingConstants.EXTRA_SHARE_TARGET_ROOM_ID, room.roomId.value)
            putExtra(SharingConstants.EXTRA_SHARE_TARGET_SESSION_ID, room.sessionId.value)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val isAdaptive = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        val icon = room.avatarUrl?.let { loadAvatar(room, isAdaptive) }?.let {
            if (isAdaptive) {
                IconCompat.createWithAdaptiveBitmap(it)
            } else {
                IconCompat.createWithBitmap(it)
            }
        } ?: IconCompat.createWithResource(context, android.R.drawable.sym_def_app_icon)

        return ShortcutInfoCompat.Builder(context, id)
            .setShortLabel(safeShortLabel(room.displayName))
            .setLongLabel("Share to ${room.displayName}")
            .setIntent(baseIntent)
            .setIcon(icon)
            .setCategories(setOf(SHARE_CATEGORY))
            .setLongLived(true)
            .setPerson(
                androidx.core.app.Person.Builder()
                .setName(room.displayName)
                .setKey(createCompositeKey(room.sessionId.value, room.roomId.value))
                .build()
            )
            .build()
    }

    private suspend fun loadAvatar(room: SharingRoomInfo, isAdaptive: Boolean): Bitmap? {
        val sizeToken = if (isAdaptive) AvatarSize.ShareShortcut else AvatarSize.CurrentUserTopBar
        val avatarData = AvatarData(
            id = room.roomId.value,
            name = room.displayName,
            url = room.avatarUrl,
            size = sizeToken,
        )
        val request = ImageRequest.Builder(context)
            .data(avatarData)
            .build()
        val result = imageLoader.execute(request)
        val bitmap = result.image?.toBitmap() ?: return null

        return if (isAdaptive) {
            val density = context.resources.displayMetrics.density
            val targetSize = (AvatarSize.ShareShortcut.dp.value * density).toInt()

            // Scale the bitmap to the target size if needed
            if (bitmap.width != targetSize || bitmap.height != targetSize) {
                Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true)
            } else {
                bitmap
            }
        } else {
            bitmap
        }
    }

    private fun createCompositeKey(sessionId: String, roomId: String): String {
        return "$sessionId/$roomId"
    }

    private fun shortcutIdForRoom(sessionId: String, roomId: String): String {
        fun String.toBase64(): String =
            Base64.encodeToString(this.toByteArray(Charset.forName("UTF-8")), Base64.NO_WRAP)

        return "directshare_${sessionId.toBase64()}_${roomId.toBase64()}"
    }

    private fun safeShortLabel(displayName: String): String {
        val trimmed = displayName.trim()
        return if (trimmed.length <= 12) trimmed else trimmed.take(12) + "…"
    }
}
