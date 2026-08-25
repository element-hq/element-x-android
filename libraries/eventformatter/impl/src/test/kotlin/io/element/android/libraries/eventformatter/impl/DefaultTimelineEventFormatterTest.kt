/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.eventformatter.impl

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.timeline.item.event.aRoomMembershipContent
import io.element.android.services.toolbox.impl.strings.AndroidStringProvider
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Before
import org.junit.Test
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

class DefaultTimelineEventFormatterTest : RobolectricTest() {
    private lateinit var context: Context
    private lateinit var formatter: DefaultTimelineEventFormatter

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication() as Context
        val fakeMatrixClient = FakeMatrixClient()
        val stringProvider = AndroidStringProvider(context.resources)
        formatter = DefaultTimelineEventFormatter(
            sp = stringProvider,
            buildMeta = aBuildMeta(),
            roomMembershipContentFormatter = RoomMembershipContentFormatter(fakeMatrixClient, stringProvider),
            profileChangeContentFormatter = ProfileChangeContentFormatter(stringProvider),
            stateContentFormatter = StateContentFormatter(stringProvider),
        )
    }

    @Test
    @Config(qualifiers = "en")
    fun `a membership event that changes nothing is not rendered`() {
        val someoneElse = UserId("@someone_else:domain")
        val byYou = aRoomMembershipContent(A_USER_ID, null, MembershipChange.NONE)
        val bySomeoneElse = aRoomMembershipContent(someoneElse, "Other", MembershipChange.NONE)

        assertThat(
            formatter.format(
                content = byYou,
                isOutgoing = true,
                sender = A_USER_ID,
                senderDisambiguatedDisplayName = "You",
            )
        ).isNull()
        assertThat(
            formatter.format(
                content = bySomeoneElse,
                isOutgoing = false,
                sender = someoneElse,
                senderDisambiguatedDisplayName = "Other",
            )
        ).isNull()
    }

    @Test
    @Config(qualifiers = "en")
    fun `a membership event that changes something is still rendered`() {
        val joined = aRoomMembershipContent(A_USER_ID, null, MembershipChange.JOINED)

        val result = formatter.format(
            content = joined,
            isOutgoing = true,
            sender = A_USER_ID,
            senderDisambiguatedDisplayName = "You",
        )

        assertThat(result.toString()).isEqualTo("You joined the room")
    }
}
