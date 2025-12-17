/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.viewer

import dev.zacsweers.metro.Inject
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import io.element.android.libraries.mediaviewer.impl.model.eventId

/**
 * x and y are loading items.
 * Capital letters are media items.
 * First list emitted
 *   x  F  G  H  y
 * indexes will be
 *   0  1  2  3  4
 * (keyOffset = 0)
 * New items added to the end of the list
 *   x  F  G  H  I  J  K  y
 * indexes will be
 *   0  1  2  3  4  5  6  7
 *  (keyOffset = 0)
 * New items added to the beginning of the list
 *   x  D  E  F  G  H  I  J  K  y
 * indexes will be
 *  -2 -1  0  1  2  3  4  5  6  7
 * (keyOffset = -2)
 * loader item vanishes
 *   D  E  F  G  H  I  J  K
 *  indexes will be
 *  -1  0  1  2  3  4  5  6
 * (keyOffset = -1)
 */
@Inject
class PagerKeysHandler {
    private data class Data(
        val mediaItems: List<MediaItem>,
        val keyOffset: Long,
    )

    // Will store the list of media items and the key offset of the first item in the list
    private var cachedData: Data = Data(emptyList(), 0)

    fun accept(mediaItems: List<MediaItem>) {
        if (cachedData.mediaItems.isEmpty()) {
            cachedData = Data(mediaItems, 0)
        } else {
            // Search a common item in both lists, i.e. an item with the same eventId
            val itemInCacheIndex = cachedData.mediaItems.indexOfFirst { mediaItem ->
                mediaItem is MediaItem.Event && mediaItems
                    .filterIsInstance<MediaItem.Event>()
                    .any { mediaItem.eventId() == it.eventId() }
            }
            cachedData = if (itemInCacheIndex == -1) {
                // If the item is not found, start with a new cache
                Data(mediaItems, 0)
            } else {
                val cachedItem = cachedData.mediaItems[itemInCacheIndex]
                val eventId = (cachedItem as? MediaItem.Event)?.eventId()
                if (eventId == null) {
                    // Should not happen, but in this case, start with a new cache
                    Data(mediaItems, 0)
                } else {
                    // Search the index of the item in the new list
                    val itemIndex = mediaItems.indexOfFirst { mediaItem ->
                        mediaItem is MediaItem.Event && mediaItem.eventId() == eventId
                    }
                    if (itemIndex == -1) {
                        // If the item is not found, start with a new cache
                        Data(mediaItems, 0)
                    } else {
                        // Update the cache with the new list and the new offset
                        Data(mediaItems, cachedData.keyOffset + itemInCacheIndex - itemIndex.toLong())
                    }
                }
            }
        }
    }

    fun getKey(mediaItem: MediaItem): Long {
        return cachedData.mediaItems.indexOf(mediaItem) + cachedData.keyOffset
    }
}
