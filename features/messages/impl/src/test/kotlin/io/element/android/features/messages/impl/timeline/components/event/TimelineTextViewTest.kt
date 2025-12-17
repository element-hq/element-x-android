/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import android.text.SpannableString
import android.text.SpannedString
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextContent
import io.element.android.features.messages.impl.utils.FakeMentionSpanFormatter
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.ui.messages.RoomMemberProfilesCache
import io.element.android.libraries.matrix.ui.messages.RoomNamesCache
import io.element.android.libraries.textcomposer.mentions.DefaultMentionSpanUpdater
import io.element.android.libraries.textcomposer.mentions.LocalMentionSpanUpdater
import io.element.android.libraries.textcomposer.mentions.MentionSpan
import io.element.android.libraries.textcomposer.mentions.MentionSpanTheme
import io.element.android.libraries.textcomposer.mentions.MentionSpanUpdater
import io.element.android.libraries.textcomposer.mentions.MentionType
import io.element.android.libraries.textcomposer.mentions.getMentionSpans
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.wysiwyg.view.spans.CustomMentionSpan
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimelineTextViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    private val mentionSpanTheme = MentionSpanTheme(currentUserId = A_USER_ID)
    private val formatLambda = lambdaRecorder<MentionType, CharSequence> { mentionType -> mentionType.toString() }
    private val mentionSpanFormatter = FakeMentionSpanFormatter(formatLambda)

    @Test
    fun `getTextWithResolvedMentions - does nothing for a non spannable CharSequence`() = runTest {
        val charSequence = "Hello <a href=\"https://matrix.to/#/@alice:example.com\">@alice:example.com</a>"
        val mentionSpanUpdater = aMentionSpanUpdater()
        val result = rule.getText(mentionSpanUpdater, aTextContentWithFormattedBody(charSequence))

        assertThat(result.getMentionSpans()).isEmpty()
        assert(formatLambda).isNeverCalled()
    }

    @Test
    fun `getTextWithResolvedMentions - does nothing if there are no mentions`() = runTest {
        val charSequence = SpannableString("Hello <a href=\"https://matrix.to/#/@alice:example.com\">@alice:example.com</a>")
        val mentionSpanUpdater = aMentionSpanUpdater()
        val result = rule.getText(mentionSpanUpdater, aTextContentWithFormattedBody(charSequence))

        assertThat(result.getMentionSpans()).isEmpty()
        assert(formatLambda).isNeverCalled()
    }

    @Test
    fun `getTextWithResolvedMentions - just returns the body if there is no formattedBody`() = runTest {
        val charSequence = "Hello <a href=\"https://matrix.to/#/@alice:example.com\">@alice:example.com</a>"
        val mentionSpanUpdater = aMentionSpanUpdater()
        val result = rule.getText(mentionSpanUpdater, aTextContentWithFormattedBody(body = charSequence, formattedBody = null))

        assertThat(result.getMentionSpans()).isEmpty()
        assertThat(result.toString()).isEqualTo(charSequence)
        assert(formatLambda).isNeverCalled()
    }

    @Test
    fun `getTextWithResolvedMentions - with Room mention format correctly`() = runTest {
        val mentionType = MentionType.Room(roomIdOrAlias = A_ROOM_ID_2.toRoomIdOrAlias())
        val charSequence = buildSpannedString {
            append("Hello ")
            inSpans(MentionSpan(mentionType)) {
                append(A_ROOM_ID.value)
            }
        }
        val mentionSpanUpdater = aMentionSpanUpdater()
        val result = rule.getText(mentionSpanUpdater, aTextContentWithFormattedBody(charSequence))

        val expectedDisplayText = mentionType.toString()
        assertThat(result.getMentionSpans().firstOrNull()?.displayText.toString()).isEqualTo(expectedDisplayText)
        assertThat(result).isEqualTo(charSequence)
        assert(formatLambda).isCalledOnce()
    }

    @Test
    fun `getTextWithResolvedMentions - replaces MentionSpan's text`() = runTest {
        val mentionType = MentionType.User(userId = A_USER_ID)
        val charSequence = buildSpannedString {
            append("Hello ")
            inSpans(MentionSpan(mentionType)) {
                append("@NotAlice")
            }
        }
        val mentionSpanUpdater = aMentionSpanUpdater()
        val result = rule.getText(mentionSpanUpdater, aTextContentWithFormattedBody(charSequence))

        val expectedDisplayText = mentionType.toString()
        assertThat(result.getMentionSpans().firstOrNull()?.displayText.toString()).isEqualTo(expectedDisplayText)
        assert(formatLambda).isCalledOnce()
    }

    @Test
    fun `getTextWithResolvedMentions - replaces MentionSpan's text inside CustomMentionSpan`() = runTest {
        val mentionType = MentionType.User(userId = A_USER_ID)
        val charSequence = buildSpannedString {
            append("Hello ")
            inSpans(CustomMentionSpan(MentionSpan(mentionType))) {
                append("@NotAlice")
            }
        }
        val mentionSpanUpdater = aMentionSpanUpdater()
        val expectedDisplayText = mentionType.toString()
        val result = rule.getText(mentionSpanUpdater, aTextContentWithFormattedBody(charSequence))
        assertThat(result.getMentionSpans().firstOrNull()?.displayText.toString()).isEqualTo(expectedDisplayText)
        assert(formatLambda).isCalledOnce()
    }

    private suspend fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.getText(
        mentionSpanUpdater: MentionSpanUpdater,
        content: TimelineItemTextBasedContent,
    ): CharSequence {
        val completable = CompletableDeferred<CharSequence>()
        setContent {
            CompositionLocalProvider(
                LocalMentionSpanUpdater provides mentionSpanUpdater
            ) {
                completable.complete(getTextWithResolvedMentions(content = content))
            }
        }
        return completable.await()
    }

    private fun aMentionSpanUpdater(): MentionSpanUpdater {
        return DefaultMentionSpanUpdater(
            formatter = mentionSpanFormatter,
            theme = mentionSpanTheme,
            roomMemberProfilesCache = RoomMemberProfilesCache(),
            roomNamesCache = RoomNamesCache(),
        )
    }

    private fun aTextContentWithFormattedBody(formattedBody: CharSequence?, body: String = "") =
        TimelineItemTextContent(
            body = body,
            htmlDocument = null,
            formattedBody = formattedBody ?: SpannedString(body),
            isEdited = false
        )
}
