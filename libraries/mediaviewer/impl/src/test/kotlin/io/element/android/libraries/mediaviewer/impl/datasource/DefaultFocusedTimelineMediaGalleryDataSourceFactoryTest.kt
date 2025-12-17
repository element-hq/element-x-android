/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.datasource

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.mediaviewer.impl.model.aMediaItemImage
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultFocusedTimelineMediaGalleryDataSourceFactoryTest {
    @Test
    fun `createFor should create a TimelineMediaGalleryDataSource`() = runTest {
        val sut = DefaultFocusedTimelineMediaGalleryDataSourceFactory(
            room = FakeJoinedRoom(),
            timelineMediaItemsFactory = createTimelineMediaItemsFactory(),
            mediaItemsPostProcessor = MediaItemsPostProcessor(),
        )
        val result = sut.createFor(
            eventId = AN_EVENT_ID,
            mediaItem = aMediaItemImage(),
            onlyPinnedEvents = false,
        )
        assertThat(result).isInstanceOf(TimelineMediaGalleryDataSource::class.java)
    }
}
