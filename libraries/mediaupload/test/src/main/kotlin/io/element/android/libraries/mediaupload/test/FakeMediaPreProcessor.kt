/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.test

import android.net.Uri
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.mediaupload.api.MediaOptimizationConfig
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import io.element.android.tests.testutils.simulateLongTask
import kotlinx.coroutines.CompletableDeferred
import java.io.File
import kotlin.time.Duration.Companion.seconds

class FakeMediaPreProcessor(
    private val processLatch: CompletableDeferred<Unit>? = null,
) : MediaPreProcessor {
    var processCallCount = 0
        private set

    var cleanUpCallCount = 0
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
        mediaOptimizationConfig: MediaOptimizationConfig,
    ): Result<MediaUploadInfo> = simulateLongTask {
        processLatch?.await()
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

    fun givenImageResult() {
        givenResult(
            Result.success(
                MediaUploadInfo.Image(
                    file = File("image.jpg"),
                    imageInfo = ImageInfo(
                        height = 100,
                        width = 100,
                        mimetype = MimeTypes.Jpeg,
                        size = 1000,
                        thumbnailInfo = null,
                        thumbnailSource = null,
                        blurhash = null,
                    ),
                    thumbnailFile = null,
                )
            )
        )
    }

    fun givenVideoResult() {
        givenResult(
            Result.success(
                MediaUploadInfo.Video(
                    file = File("image.jpg"),
                    videoInfo = VideoInfo(
                        duration = 1000.seconds,
                        height = 100,
                        width = 100,
                        mimetype = MimeTypes.Mp4,
                        size = 1000,
                        thumbnailInfo = null,
                        thumbnailSource = null,
                        blurhash = null,
                    ),
                    thumbnailFile = null,
                )
            )
        )
    }

    override fun cleanUp() {
        cleanUpCallCount += 1
    }
}
