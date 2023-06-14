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

package io.element.android.libraries.mediaupload.test

import android.net.Uri
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import kotlinx.coroutines.delay
import java.io.File

class FakeMediaPreProcessor : MediaPreProcessor {

    private var result: Result<MediaUploadInfo> = Result.success(
        MediaUploadInfo.AnyFile(
            File("test"),
            FileInfo(
                mimetype = "*/*",
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
    ): Result<MediaUploadInfo> {
        delay(1)
        return result
    }

    fun givenResult(value: Result<MediaUploadInfo>) {
        this.result = value
    }
}
