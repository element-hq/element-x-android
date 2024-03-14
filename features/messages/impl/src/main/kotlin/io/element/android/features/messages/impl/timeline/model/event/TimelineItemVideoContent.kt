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
import io.element.android.libraries.matrix.api.timeline.item.event.FormattedBody
import kotlin.time.Duration

data class TimelineItemVideoContent(
    val body: String,
    val formatted: FormattedBody?,
    val filename: String?,
    val duration: Duration,
    val videoSource: MediaSource,
    val thumbnailSource: MediaSource?,
    val aspectRatio: Float?,
    val blurHash: String?,
    val height: Int?,
    val width: Int?,
    val mimeType: String,
    val formattedFileSize: String,
    val fileExtension: String,
) : TimelineItemEventContent {
    override val type: String = "TimelineItemImageContent"

    val showCaption = filename != null && filename != body
    val caption = if (showCaption) body else ""
}
