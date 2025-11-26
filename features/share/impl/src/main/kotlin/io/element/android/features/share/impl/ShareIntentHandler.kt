/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
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
import android.os.Build
import androidx.core.content.IntentCompat
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
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
import timber.log.Timber

interface ShareIntentHandler {
    data class UriToShare(
        val uri: Uri,
        val mimeType: String,
    )

    /**
     * This methods aims to handle incoming share intents.
     *
     * @return true if it can handle the intent data, false otherwise
     */
    suspend fun handleIncomingShareIntent(
        intent: Intent,
        onUris: suspend (List<UriToShare>) -> Boolean,
        onPlainText: suspend (String) -> Boolean,
    ): Boolean
}

@ContributesBinding(AppScope::class)
class DefaultShareIntentHandler(
    @ApplicationContext private val context: Context,
) : ShareIntentHandler {
    override suspend fun handleIncomingShareIntent(
        intent: Intent,
        onUris: suspend (List<ShareIntentHandler.UriToShare>) -> Boolean,
        onPlainText: suspend (String) -> Boolean,
    ): Boolean {
        val type = intent.resolveType(context) ?: return false
        val uris = getIncomingUris(intent, type)
        return when {
            uris.isEmpty() && type == MimeTypes.PlainText -> handlePlainText(intent, onPlainText)
            type.isMimeTypeImage() ||
                type.isMimeTypeVideo() ||
                type.isMimeTypeAudio() ||
                type.isMimeTypeApplication() ||
                type.isMimeTypeFile() ||
                type.isMimeTypeText() ||
                type.isMimeTypeAny() -> {
                val result = onUris(uris)
                revokeUriPermissions(uris.map { it.uri })
                result
            }
            else -> false
        }
    }

    private suspend fun handlePlainText(intent: Intent, onPlainText: suspend (String) -> Boolean): Boolean {
        val content = intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
        return if (content?.isNotEmpty() == true) {
            onPlainText(content)
        } else {
            false
        }
    }

    /**
     * Use this function to retrieve files which are shared from another application or internally
     * by using android.intent.action.SEND or android.intent.action.SEND_MULTIPLE actions.
     */
    private fun getIncomingUris(intent: Intent, fallbackMimeType: String): List<ShareIntentHandler.UriToShare> {
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
            ShareIntentHandler.UriToShare(
                uri = uri,
                mimeType = mimeType,
            )
        }
    }

    private fun revokeUriPermissions(uris: List<Uri>) {
        uris.forEach { uri ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.revokeUriPermission(context.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    context.revokeUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } catch (e: Exception) {
                Timber.w(e, "Unable to revoke Uri permission")
            }
        }
    }
}
