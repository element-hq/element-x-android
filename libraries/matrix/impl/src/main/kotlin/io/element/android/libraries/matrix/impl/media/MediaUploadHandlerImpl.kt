/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.media

import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.media.MediaUploadHandler
import org.matrix.rustcomponents.sdk.SendAttachmentJoinHandle
import java.io.File

class MediaUploadHandlerImpl(
    private val filesToUpload: List<File>,
    private val sendAttachmentJoinHandle: SendAttachmentJoinHandle,
) : MediaUploadHandler {
    override suspend fun await(): Result<Unit> =
        runCatchingExceptions {
            sendAttachmentJoinHandle.join()
        }
            .also { cleanUpFiles() }

    override fun cancel() {
        sendAttachmentJoinHandle.cancel()
        cleanUpFiles()
    }

    private fun cleanUpFiles() {
        filesToUpload.forEach { file -> file.safeDelete() }
    }
}
