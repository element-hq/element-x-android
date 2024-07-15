/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
