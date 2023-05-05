/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.libraries.matrix.test.media

import android.net.Uri
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.media.MatrixMediaSource
import java.io.File

class FakeMediaLoader : MatrixMediaLoader {

    var shouldFail = false

    override suspend fun loadMediaContent(source: MatrixMediaSource): Result<ByteArray> {
        return if (shouldFail) {
            Result.failure(RuntimeException())
        } else {
            return Result.success(ByteArray(0))
        }
    }

    override suspend fun loadMediaThumbnail(source: MatrixMediaSource, width: Long, height: Long): Result<ByteArray> {
        return if (shouldFail) {
            Result.failure(RuntimeException())
        } else {
            return Result.success(ByteArray(0))
        }
    }

    override suspend fun loadMediaFile(source: MatrixMediaSource, mimeType: String?): Result<Uri> {
        return if (shouldFail) {
            Result.failure(RuntimeException())
        } else {
            return Result.success(Uri.fromFile(File("path")))
        }
    }
}
