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
import android.content.SharedPreferences
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
import io.element.android.features.sharing.api.SharingRoomInfo
import io.element.android.features.sharing.api.SharingShortcutsManager
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.di.annotations.ApplicationContext
import java.security.MessageDigest

@SingleIn(AppScope::class)
class DefaultSharingShortcutsManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : SharingShortcutsManager {
    private val imageLoader: ImageLoader get() = context.imageLoader

    companion object {
        const val SHARE_CATEGORY = "io.element.android.x.SHARE_TARGET"
        private const val PREF_PREFIX = "shareshortcut.room."
        private const val PREFS_NAME = "sharing_shortcuts_prefs"
    }


    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override suspend fun publishShortcutsForRooms(rooms: List<SharingRoomInfo>) {
        val shortcuts = rooms.mapNotNull { buildShortcutForRoom(it) }
        if (shortcuts.isNotEmpty()) {
            ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
            shortcuts.forEach { ShortcutManagerCompat.pushDynamicShortcut(context, it) }
        } else {
             ShortcutManagerCompat.removeAllDynamicShortcuts(context)
        }
    }

    private suspend fun buildShortcutForRoom(room: SharingRoomInfo): ShortcutInfoCompat? {
        val id = shortcutIdForRoom(room.roomId)

        val baseIntent = Intent(Intent.ACTION_SEND).apply {
            component = ComponentName(context.packageName, "io.element.android.features.sharing.impl.ShareReceiverActivity")
            type = "*/*"
            putExtra("room_id", room.roomId)
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

        // store mapping in prefs so ShareReceiverActivity can resolve roomId (no DI required there).
        prefs.edit().putString(PREF_PREFIX + id, room.roomId).apply()

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
                .setKey(room.roomId)
                .build()
            )
            .build()
    }

    private suspend fun loadAvatar(room: SharingRoomInfo, isAdaptive: Boolean): Bitmap? {
        val sizeToken = if (isAdaptive) AvatarSize.ShareShortcut else AvatarSize.CurrentUserTopBar
        val avatarData = AvatarData(
            id = room.roomId,
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
            val targetSize = (108 * density).toInt()

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

    override fun removeShortcutForRoom(roomId: String) {
        val id = shortcutIdForRoom(roomId)
        ShortcutManagerCompat.removeLongLivedShortcuts(context, listOf(id))
        prefs.edit().remove(PREF_PREFIX + id).apply()
    }

    private fun shortcutIdForRoom(roomId: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(roomId.toByteArray(Charsets.UTF_8))
        val hex = bytes.joinToString("") { "%02x".format(it) }
        return "room_" + hex.take(24)
    }

    private fun safeShortLabel(displayName: String): String {
        val trimmed = displayName.trim()
        return if (trimmed.length <= 12) trimmed else trimmed.take(12) + "…"
    }
}
