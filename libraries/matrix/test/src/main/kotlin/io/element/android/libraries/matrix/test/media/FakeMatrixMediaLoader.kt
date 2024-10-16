/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.media

import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.media.MediaFile
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.tests.testutils.simulateLongTask

class FakeMatrixMediaLoader : MatrixMediaLoader {
    var shouldFail = false
    var path: String = ""

    override suspend fun loadMediaContent(source: MediaSource): Result<ByteArray> = simulateLongTask {
        if (shouldFail) {
            Result.failure(RuntimeException())
        } else {
            Result.success(ByteArray(0))
        }
    }

    override suspend fun loadMediaThumbnail(source: MediaSource, width: Long, height: Long): Result<ByteArray> = simulateLongTask {
        if (shouldFail) {
            Result.failure(RuntimeException())
        } else {
            Result.success(ByteArray(0))
        }
    }

    override suspend fun downloadMediaFile(
        source: MediaSource,
        mimeType: String?,
        filename: String?,
        useCache: Boolean,
    ): Result<MediaFile> = simulateLongTask {
        if (shouldFail) {
            Result.failure(RuntimeException())
        } else {
            Result.success(FakeMediaFile(path))
        }
    }
}
