/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.messages.impl.timeline

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.utils.FakeMentionSpanFormatter
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.libraries.textcomposer.mentions.MentionSpanProvider
import io.element.android.libraries.textcomposer.mentions.MentionSpanTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DefaultHtmlConverterProviderTest {
    private val provider = DefaultHtmlConverterProvider(
        mentionSpanProvider = MentionSpanProvider(
            permalinkParser = FakePermalinkParser(),
            mentionSpanFormatter = FakeMentionSpanFormatter(),
            mentionSpanTheme = MentionSpanTheme(A_USER_ID)
        )
    )

    @Test
    fun `calling provide without calling Update first should throw an exception`() {
        val exception = runCatchingExceptions { provider.provide() }.exceptionOrNull()

        assertThat(exception).isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `calling provide after calling Update first should return an HtmlConverter`() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                provider.Update()
            }
        }
        val htmlConverter = runCatchingExceptions { provider.provide() }.getOrNull()

        assertThat(htmlConverter).isNotNull()
    }
}
