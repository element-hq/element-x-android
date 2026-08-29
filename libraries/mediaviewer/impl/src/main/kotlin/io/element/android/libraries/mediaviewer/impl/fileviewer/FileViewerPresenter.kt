/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.fileviewer

import android.content.ActivityNotFoundException
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.net.toUri
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.libraries.androidutils.filesize.FileSizeFormatter
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.designsystem.utils.snackbar.collectSnackbarMessageAsState
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.mediaviewer.api.FileViewerEntryPoint
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.api.util.FileExtensionExtractor
import io.element.android.libraries.mediaviewer.impl.local.LocalMediaActions
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import io.element.android.libraries.androidutils.R as UtilsR

@AssistedInject
class FileViewerPresenter(
    @Assisted private val params: FileViewerEntryPoint.Params,
    @ApplicationContext private val context: Context,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val fileSizeFormatter: FileSizeFormatter,
    private val fileExtensionExtractor: FileExtensionExtractor,
    private val localMediaActions: LocalMediaActions,
) : Presenter<FileViewerState> {
    @AssistedFactory
    fun interface Factory {
        fun create(params: FileViewerEntryPoint.Params): FileViewerPresenter
    }

    // Use a local snackbarDispatcher, because this screen can be rendered from any Node.
    private val snackbarDispatcher = SnackbarDispatcher()

    @Composable
    override fun present(): FileViewerState {
        val coroutineScope = rememberCoroutineScope()
        val localMedia = remember { mutableStateOf<AsyncData<LocalMedia>>(AsyncData.Uninitialized) }
        val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()

        localMediaActions.Configure()

        LaunchedEffect(Unit) {
            suspend { createLocalMedia() }.runCatchingUpdatingState(localMedia)
        }

        fun handleEvent(event: FileViewerEvent) {
            when (event) {
                FileViewerEvent.SaveOnDisk -> coroutineScope.saveOnDisk(localMedia.value)
                FileViewerEvent.Share -> coroutineScope.share(localMedia.value)
            }
        }

        return FileViewerState(
            filename = params.filename,
            localMedia = localMedia.value,
            snackbarMessage = snackbarMessage,
            eventSink = ::handleEvent,
        )
    }

    /**
     * Write the content to a temporary file, so that it can be rendered, shared and saved on disk.
     * The file is always written to the same location, so it does not need to be cleaned up.
     */
    private suspend fun createLocalMedia(): LocalMedia = withContext(coroutineDispatchers.io) {
        val folder = File(context.cacheDir, "temp/fileviewer").apply { mkdirs() }
        val file = File(folder, params.filename)
        file.writeText(params.content)
        LocalMedia(
            uri = file.toUri(),
            info = MediaInfo(
                filename = params.filename,
                caption = null,
                mimeType = params.mimeType,
                fileSize = file.length(),
                formattedFileSize = fileSizeFormatter.format(file.length()),
                fileExtension = fileExtensionExtractor.extractFromName(params.filename),
                senderId = null,
                senderName = null,
                senderAvatar = null,
                dateSent = null,
                dateSentFull = null,
                waveform = null,
                duration = null,
            ),
        )
    }

    private fun CoroutineScope.saveOnDisk(localMedia: AsyncData<LocalMedia>) = launch {
        if (localMedia is AsyncData.Success) {
            localMediaActions.saveOnDisk(localMedia.data)
                .onSuccess {
                    snackbarDispatcher.post(SnackbarMessage(CommonStrings.common_file_saved_on_disk_android))
                }
                .onFailure {
                    snackbarDispatcher.post(SnackbarMessage(mediaActionsError(it)))
                }
        }
    }

    private fun CoroutineScope.share(localMedia: AsyncData<LocalMedia>) = launch {
        if (localMedia is AsyncData.Success) {
            localMediaActions.share(localMedia.data)
                .onFailure {
                    snackbarDispatcher.post(SnackbarMessage(mediaActionsError(it)))
                }
        }
    }

    private fun mediaActionsError(throwable: Throwable): Int {
        return if (throwable is ActivityNotFoundException) {
            UtilsR.string.error_no_compatible_app_found
        } else {
            CommonStrings.error_unknown
        }
    }
}
