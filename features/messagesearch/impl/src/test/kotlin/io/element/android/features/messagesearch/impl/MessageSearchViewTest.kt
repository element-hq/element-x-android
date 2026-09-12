/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.messagesearch.impl

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class MessageSearchViewTest : RobolectricTest() {
    @Test
    fun `generic search error is rendered`() = runAndroidComposeUiTest<ComponentActivity> {
        setContent {
            MessageSearchView(
                state = aMessageSearchState(
                    query = "a private search query",
                    hasError = true,
                ),
                onResultClick = {},
                onBackClick = {},
            )
        }

        onNodeWithText(activity!!.getString(CommonStrings.screen_message_search_error)).assertExists()
    }

    @Test
    fun `an exhausted search reports no results`() = runAndroidComposeUiTest<ComponentActivity> {
        setContent {
            MessageSearchView(
                state = aMessageSearchState(
                    query = "a word with no matches",
                    endReached = true,
                ),
                onResultClick = {},
                onBackClick = {},
            )
        }

        onNodeWithText(activity!!.getString(CommonStrings.common_no_results)).assertExists()
    }

    @Test
    fun `a result list whose end is on screen reports it`() = runAndroidComposeUiTest<ComponentActivity> {
        // The View's only say in pagination. It reports what it can see and nothing else — how many
        // pages that is worth is the presenter's decision, because only the presenter can tell when
        // a page has actually landed.
        val reported = mutableListOf<Boolean>()
        val state = aMessageSearchState(
            query = "hello",
            results = aMessageSearchResultItemList(),
            eventSink = { event ->
                if (event is MessageSearchEvents.ListEndVisible) {
                    reported += event.isVisible
                }
            },
        )
        setContent {
            MessageSearchView(
                state = state,
                onResultClick = {},
                onBackClick = {},
            )
        }
        waitForIdle()

        // The whole list fits on screen, so its end is in the viewport.
        assertThat(reported.last()).isTrue()
    }

    @Test
    fun `the spinner is shown only while a page is actually in flight`() = runAndroidComposeUiTest<ComponentActivity> {
        // What the user reported was a spinner that never resolved. It was tied to "more pages
        // exist" rather than to "a page is loading", so it sat under a list that had stopped doing
        // anything and read as a hang.
        val idle = aMessageSearchState(
            query = "hello",
            results = aMessageSearchResultItemList(),
            isPaginating = false,
        )

        assertThat(idle.displayLoadMoreIndicator).isFalse()
        assertThat(idle.copy(isPaginating = true).displayLoadMoreIndicator).isTrue()
        assertThat(idle.copy(isPaginating = true, endReached = true).displayLoadMoreIndicator).isFalse()
    }
}
