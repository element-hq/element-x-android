/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.selection

import androidx.annotation.StringRes
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.messages.impl.R
import io.element.android.libraries.core.extensions.mapCatchingExceptions
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.local.LocalMediaFactory
import io.element.android.libraries.mediaviewer.api.local.MediaFileSaver
import io.element.android.libraries.ui.strings.CommonStrings
import timber.log.Timber

/** Downloads a single selected file and writes it to the Downloads folder. */
interface SelectionMediaSaver {
    suspend fun save(media: SavableMedia): Result<Unit>
}

/**
 * Saves the files one after the other, so that a batch of videos does not start several downloads at
 * once, and returns how many were written. A file which cannot be saved does not stop the batch.
 */
suspend fun SelectionMediaSaver.saveAll(
    media: List<SavableMedia>,
    onProgress: (saved: Int) -> Unit,
): Int {
    var saved = 0
    media.forEach { item ->
        save(item)
            .onSuccess {
                saved++
                onProgress(saved)
            }
            .onFailure { Timber.w(it, "Failed to save one file out of ${media.size}") }
    }
    return saved
}

@ContributesBinding(SessionScope::class)
class DefaultSelectionMediaSaver(
    private val mediaLoader: MatrixMediaLoader,
    private val localMediaFactory: LocalMediaFactory,
    private val mediaFileSaver: MediaFileSaver,
) : SelectionMediaSaver {
    override suspend fun save(media: SavableMedia): Result<Unit> {
        return mediaLoader.downloadMediaFile(
            source = media.source,
            mimeType = media.mimeType,
            filename = media.filename,
        ).mapCatchingExceptions { mediaFile ->
            mediaFile.use {
                val localMedia = localMediaFactory.createFromMediaFile(
                    mediaFile = it,
                    mediaInfo = media.toMediaInfo(),
                )
                mediaFileSaver.saveInDownloads(localMedia).getOrThrow()
            }
        }
    }
}

private fun SavableMedia.toMediaInfo() = MediaInfo(
    filename = filename,
    caption = null,
    mimeType = mimeType,
    fileSize = null,
    formattedFileSize = "",
    fileExtension = filename.substringAfterLast('.', ""),
    senderId = null,
    senderName = null,
    senderAvatar = null,
    dateSent = null,
    dateSentFull = null,
    waveform = null,
    duration = null,
)

/** Message to show once a batch is done, which differs for a full, partial or failed batch. */
@StringRes
fun bulkSaveMessage(saved: Int, total: Int): Int = when {
    saved == 0 -> CommonStrings.common_error
    saved < total -> R.string.screen_room_selection_saved_partly
    saved == 1 -> CommonStrings.common_file_saved_on_disk_android
    else -> R.string.screen_room_selection_saved
}
