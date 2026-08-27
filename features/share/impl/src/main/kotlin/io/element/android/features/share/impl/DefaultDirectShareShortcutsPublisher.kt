/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.impl

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.util.Base64
import androidx.core.app.Person
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.scale
import coil3.ImageLoader
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.toBitmap
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.features.share.api.DirectShareShortcutsPublisher
import io.element.android.features.share.api.SharingRoomInfo
import io.element.android.libraries.core.extensions.ellipsize
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.di.annotations.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultDirectShareShortcutsPublisher(
    @ApplicationContext private val context: Context,
) : DirectShareShortcutsPublisher {
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
        val targetComponent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.component ?: return null

        val baseIntent = Intent(Intent.ACTION_SEND).apply {
            component = targetComponent
            putExtra(ShareIntentExtras.EXTRA_SHARE_TARGET_ROOM_ID, room.roomId.value)
            putExtra(ShareIntentExtras.EXTRA_SHARE_TARGET_SESSION_ID, room.sessionId.value)
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
            .setShortLabel(room.displayName.trim().ellipsize(12))
            .setLongLabel(context.getString(R.string.common_share_to, room.displayName))
            .setIntent(baseIntent)
            .setIcon(icon)
            .setCategories(setOf(SHARE_CATEGORY))
            .setLongLived(true)
            .setPerson(
                Person.Builder()
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
                bitmap.scale(targetSize, targetSize)
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
            Base64.encodeToString(this.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

        return "directshare_${sessionId.toBase64()}_${roomId.toBase64()}"
    }
}
