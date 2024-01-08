/*
 * Copyright (c) 2022 New Vector Ltd
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

import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.mediaviewer.api.helper.formatFileExtensionAndSize
import kotlin.time.Duration

data class TimelineItemAudioContent(
    val body: String,
    val duration: Duration,
    val mediaSource: MediaSource,
    val mimeType: String,
    val formattedFileSize: String,
    val fileExtension: String,
) : TimelineItemEventContent {

    val fileExtensionAndSize =
        formatFileExtensionAndSize(
            fileExtension,
            formattedFileSize
        )
    override val type: String = "TimelineItemAudioContent"
}
