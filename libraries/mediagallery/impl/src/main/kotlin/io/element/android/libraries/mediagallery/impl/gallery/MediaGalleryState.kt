/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediagallery.impl.gallery

import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.mediagallery.impl.R
import kotlinx.collections.immutable.ImmutableList

data class MediaGalleryState(
    val roomName: String,
    val mode: MediaGalleryMode,
    val imageItems: AsyncData<ImmutableList<MediaItem>>,
    val fileItems: AsyncData<ImmutableList<MediaItem>>,
    val eventSink: (MediaGalleryEvents) -> Unit,
)

enum class MediaGalleryMode(val stringResource: Int) {
    Images(R.string.screen_media_browser_list_mode_media),
    Files(R.string.screen_media_browser_list_mode_files),
}
