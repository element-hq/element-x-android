/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.di.RoomScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

interface MediaItemsPostProcessor {
    fun process(
        mediaItems: AsyncData<ImmutableList<MediaItem>>,
        predicate: (MediaItem.Event) -> Boolean,
    ): AsyncData<ImmutableList<MediaItem>>
}

@ContributesBinding(RoomScope::class)
class DefaultMediaItemsPostProcessor @Inject constructor(
) : MediaItemsPostProcessor {
    override fun process(
        mediaItems: AsyncData<ImmutableList<MediaItem>>,
        predicate: (MediaItem.Event) -> Boolean,
    ): AsyncData<ImmutableList<MediaItem>> {
        return when (mediaItems) {
            is AsyncData.Uninitialized -> mediaItems
            is AsyncData.Loading -> mediaItems
            is AsyncData.Failure -> mediaItems
            is AsyncData.Success -> AsyncData.Success(
                process(
                    mediaItems = mediaItems.data,
                    predicate = predicate,
                )
            )
        }
    }

    private fun process(
        mediaItems: List<MediaItem>,
        predicate: (MediaItem.Event) -> Boolean,
    ) = buildList {
        val eventList = mutableListOf<MediaItem.Event>()
        for (item in mediaItems) {
            when (item) {
                is MediaItem.DateSeparator -> {
                    if (eventList.isNotEmpty()) {
                        // Date separator first
                        add(item)
                        // Then events
                        addAll(eventList)
                        eventList.clear()
                    }
                }
                is MediaItem.Event -> {
                    if (predicate(item)) {
                        eventList.add(item)
                    }
                }
                is MediaItem.LoadingIndicator -> {
                    add(item)
                }
            }
        }
    }.toImmutableList()
}
