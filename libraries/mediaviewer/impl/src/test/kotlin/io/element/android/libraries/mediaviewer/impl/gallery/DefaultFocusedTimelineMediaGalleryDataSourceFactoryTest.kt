/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.mediaviewer.impl.gallery.ui.aMediaItemImage
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultFocusedTimelineMediaGalleryDataSourceFactoryTest {
    @Test
    fun `createFor should create a TimelineMediaGalleryDataSource`() = runTest {
        val sut = DefaultFocusedTimelineMediaGalleryDataSourceFactory(
            room = FakeMatrixRoom(),
            timelineMediaItemsFactory = createTimelineMediaItemsFactory(),
            mediaItemsPostProcessor = MediaItemsPostProcessor(),
        )
        val result = sut.createFor(
            eventId = AN_EVENT_ID,
            mediaItem = aMediaItemImage(),
        )
        assertThat(result).isInstanceOf(TimelineMediaGalleryDataSource::class.java)
    }
}
