/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.utils

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.element.android.features.location.api.Location
import io.element.android.features.messages.impl.timeline.model.event.RtcNotificationState
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContent.Mode
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRtcNotificationContent
import io.element.android.features.messages.impl.utils.messagesummary.DefaultMessageSummaryFormatter
import io.element.android.libraries.matrix.api.notification.CallIntent
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.timeline.aProfileDetails
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

class DefaultMessageSummaryFormatterTest : RobolectricTest() {
    private val formatter = DefaultMessageSummaryFormatter(
        RuntimeEnvironment.getApplication() as Context
    )

    @Test
    @Config(qualifiers = "en")
    fun `format call notification started`() {
        val expected = formatter.format(
            TimelineItemRtcNotificationContent(
                callIntent = CallIntent.VIDEO,
                state = RtcNotificationState.Started
            )
        )
        assertThat(expected).isEqualTo("Call started")
    }

    @Test
    @Config(qualifiers = "en")
    fun `format call notification declined by me`() {
        val expected = formatter.format(
            TimelineItemRtcNotificationContent(
                callIntent = CallIntent.VIDEO,
                state = RtcNotificationState.Declined(byMe = true)
            )
        )
        assertThat(expected).isEqualTo("You declined a call")
    }

    @Test
    @Config(qualifiers = "en")
    fun `format call notification declined`() {
        val expected = formatter.format(
            TimelineItemRtcNotificationContent(
                callIntent = CallIntent.VIDEO,
                state = RtcNotificationState.Declined(byMe = false)
            )
        )
        assertThat(expected).isEqualTo("Call declined")
    }

    @Test
    @Config(qualifiers = "en")
    fun `format live location`() {
        val expected = formatter.format(
            aLocationContent(isLive = true)
        )
        assertThat(expected).isEqualTo("Shared live location")
    }

    @Test
    @Config(qualifiers = "en")
    fun `format static location`() {
        val expected = formatter.format(
            aLocationContent(isLive = false)
        )
        assertThat(expected).isEqualTo("Shared location")
    }
}

private fun aLocationContent(isLive: Boolean) = TimelineItemLocationContent(
    senderId = A_USER_ID,
    senderProfile = aProfileDetails(),
    description = null,
    assetType = null,
    mode = if (isLive) {
        Mode.Live(
            lastKnownLocation = Location.fromGeoUri("geo:1,5"),
            isActive = true,
            endsAt = "",
            endTimestamp = 0,
            isOwnUser = true,
        )
    } else {
        Mode.Static(
            location = Location.fromGeoUri("geo:1,5")!!,
        )
    }
)
