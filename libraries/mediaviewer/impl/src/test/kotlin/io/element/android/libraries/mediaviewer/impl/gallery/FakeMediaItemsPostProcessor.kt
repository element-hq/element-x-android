/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery

import io.element.android.libraries.architecture.AsyncData
import kotlinx.collections.immutable.ImmutableList

class FakeMediaItemsPostProcessor : MediaItemsPostProcessor {
    override fun process(mediaItems: AsyncData<ImmutableList<MediaItem>>, predicate: (MediaItem.Event) -> Boolean): AsyncData<ImmutableList<MediaItem>> {
        return mediaItems
    }
}
