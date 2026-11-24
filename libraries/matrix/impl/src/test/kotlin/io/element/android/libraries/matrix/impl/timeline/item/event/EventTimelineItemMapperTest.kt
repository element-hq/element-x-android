/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline.item.event

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustEventTimelineItem
import org.junit.Test

class EventTimelineItemMapperTest {
    private val sut = EventTimelineItemMapper()

    @Test
    fun `map should map the redaction reason`() {
        val result = sut.map(aRustEventTimelineItem(redactionReason = "Spam"))
        assertThat(result.redactionReason).isEqualTo("Spam")
    }

    @Test
    fun `map should map a missing redaction reason`() {
        val result = sut.map(aRustEventTimelineItem(redactionReason = null))
        assertThat(result.redactionReason).isNull()
    }
}
