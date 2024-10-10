/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.androidutils.system.startInstallFromSourceIntent
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.api.local.LocalMediaActions
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class AndroidLocalMediaActions @Inject constructor(
    @ApplicationContext private val context: Context,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val buildMeta: BuildMeta,
) : LocalMediaActions {
    private var activityContext: Context? = null
    private var apkInstallLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>? = null
    private var pendingMedia: LocalMedia? = null

    @Composable
    override fun Configure() {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        apkInstallLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                pendingMedia?.let {
                    coroutineScope.launch {
                        openFile(it)
                    }
                }
            } else {
                // User cancelled
            }
            pendingMedia = null
        }
        return DisposableEffect(Unit) {
            activityContext = context
            onDispose {
                activityContext = null
            }
        }
    }

    override suspend fun saveOnDisk(localMedia: LocalMedia): Result<Unit> = withContext(coroutineDispatchers.io) {
        require(localMedia.uri.scheme == ContentResolver.SCHEME_FILE)
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveOnDiskUsingMediaStore(localMedia)
            } else {
                saveOnDiskUsingExternalStorageApi(localMedia)
            }
        }.onSuccess {
            Timber.v("Save on disk succeed")
        }.onFailure {
            Timber.e(it, "Save on disk failed")
        }
    }

    override suspend fun share(localMedia: LocalMedia): Result<Unit> = withContext(coroutineDispatchers.io) {
        require(localMedia.uri.scheme == ContentResolver.SCHEME_FILE)
        runCatching {
            val shareableUri = localMedia.toShareableUri()
            val shareMediaIntent = Intent(Intent.ACTION_SEND)
                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .putExtra(Intent.EXTRA_STREAM, shareableUri)
                .setTypeAndNormalize(localMedia.info.mimeType)
            withContext(coroutineDispatchers.main) {
                val intent = Intent.createChooser(shareMediaIntent, null)
                activityContext!!.startActivity(intent)
            }
        }.onSuccess {
            Timber.v("Share media succeed")
        }.onFailure {
            Timber.e(it, "Share media failed")
        }
    }

    override suspend fun open(localMedia: LocalMedia): Result<Unit> = withContext(coroutineDispatchers.io) {
        require(localMedia.uri.scheme == ContentResolver.SCHEME_FILE)
        runCatching {
            when (localMedia.info.mimeType) {
                MimeTypes.Apk -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (activityContext?.packageManager?.canRequestPackageInstalls() == false) {
                            pendingMedia = localMedia
                            activityContext?.startInstallFromSourceIntent(apkInstallLauncher!!).let { }
                        } else {
                            openFile(localMedia)
                        }
                    } else {
                        openFile(localMedia)
                    }
                }
                else -> openFile(localMedia)
            }
        }.onSuccess {
            Timber.v("Open media succeed")
        }.onFailure {
            Timber.e(it, "Open media failed")
        }
    }

    private suspend fun openFile(localMedia: LocalMedia) {
        val openMediaIntent = Intent(Intent.ACTION_VIEW)
            .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .setDataAndType(localMedia.toShareableUri(), localMedia.info.mimeType)
        withContext(coroutineDispatchers.main) {
            activityContext?.startActivity(openMediaIntent)
        }
    }

    private fun LocalMedia.toShareableUri(): Uri {
        val mediaAsFile = this.toFile()
        val authority = "${buildMeta.applicationId}.fileprovider"
        return FileProvider.getUriForFile(context, authority, mediaAsFile).normalizeScheme()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveOnDiskUsingMediaStore(localMedia: LocalMedia) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, localMedia.info.filename)
            put(MediaStore.MediaColumns.MIME_TYPE, localMedia.info.mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val resolver = context.contentResolver
        val outputUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (outputUri != null) {
            localMedia.openStream()?.use { input ->
                resolver.openOutputStream(outputUri).use { output ->
                    input.copyTo(output!!, DEFAULT_BUFFER_SIZE)
                }
            }
        }
    }

    private fun saveOnDiskUsingExternalStorageApi(localMedia: LocalMedia) {
        val target = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            localMedia.info.filename
        )
        localMedia.openStream()?.use { input ->
            FileOutputStream(target).use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun LocalMedia.openStream(): InputStream? {
        return context.contentResolver.openInputStream(uri)
    }

    /**
     * Tries to extract a file from the uri.
     */
    private fun LocalMedia.toFile(): File {
        return uri.toFile()
    }
}
