/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline.item.event

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustEventTimelineItem
import org.junit.Test
import org.matrix.rustcomponents.sdk.EventSendState
import org.matrix.rustcomponents.sdk.QueueWedgeError

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

    @Test
    fun `map should map a recoverable sending failure`() {
        val result = sut.map(
            aRustEventTimelineItem(
                localSendState = EventSendState.SendingFailed(
                    error = QueueWedgeError.GenericApiError("An error"),
                    isRecoverable = true,
                )
            )
        )
        assertThat(result.localSendState).isEqualTo(
            LocalEventSendState.Failed.Unknown(error = "An error", isRecoverable = true)
        )
    }

    @Test
    fun `map should map an unrecoverable sending failure`() {
        val result = sut.map(
            aRustEventTimelineItem(
                localSendState = EventSendState.SendingFailed(
                    error = QueueWedgeError.CrossVerificationRequired,
                    isRecoverable = false,
                )
            )
        )
        assertThat(result.localSendState).isEqualTo(
            LocalEventSendState.Failed.SendingFromUnverifiedDevice(isRecoverable = false)
        )
    }
}
