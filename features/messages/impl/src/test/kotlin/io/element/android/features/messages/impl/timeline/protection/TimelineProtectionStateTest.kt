/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.protection

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.AN_EVENT_ID_2
import kotlinx.collections.immutable.persistentSetOf
import org.junit.Test

class TimelineProtectionStateTest {
    @Test
    fun `when protectionState is RenderAll, hideMediaContent always return null`() {
        val sut = aTimelineProtectionState(
            protectionState = ProtectionState.RenderAll
        )
        assertThat(sut.hideMediaContent(null)).isFalse()
        assertThat(sut.hideMediaContent(AN_EVENT_ID)).isFalse()
    }

    @Test
    fun `when protectionState is RenderOnly with empty set, hideMediaContent always return true`() {
        val sut = aTimelineProtectionState(
            protectionState = ProtectionState.RenderOnly(persistentSetOf())
        )
        assertThat(sut.hideMediaContent(null)).isTrue()
        assertThat(sut.hideMediaContent(AN_EVENT_ID)).isTrue()
    }

    @Test
    fun `when protectionState is RenderOnly with an Event, hideMediaContent can return true or false`() {
        val sut = aTimelineProtectionState(
            protectionState = ProtectionState.RenderOnly(persistentSetOf(AN_EVENT_ID))
        )
        assertThat(sut.hideMediaContent(null)).isTrue()
        assertThat(sut.hideMediaContent(AN_EVENT_ID)).isFalse()
        assertThat(sut.hideMediaContent(AN_EVENT_ID_2)).isTrue()
    }
}
