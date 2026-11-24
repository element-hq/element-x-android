/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomshortcut.api

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import coil3.SingletonImageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import io.element.android.libraries.deeplink.api.DeepLinkCreator
import io.element.android.libraries.deeplink.impl.DefaultDeepLinkCreator
import io.element.android.libraries.designsystem.R
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrixmedia.api.MediaRequestData

private const val TAG = "RoomShortcut"

private val deepLinkCreator: DeepLinkCreator = DefaultDeepLinkCreator()
suspend fun downloadBitmap(context: Context, url: String): Bitmap? {
    val imageLoader = SingletonImageLoader.get(context)
    val requestData = MediaRequestData(
        source = MediaSource(url = url),
        kind = MediaRequestData.Kind.Content
    )

    val request = ImageRequest.Builder(context)
        .data(requestData)
        .allowHardware(false)
        .build()

    Log.i(TAG, "Image load request is builded, start executing")
    return when (val result = imageLoader.execute(request)) {
        is SuccessResult -> {
            Log.i(TAG, "Image successfully downloaded!")
            result.image.toBitmap()
        }
        is ErrorResult -> {
            Log.w(TAG, "Failed to load room image!", result.throwable)
            null
        }
    }
}

suspend fun createRoomShortcut(context: Context, sessionId: SessionId, roomId: RoomId, roomName: String, roomAvatarUrl: String?) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        Toast.makeText(context, "Shortcuts are not supported", Toast.LENGTH_SHORT).show()
        return
    }
    val packageName = context.packageName
    val shortcutManager = context.getSystemService(Context.SHORTCUT_SERVICE) as? ShortcutManager ?: return
    val bitmap = if (roomAvatarUrl != null) {
            downloadBitmap(context, roomAvatarUrl)
        } else {
            null
        }

    Log.i(TAG, "Bitmap donwloaded ${if (bitmap == null) {
        "NO"
    } else {
        "YES"
    }}")
    if (!shortcutManager.isRequestPinShortcutSupported) {
        Toast.makeText(context, "Shortcut request is not supported!", Toast.LENGTH_LONG).show()
        return
    }

    Log.i(TAG, "Request ping shortcut is supported!")

    val shortcutIntent = Intent().apply {
        setClassName(packageName, "io.element.android.x.MainActivity")
        action = Intent.ACTION_VIEW
        data = deepLinkCreator.create(sessionId, roomId, null, null).toUri()
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val iconCompat = if (bitmap != null) {
        IconCompat.createWithAdaptiveBitmap(bitmap)
    } else {
        // Fallback static resource icon if download fails
        IconCompat.createWithResource(context, R.drawable.pin)
    }

    val shortcut = ShortcutInfo.Builder(context, "shortcut_chat_$roomId")
        .setShortLabel(roomName)
        .setLongLabel("Chat with $roomName")
        .setIcon(iconCompat.toIcon(context))
        .setIntent(shortcutIntent)
        .build()

    shortcutManager.requestPinShortcut(shortcut, null)
    return
}
