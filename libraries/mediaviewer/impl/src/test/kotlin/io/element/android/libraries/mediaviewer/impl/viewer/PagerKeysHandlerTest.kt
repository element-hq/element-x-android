/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.viewer

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.AN_EVENT_ID_2
import io.element.android.libraries.mediaviewer.impl.model.aMediaItemImage
import io.element.android.libraries.mediaviewer.impl.model.aMediaItemLoadingIndicator
import org.junit.Test

class PagerKeysHandlerTest {
    private val image1 = aMediaItemImage(
        eventId = AN_EVENT_ID,
    )
    private val image2 = aMediaItemImage(
        eventId = AN_EVENT_ID_2,
    )
    private val aBackwardLoadingIndicator = aMediaItemLoadingIndicator(
        direction = Timeline.PaginationDirection.BACKWARDS
    )
    private val aForwardLoadingIndicator = aMediaItemLoadingIndicator(
        direction = Timeline.PaginationDirection.FORWARDS
    )

    @Test
    fun `when new items are inserted after existing items, keys are not shifted`() {
        val sut = PagerKeysHandler()
        sut.accept(listOf(aBackwardLoadingIndicator, image1, aForwardLoadingIndicator))
        assertThat(sut.getKey(aBackwardLoadingIndicator)).isEqualTo(0)
        assertThat(sut.getKey(image1)).isEqualTo(1)
        assertThat(sut.getKey(aForwardLoadingIndicator)).isEqualTo(2)
        sut.accept(listOf(aBackwardLoadingIndicator, image1, image2, aForwardLoadingIndicator))
        assertThat(sut.getKey(aBackwardLoadingIndicator)).isEqualTo(0)
        assertThat(sut.getKey(image1)).isEqualTo(1)
        assertThat(sut.getKey(image2)).isEqualTo(2)
        assertThat(sut.getKey(aForwardLoadingIndicator)).isEqualTo(3)
    }

    @Test
    fun `when new items are inserted before existing items, keys are not shifted`() {
        val sut = PagerKeysHandler()
        sut.accept(listOf(aBackwardLoadingIndicator, image1, aForwardLoadingIndicator))
        assertThat(sut.getKey(aBackwardLoadingIndicator)).isEqualTo(0)
        assertThat(sut.getKey(image1)).isEqualTo(1)
        assertThat(sut.getKey(aForwardLoadingIndicator)).isEqualTo(2)
        sut.accept(listOf(aBackwardLoadingIndicator, image2, image1, aForwardLoadingIndicator))
        assertThat(sut.getKey(aBackwardLoadingIndicator)).isEqualTo(-1)
        assertThat(sut.getKey(image2)).isEqualTo(0)
        assertThat(sut.getKey(image1)).isEqualTo(1)
        assertThat(sut.getKey(aForwardLoadingIndicator)).isEqualTo(2)
        // Accepting the same list should not change the keys
        sut.accept(listOf(aBackwardLoadingIndicator, image2, image1, aForwardLoadingIndicator))
        assertThat(sut.getKey(aBackwardLoadingIndicator)).isEqualTo(-1)
        assertThat(sut.getKey(image2)).isEqualTo(0)
        assertThat(sut.getKey(image1)).isEqualTo(1)
        assertThat(sut.getKey(aForwardLoadingIndicator)).isEqualTo(2)
    }

    @Test
    fun `when loaders are removed, keys are not shifted`() {
        val sut = PagerKeysHandler()
        sut.accept(listOf(aBackwardLoadingIndicator, image1, aForwardLoadingIndicator))
        assertThat(sut.getKey(aBackwardLoadingIndicator)).isEqualTo(0)
        assertThat(sut.getKey(image1)).isEqualTo(1)
        assertThat(sut.getKey(aForwardLoadingIndicator)).isEqualTo(2)
        sut.accept(listOf(image1))
        assertThat(sut.getKey(image1)).isEqualTo(1)
    }
}
