/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.test

import android.net.Uri
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import io.element.android.tests.testutils.simulateLongTask
import java.io.File
import kotlin.time.Duration.Companion.seconds

class FakeMediaPreProcessor : MediaPreProcessor {
    var processCallCount = 0
        private set

    private var result: Result<MediaUploadInfo> = Result.success(
        MediaUploadInfo.AnyFile(
            File("test"),
            FileInfo(
                mimetype = MimeTypes.Any,
                size = 999L,
                thumbnailInfo = null,
                thumbnailSource = null,
            )
        )
    )

    override suspend fun process(
        uri: Uri,
        mimeType: String,
        deleteOriginal: Boolean,
        compressIfPossible: Boolean
    ): Result<MediaUploadInfo> = simulateLongTask {
        processCallCount++
        result
    }

    fun givenResult(value: Result<MediaUploadInfo>) {
        this.result = value
    }

    fun givenAudioResult() {
        givenResult(
            Result.success(
                MediaUploadInfo.Audio(
                    file = File("audio.ogg"),
                    audioInfo = AudioInfo(
                        duration = 1000.seconds,
                        size = 1000,
                        mimetype = MimeTypes.Ogg,
                    ),
                )
            )
        )
    }
}
