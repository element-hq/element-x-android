/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.createComposeRule
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.libraries.textcomposer.mentions.MentionSpanProvider
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DefaultHtmlConverterProviderTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun `calling provide without calling Update first should throw an exception`() {
        val provider = DefaultHtmlConverterProvider(mentionSpanProvider = MentionSpanProvider(FakePermalinkParser()))

        val exception = runCatching { provider.provide() }.exceptionOrNull()

        assertThat(exception).isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `calling provide after calling Update first should return an HtmlConverter`() {
        val provider = DefaultHtmlConverterProvider(mentionSpanProvider = MentionSpanProvider(FakePermalinkParser()))
        composeTestRule.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                provider.Update(currentUserId = A_USER_ID)
            }
        }

        val htmlConverter = runCatching { provider.provide() }.getOrNull()

        assertThat(htmlConverter).isNotNull()
    }
}
