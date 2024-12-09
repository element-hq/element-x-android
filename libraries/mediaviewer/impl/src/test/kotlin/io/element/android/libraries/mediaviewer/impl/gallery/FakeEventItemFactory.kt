/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery

import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem

class FakeEventItemFactory : EventItemFactory {
    override fun create(currentTimelineItem: MatrixTimelineItem.Event): MediaItem.Event? {
        return null
    }
}
