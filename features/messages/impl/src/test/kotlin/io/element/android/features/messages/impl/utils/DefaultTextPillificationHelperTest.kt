/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.utils

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.permalink.FakePermalinkBuilder
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.libraries.textcomposer.mentions.MentionSpanFormatter
import io.element.android.libraries.textcomposer.mentions.MentionSpanProvider
import io.element.android.libraries.textcomposer.mentions.MentionSpanTheme
import io.element.android.libraries.textcomposer.mentions.MentionType
import io.element.android.libraries.textcomposer.mentions.getMentionSpans
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultTextPillificationHelperTest {
    @Test
    fun `pillify - adds pills for user ids`() {
        val text = "A @user:server.com"
        val formatter = FakeMentionSpanFormatter()
        val userId = UserId("@user:server.com")
        val helper = aTextPillificationHelper(
            permalinkParser = FakePermalinkParser(result = {
                PermalinkData.UserLink(userId)
            }),
            permalinkBuilder = FakePermalinkBuilder(permalinkForUserLambda = {
                Result.success("https://matrix.to/#/@user:server.com")
            }),
            mentionSpanFormatter = formatter,
        )
        val pillified = helper.pillify(text)
        val mentionSpans = pillified.getMentionSpans()
        assertThat(mentionSpans).hasSize(1)
        val mentionSpan = mentionSpans.first()
        assertThat(mentionSpan.type).isInstanceOf(MentionType.User::class.java)
        val userType = mentionSpan.type as MentionType.User
        assertThat(userType.userId).isEqualTo(userId)
        val formatted = formatter.formatDisplayText(MentionType.User(userId))
        assertThat(mentionSpan.displayText.toString()).isEqualTo(formatted)
    }

    @Test
    fun `pillify - adds pills for room aliases`() {
        val text = "A #room:server.com"
        val roomAlias = RoomAlias("#room:server.com")
        val formatter = FakeMentionSpanFormatter()
        val helper = aTextPillificationHelper(
            permalinkParser = FakePermalinkParser(result = {
                PermalinkData.RoomLink(RoomIdOrAlias.Alias(roomAlias))
            }),
            permalinkBuilder = FakePermalinkBuilder(permalinkForRoomAliasLambda = {
                Result.success("https://matrix.to/#/#room:server.com")
            }),
            mentionSpanFormatter = formatter,
        )
        val pillified = helper.pillify(text)
        val mentionSpans = pillified.getMentionSpans()
        assertThat(mentionSpans).hasSize(1)
        val mentionSpan = mentionSpans.first()
        assertThat(mentionSpan.type).isInstanceOf(MentionType.Room::class.java)
        val roomType = mentionSpan.type as MentionType.Room
        assertThat(roomType.roomIdOrAlias).isEqualTo(roomAlias.toRoomIdOrAlias())
        val formatted = formatter.formatDisplayText(MentionType.Room(roomAlias.toRoomIdOrAlias()))
        assertThat(mentionSpan.displayText.toString()).isEqualTo(formatted)
    }

    @Test
    fun `pillify - adds pills for @room mentions`() {
        val text = "An @room mention"
        val formatter = FakeMentionSpanFormatter()
        val helper = aTextPillificationHelper(
            permalinkParser = FakePermalinkParser(result = {
                PermalinkData.FallbackLink(Uri.EMPTY)
            }),
            mentionSpanFormatter = formatter,
        )
        val pillified = helper.pillify(text)
        val mentionSpans = pillified.getMentionSpans()
        assertThat(mentionSpans).hasSize(1)
        val mentionSpan = mentionSpans.first()
        assertThat(mentionSpan.type).isEqualTo(MentionType.Everyone)
        val formatted = formatter.formatDisplayText(MentionType.Everyone)
        assertThat(mentionSpan.displayText.toString()).isEqualTo(formatted)
    }

    @Test
    fun `pillify - adds pills for message permalinks`() {
        val text = "Check this message: https://matrix.to/#/!roomid:server.com/$123"
        val roomId = RoomId("!roomid:server.com")
        val eventId = EventId("$123")
        val formatter = FakeMentionSpanFormatter()
        val helper = aTextPillificationHelper(
            permalinkParser = FakePermalinkParser(result = {
                PermalinkData.RoomLink(
                    roomIdOrAlias = RoomIdOrAlias.Id(roomId),
                    eventId = eventId
                )
            }),
            permalinkBuilder = FakePermalinkBuilder(),
            mentionSpanFormatter = formatter,
        )
        val pillified = helper.pillify(text)
        val mentionSpans = pillified.getMentionSpans()
        assertThat(mentionSpans).hasSize(1)
        val mentionSpan = mentionSpans.first()
        assertThat(mentionSpan.type).isInstanceOf(MentionType.Message::class.java)
        val messageType = mentionSpan.type as MentionType.Message
        assertThat(messageType.roomIdOrAlias).isEqualTo(roomId.toRoomIdOrAlias())
        assertThat(messageType.eventId).isEqualTo(eventId)
        val formatted = formatter.formatDisplayText(MentionType.Message(roomId.toRoomIdOrAlias(), eventId))
        assertThat(mentionSpan.displayText.toString()).isEqualTo(formatted)
    }

    @Test
    fun `pillify - with pillifyPermalinks false does not add pills for permalinks`() {
        val text = "Check this message: https://matrix.to/#/!roomid:server.com/$123"
        val roomId = RoomId("!roomid:server.com")
        val eventId = EventId("$123")
        val formatter = FakeMentionSpanFormatter()
        val helper = aTextPillificationHelper(
            permalinkParser = FakePermalinkParser(result = {
                PermalinkData.RoomLink(
                    roomIdOrAlias = RoomIdOrAlias.Id(roomId),
                    eventId = eventId
                )
            }),
            permalinkBuilder = FakePermalinkBuilder(),
            mentionSpanFormatter = formatter,
        )
        val pillified = helper.pillify(text, pillifyPermalinks = false)
        val mentionSpans = pillified.getMentionSpans()
        assertThat(mentionSpans).isEmpty()
    }

    @Test
    fun `pillify - with pillifyPermalinks false still adds pills for matrix patterns`() {
        val text = "A @user:server.com mention and a permalink https://matrix.to/#/!roomid:server.com/$123"
        val userId = UserId("@user:server.com")
        val formatter = FakeMentionSpanFormatter()
        val helper = aTextPillificationHelper(
            permalinkParser = FakePermalinkParser(result = {
                PermalinkData.UserLink(userId)
            }),
            permalinkBuilder = FakePermalinkBuilder(permalinkForUserLambda = {
                Result.success("https://matrix.to/#/@user:server.com")
            }),
            mentionSpanFormatter = formatter,
        )
        val pillified = helper.pillify(text, pillifyPermalinks = false)
        val mentionSpans = pillified.getMentionSpans()
        assertThat(mentionSpans).hasSize(1)
        val mentionSpan = mentionSpans.first()
        assertThat(mentionSpan.type).isInstanceOf(MentionType.User::class.java)
        val userType = mentionSpan.type as MentionType.User
        assertThat(userType.userId).isEqualTo(userId)
    }

    @Test
    fun `pillify - with pillifyPermalinks true adds pills for both matrix patterns and permalinks`() {
        val text = "A @user:server.com mention and a permalink https://matrix.to/#/!roomid:server.com/$123"
        val userId = UserId("@user:server.com")
        val roomId = RoomId("!roomid:server.com")
        val eventId = EventId("$123")
        val formatter = FakeMentionSpanFormatter()
        val permalinkParser = FakePermalinkParser(result = { url ->
            if (url.contains("matrix.to")) {
                PermalinkData.RoomLink(
                    roomIdOrAlias = RoomIdOrAlias.Id(roomId),
                    eventId = eventId
                )
            } else {
                PermalinkData.UserLink(userId)
            }
        })
        val helper = aTextPillificationHelper(
            permalinkParser = permalinkParser,
            permalinkBuilder = FakePermalinkBuilder(permalinkForUserLambda = {
                Result.success("https://matrix.to/#/@user:server.com")
            }),
            mentionSpanFormatter = formatter,
        )
        val pillified = helper.pillify(text, pillifyPermalinks = true)
        val mentionSpans = pillified.getMentionSpans()
        assertThat(mentionSpans).hasSize(2)

        // Check that we have both a user mention and a message mention
        val types = mentionSpans.map { it.type::class.java }
        assertThat(types).contains(MentionType.User::class.java)
        assertThat(types).contains(MentionType.Message::class.java)

        // Verify the user mention
        val userMention = mentionSpans.first { it.type is MentionType.User }.type as MentionType.User
        assertThat(userMention.userId).isEqualTo(userId)

        // Verify the message mention
        val messageMention = mentionSpans.first { it.type is MentionType.Message }.type as MentionType.Message
        assertThat(messageMention.roomIdOrAlias).isEqualTo(roomId.toRoomIdOrAlias())
        assertThat(messageMention.eventId).isEqualTo(eventId)
    }

    private fun aTextPillificationHelper(
        permalinkParser: PermalinkParser = FakePermalinkParser(),
        permalinkBuilder: FakePermalinkBuilder = FakePermalinkBuilder(),
        mentionSpanFormatter: MentionSpanFormatter = FakeMentionSpanFormatter(),
    ): TextPillificationHelper {
        val mentionSpanProvider = MentionSpanProvider(
            permalinkParser = permalinkParser,
            mentionSpanFormatter = mentionSpanFormatter,
            mentionSpanTheme = MentionSpanTheme(A_USER_ID),
        )
        return DefaultTextPillificationHelper(
            mentionSpanProvider = mentionSpanProvider,
            permalinkBuilder = permalinkBuilder,
            permalinkParser = permalinkParser,
        )
    }
}
