/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.impl

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.util.Base64
import androidx.core.content.IntentCompat
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.share.api.ShareIntentData
import io.element.android.features.share.api.ShareIntentHandler
import io.element.android.features.share.api.UriToShare
import io.element.android.libraries.androidutils.compat.queryIntentActivitiesCompat
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeAny
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeApplication
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeAudio
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeFile
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeImage
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeText
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeVideo
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import timber.log.Timber

@ContributesBinding(AppScope::class)
class DefaultShareIntentHandler(
    @ApplicationContext private val context: Context,
) : ShareIntentHandler {
    override fun handleIncomingShareIntent(
        intent: Intent,
    ): ShareIntentData? {
        val type = intent.resolveType(context) ?: return null
        val uris = getIncomingUris(intent, type)
        val (directShareSessionId, directShareRoomId) = resolveDirectShareTarget(intent)
        return when {
            uris.isEmpty() && type == MimeTypes.PlainText -> handlePlainText(intent, directShareSessionId, directShareRoomId)
            type.isMimeTypeImage() ||
                type.isMimeTypeVideo() ||
                type.isMimeTypeAudio() ||
                type.isMimeTypeApplication() ||
                type.isMimeTypeFile() ||
                type.isMimeTypeText() ||
                type.isMimeTypeAny() -> {
                ShareIntentData.Uris(
                    text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()?.takeIf { it.isNotEmpty() },
                    uris = uris,
                    directShareSessionId = directShareSessionId,
                    directShareRoomId = directShareRoomId,
                )
            }
            else -> null
        }
    }

    /**
     * EXTRA_SHORTCUT_ID is the only identifier Android reliably attaches when a Direct Share
     * target is tapped from the share sheet, so it's decoded first. The custom intent extras
     * are kept as a fallback for share flows that don't go through a shortcut at all.
     */
    private fun resolveDirectShareTarget(intent: Intent): Pair<SessionId?, RoomId?> {
        val shortcutId = intent.getStringExtra(Intent.EXTRA_SHORTCUT_ID)
        val fromShortcutId = if (shortcutId?.startsWith("directshare_") == true) {
            runCatching {
                val (_, sessionEncoded, roomEncoded) = shortcutId.split("_")
                val sessionId = String(Base64.decode(sessionEncoded, Base64.NO_WRAP), Charsets.UTF_8)
                val roomId = String(Base64.decode(roomEncoded, Base64.NO_WRAP), Charsets.UTF_8)
                SessionId(sessionId) to RoomId(roomId)
            }.getOrNull()
        } else {
            null
        }
        val sessionId = fromShortcutId?.first
            ?: intent.getStringExtra(ShareIntentExtras.EXTRA_SHARE_TARGET_SESSION_ID)?.let(::SessionId)
        val roomId = fromShortcutId?.second
            ?: intent.getStringExtra(ShareIntentExtras.EXTRA_SHARE_TARGET_ROOM_ID)?.let(::RoomId)
        return sessionId to roomId
    }

    private fun handlePlainText(
        intent: Intent,
        directShareSessionId: SessionId?,
        directShareRoomId: RoomId?,
    ): ShareIntentData.PlainText? {
        val content = intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
        return if (content?.isNotEmpty() == true) {
            ShareIntentData.PlainText(content, directShareSessionId, directShareRoomId)
        } else {
            null
        }
    }

    /**
     * Use this function to retrieve files which are shared from another application or internally
     * by using android.intent.action.SEND or android.intent.action.SEND_MULTIPLE actions.
     */
    private fun getIncomingUris(intent: Intent, fallbackMimeType: String): List<UriToShare> {
        val uriList = mutableListOf<Uri>()
        if (intent.action == Intent.ACTION_SEND) {
            IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
                ?.let { uriList.add(it) }
        } else if (intent.action == Intent.ACTION_SEND_MULTIPLE) {
            IntentCompat.getParcelableArrayListExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
                ?.let { uriList.addAll(it) }
        }
        val resInfoList: List<ResolveInfo> = context.packageManager.queryIntentActivitiesCompat(intent, PackageManager.MATCH_DEFAULT_ONLY)
        uriList.forEach { uri ->
            resInfoList.forEach resolve@{ resolveInfo ->
                val packageName: String = resolveInfo.activityInfo.packageName
                // Replace implicit intent by an explicit to fix crash on some devices like Xiaomi.
                // see https://juejin.cn/post/7031736325422186510
                try {
                    context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (e: Exception) {
                    Timber.w(e, "Unable to grant Uri permission")
                    return@resolve
                }
                intent.action = null
                intent.component = ComponentName(packageName, resolveInfo.activityInfo.name)
            }
        }
        return uriList.map { uri ->
            // The value in fallbackMimeType can be wrong, especially if several uris were received
            // in the same intent (i.e. 'image/*'). We need to check the mime type of each uri.
            val mimeType = context.contentResolver.getType(uri) ?: fallbackMimeType
            UriToShare(
                uri = uri,
                mimeType = mimeType,
            )
        }
    }
}
