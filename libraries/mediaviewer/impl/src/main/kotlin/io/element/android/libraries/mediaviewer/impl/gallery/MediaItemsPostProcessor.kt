/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery

import io.element.android.libraries.architecture.AsyncData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class MediaItemsPostProcessor @Inject constructor() {
    fun process(
        mediaItems: AsyncData<ImmutableList<MediaItem>>,
    ): AsyncData<GroupedMediaItems> {
        return when (mediaItems) {
            is AsyncData.Uninitialized -> AsyncData.Uninitialized
            is AsyncData.Loading -> AsyncData.Loading()
            is AsyncData.Failure -> AsyncData.Failure(mediaItems.error)
            is AsyncData.Success -> AsyncData.Success(
                mediaItems.data.process()
            )
        }
    }

    private fun List<MediaItem>.process(): GroupedMediaItems {
        val imageAndVideoItems = mutableListOf<MediaItem>()
        val fileItems = mutableListOf<MediaItem>()

        val imageAndVideoItemsSubList = mutableListOf<MediaItem.Event>()
        val fileItemsSublist = mutableListOf<MediaItem.Event>()
        forEach { item ->
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
