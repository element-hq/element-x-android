/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.viewfolder.impl.file

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.di.annotations.ApplicationContext
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

interface FileShare {
    suspend fun share(
        path: String
    )
}

@ContributesBinding(AppScope::class)
class DefaultFileShare(
    @ApplicationContext private val context: Context,
    private val dispatchers: CoroutineDispatchers,
    private val buildMeta: BuildMeta,
) : FileShare {
    override suspend fun share(
        path: String,
    ) {
        runCatchingExceptions {
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
