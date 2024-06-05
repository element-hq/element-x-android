/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.share.impl

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.androidutils.compat.getParcelableArrayListExtraCompat
import io.element.android.libraries.androidutils.compat.getParcelableExtraCompat
import io.element.android.libraries.androidutils.compat.queryIntentActivitiesCompat
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeAny
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeApplication
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeAudio
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeFile
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeImage
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeText
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeVideo
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import javax.inject.Inject

interface ShareIntentHandler {
    suspend fun handleIncomingShareIntent(
        intent: Intent,
        onFile: suspend (List<DefaultShareIntentHandler.FileToShare>) -> Boolean,
        onPlainText: suspend (String) -> Boolean,
    ): Boolean
}

@ContributesBinding(AppScope::class)
class DefaultShareIntentHandler @Inject constructor(
    @ApplicationContext private val context: Context,
) : ShareIntentHandler {
    data class FileToShare(
        val uri: Uri,
        val mimeType: String,
    )

    /**
     * This methods aims to handle incoming share intents.
     *
     * @return true if it can handle the intent data, false otherwise
     */
    override suspend fun handleIncomingShareIntent(
        intent: Intent,
        onFile: suspend (List<FileToShare>) -> Boolean,
        onPlainText: suspend (String) -> Boolean,
    ): Boolean {
        val type = intent.resolveType(context) ?: return false
        return when {
            type == MimeTypes.PlainText -> handlePlainText(intent, onPlainText)
            type.isMimeTypeImage() ||
                type.isMimeTypeVideo() ||
                type.isMimeTypeAudio() ||
                type.isMimeTypeApplication() ||
                type.isMimeTypeFile() ||
                type.isMimeTypeText() ||
                type.isMimeTypeAny() -> onFile(getIncomingFiles(intent, type))
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
    private fun getIncomingFiles(data: Intent, type: String): List<FileToShare> {
        val uriList = mutableListOf<Uri>()
        if (data.action == Intent.ACTION_SEND) {
            data.getParcelableExtraCompat<Uri>(Intent.EXTRA_STREAM)?.let { uriList.add(it) }
        } else if (data.action == Intent.ACTION_SEND_MULTIPLE) {
            val extraUriList: List<Uri>? = data.getParcelableArrayListExtraCompat(Intent.EXTRA_STREAM)
            extraUriList?.let { uriList.addAll(it) }
        }
        val resInfoList: List<ResolveInfo> = context.packageManager.queryIntentActivitiesCompat(data, PackageManager.MATCH_DEFAULT_ONLY)
        uriList.forEach {
            for (resolveInfo in resInfoList) {
                val packageName: String = resolveInfo.activityInfo.packageName
                // Replace implicit intent by an explicit to fix crash on some devices like Xiaomi.
                // see https://juejin.cn/post/7031736325422186510
                try {
                    context.grantUriPermission(packageName, it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (e: Exception) {
                    continue
                }
                data.action = null
                data.component = ComponentName(packageName, resolveInfo.activityInfo.name)
                break
            }
        }
        return uriList.map { uri ->
            FileToShare(
                uri = uri,
                mimeType = type
            )
        }
    }
}
