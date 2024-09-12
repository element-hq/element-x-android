/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.impl.mentions

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.libraries.textcomposer.mentions.MentionSpan
import io.element.android.libraries.textcomposer.mentions.MentionSpanProvider
import io.element.android.tests.testutils.WarmUpRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class IntentionalMentionSpanProviderTest {
    @JvmField @Rule
    val warmUpRule = WarmUpRule()

    private val permalinkParser = FakePermalinkParser()
    private val mentionSpanProvider = MentionSpanProvider(
        permalinkParser = permalinkParser,
    )

    @Test
    fun `getting mention span for a user returns a MentionSpan of type USER`() {
        permalinkParser.givenResult(PermalinkData.UserLink(A_USER_ID))
        val mentionSpan = mentionSpanProvider.getMentionSpanFor("@me:matrix.org", "https://matrix.to/#/${A_USER_ID.value}")
        assertThat(mentionSpan.type).isEqualTo(MentionSpan.Type.USER)
    }

    @Test
    fun `getting mention span for everyone in the room returns a MentionSpan of type EVERYONE`() {
        permalinkParser.givenResult(PermalinkData.FallbackLink(uri = Uri.EMPTY))
        val mentionSpan = mentionSpanProvider.getMentionSpanFor("@room", "#")
        assertThat(mentionSpan.type).isEqualTo(MentionSpan.Type.EVERYONE)
    }

    @Test
    fun `getting mention span for a room returns a MentionSpan of type ROOM`() {
        permalinkParser.givenResult(
            PermalinkData.RoomLink(
                roomIdOrAlias = RoomAlias("#room:matrix.org").toRoomIdOrAlias(),
            )
        )
        val mentionSpan = mentionSpanProvider.getMentionSpanFor("#room:matrix.org", "https://matrix.to/#/#room:matrix.org")
        assertThat(mentionSpan.type).isEqualTo(MentionSpan.Type.ROOM)
    }
}
