/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.selection

import androidx.compose.runtime.Immutable
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemAttachmentsContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContentWithAttachment
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemGalleryContent
import io.element.android.libraries.matrix.api.media.MediaSource

/** Progress of an ongoing bulk save, in files. */
@Immutable
data class SelectionSaveProgress(
    val saved: Int,
    val total: Int,
)

/** A single file to download and write to the Downloads folder. */
data class SavableMedia(
    val source: MediaSource,
    val filename: String,
    val mimeType: String,
)

/** The files carried by an event. A gallery or an attachment group counts as all of its items. */
fun TimelineItemEventContent.savableMedia(): List<SavableMedia> = when (this) {
    is TimelineItemEventContentWithAttachment -> listOf(SavableMedia(mediaSource, filename, mimeType))
    is TimelineItemGalleryContent -> items.map { SavableMedia(it.mediaSource, it.filename, it.mimeType) }
    is TimelineItemAttachmentsContent -> attachments.map { SavableMedia(it.mediaSource, it.filename, it.mimeType) }
    else -> emptyList()
}
