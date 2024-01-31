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

package io.element.android.features.viewfolder.impl.file

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

interface FileShare {
    suspend fun share(
        path: String
    )
}

@ContributesBinding(AppScope::class)
class DefaultFileShare @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: CoroutineDispatchers,
    private val buildMeta: BuildMeta,
) : FileShare {
    override suspend fun share(
        path: String,
    ) {
        runCatching {
            val file = File(path)
            val shareableUri = file.toShareableUri()
            val shareMediaIntent = Intent(Intent.ACTION_SEND)
                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .putExtra(Intent.EXTRA_STREAM, shareableUri)
                .setTypeAndNormalize(MimeTypes.OctetStream)
            withContext(dispatchers.main) {
                val intent = Intent.createChooser(shareMediaIntent, null)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }.onSuccess {
            Timber.v("Share file succeed")
        }.onFailure {
            Timber.e(it, "Share file failed")
        }
    }

    private fun File.toShareableUri(): Uri {
        val authority = "${buildMeta.applicationId}.fileprovider"
        return FileProvider.getUriForFile(context, authority, this).normalizeScheme()
    }
}
