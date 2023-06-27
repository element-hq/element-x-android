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

package io.element.android.features.messages.impl.timeline.model.event

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.media.MediaSource

open class TimelineItemFileContentProvider : PreviewParameterProvider<TimelineItemFileContent> {
    override val values: Sequence<TimelineItemFileContent>
        get() = sequenceOf(
            aTimelineItemFileContent("A file.pdf"),
            aTimelineItemFileContent("A bigger name file.pdf"),
            aTimelineItemFileContent("An even bigger bigger bigger bigger bigger bigger bigger file name which doesn't fit .pdf"),
        )
}

fun aTimelineItemFileContent(fileName: String = "A file.pdf") = TimelineItemFileContent(
    body = fileName,
    thumbnailSource = MediaSource(url = ""),
    fileSource = MediaSource(url = ""),
    mimeType = MimeTypes.Pdf,
    formattedFileSize = "100kB",
    fileExtension = "pdf"
)
