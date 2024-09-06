/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.utils

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.test.permalink.FakePermalinkBuilder
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.libraries.matrix.ui.messages.RoomMemberProfilesCache
import io.element.android.libraries.textcomposer.mentions.MentionSpan
import io.element.android.libraries.textcomposer.mentions.MentionSpanProvider
import io.element.android.libraries.textcomposer.mentions.getMentionSpans
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultTextPillificationHelperTest {
    @Test
    fun `pillify - adds pills for user ids`() {
        val text = "A @user:server.com"
        val helper = aTextPillificationHelper(
            permalinkparser = FakePermalinkParser(result = {
                PermalinkData.UserLink(UserId("@user:server.com"))
            }),
            permalinkBuilder = FakePermalinkBuilder(permalinkForUserLambda = {
                Result.success("https://matrix.to/#/@user:server.com")
            }),
        )
        val pillified = helper.pillify(text)
        val mentionSpans = pillified.getMentionSpans()
        assertThat(mentionSpans).hasSize(1)
        val mentionSpan = mentionSpans.firstOrNull()
        assertThat(mentionSpan?.type).isEqualTo(MentionSpan.Type.USER)
        assertThat(mentionSpan?.rawValue).isEqualTo("@user:server.com")
        assertThat(mentionSpan?.text).isEqualTo("@user:server.com")
    }

    @Test
    fun `pillify - uses the cached display name for user mentions`() {
        val text = "A @user:server.com"
        val helper = aTextPillificationHelper(
            permalinkparser = FakePermalinkParser(result = {
                PermalinkData.UserLink(UserId("@user:server.com"))
            }),
            permalinkBuilder = FakePermalinkBuilder(permalinkForUserLambda = {
                Result.success("https://matrix.to/#/@user:server.com")
            }),
            roomMemberProfilesCache = RoomMemberProfilesCache().apply {
                replace(listOf(aRoomMember(userId = UserId("@user:server.com"), displayName = "Alice")))
            },
        )
        val pillified = helper.pillify(text)
        val mentionSpans = pillified.getMentionSpans()
        assertThat(mentionSpans).hasSize(1)
        val mentionSpan = mentionSpans.firstOrNull()
        assertThat(mentionSpan?.type).isEqualTo(MentionSpan.Type.USER)
        assertThat(mentionSpan?.rawValue).isEqualTo("@user:server.com")
        assertThat(mentionSpan?.text).isEqualTo("Alice")
    }

    @Test
    fun `pillify - adds pills for room aliases`() {
        val text = "A #room:server.com"
        val helper = aTextPillificationHelper(
            permalinkparser = FakePermalinkParser(result = {
                PermalinkData.RoomLink(RoomIdOrAlias.Alias(RoomAlias("#room:server.com")))
            }),
            permalinkBuilder = FakePermalinkBuilder(permalinkForRoomAliasLambda = {
                Result.success("https://matrix.to/#/#room:server.com")
            }),
        )
        val pillified = helper.pillify(text)
        val mentionSpans = pillified.getMentionSpans()
        assertThat(mentionSpans).hasSize(1)
        val mentionSpan = mentionSpans.firstOrNull()
        assertThat(mentionSpan?.type).isEqualTo(MentionSpan.Type.ROOM)
        assertThat(mentionSpan?.rawValue).isEqualTo("#room:server.com")
        assertThat(mentionSpan?.text).isEqualTo("#room:server.com")
    }

    @Test
    fun `pillify - adds pills for @room mentions`() {
        val text = "An @room mention"
        val helper = aTextPillificationHelper(permalinkparser = FakePermalinkParser(result = {
            PermalinkData.FallbackLink(Uri.EMPTY)
        }))
        val pillified = helper.pillify(text)
        val mentionSpans = pillified.getMentionSpans()
        assertThat(mentionSpans).hasSize(1)
        val mentionSpan = mentionSpans.firstOrNull()
        assertThat(mentionSpan?.type).isEqualTo(MentionSpan.Type.EVERYONE)
        assertThat(mentionSpan?.rawValue).isEqualTo("@room")
        assertThat(mentionSpan?.text).isEqualTo("@room")
    }

    private fun aTextPillificationHelper(
        permalinkparser: PermalinkParser = FakePermalinkParser(),
        permalinkBuilder: FakePermalinkBuilder = FakePermalinkBuilder(),
        mentionSpanProvider: MentionSpanProvider = MentionSpanProvider(permalinkparser),
        roomMemberProfilesCache: RoomMemberProfilesCache = RoomMemberProfilesCache(),
    ) = DefaultTextPillificationHelper(
        mentionSpanProvider = mentionSpanProvider,
        permalinkBuilder = permalinkBuilder,
        permalinkParser = permalinkparser,
        roomMemberProfilesCache = roomMemberProfilesCache,
    )
}
