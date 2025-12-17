/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.datasource

import dev.zacsweers.metro.Inject
import io.element.android.libraries.mediaviewer.impl.model.GroupedMediaItems
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import kotlinx.collections.immutable.toImmutableList

@Inject
class MediaItemsPostProcessor {
    fun process(
        mediaItems: List<MediaItem>,
    ): GroupedMediaItems {
        val imageAndVideoItems = mutableListOf<MediaItem>()
        val fileItems = mutableListOf<MediaItem>()

        val imageAndVideoItemsSubList = mutableListOf<MediaItem.Event>()
        val fileItemsSublist = mutableListOf<MediaItem.Event>()
        mediaItems.forEach { item ->
            when (item) {
                is MediaItem.DateSeparator -> {
                    if (imageAndVideoItemsSubList.isNotEmpty()) {
                        // Date separator first
                        imageAndVideoItems.add(item)
                        // Then events
                        imageAndVideoItems.addAll(imageAndVideoItemsSubList)
                        imageAndVideoItemsSubList.clear()
                    }
                    if (fileItemsSublist.isNotEmpty()) {
                        // Date separator first
                        fileItems.add(item)
                        // Then events
                        fileItems.addAll(fileItemsSublist)
                        fileItemsSublist.clear()
                    }
                }
                is MediaItem.Event -> {
                    when (item) {
                        is MediaItem.Image,
                        is MediaItem.Video -> {
                            imageAndVideoItemsSubList.add(item)
                        }
                        is MediaItem.Audio,
                        is MediaItem.Voice,
                        is MediaItem.File -> {
                            fileItemsSublist.add(item)
                        }
                    }
                }
                is MediaItem.LoadingIndicator -> {
                    imageAndVideoItems.add(item)
                    fileItems.add(item)
                }
            }
        }
        if (imageAndVideoItemsSubList.isNotEmpty()) {
            // Should not happen, since the SDK is always adding a date separator
            imageAndVideoItems.addAll(imageAndVideoItemsSubList)
        }
        if (fileItemsSublist.isNotEmpty()) {
            // Should not happen, since the SDK is always adding a date separator
            fileItems.addAll(fileItemsSublist)
        }
        return GroupedMediaItems(
            imageAndVideoItems = imageAndVideoItems.toImmutableList(),
            fileItems = fileItems.toImmutableList(),
        )
    }
}
