/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.emoji.impl.picker

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import app.cash.turbine.TurbineTestContext
import com.google.common.truth.Truth.assertThat
import io.element.android.emojibasebindings.Emoji
import io.element.android.emojibasebindings.EmojibaseCategory
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.emoji.api.picker.EmojiPickerState
import io.element.android.libraries.emoji.api.recentemojis.GetRecentEmojis
import io.element.android.libraries.emoji.impl.fixtures.FakeEmojibaseProvider
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.robolectric.RobolectricTest
import io.element.android.tests.testutils.test
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class EmojiPickerPresenterTest : RobolectricTest() {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `updating search query loads new results`() = runTest {
        createPresenter().test {
            val initialState = awaitStoreLoaded()
            assertThat(initialState.searchQuery.text.toString()).isEmpty()
            assertThat(initialState.searchResults).isInstanceOf(SearchBarResultState.Initial::class.java)

            initialState.searchQuery.setTextAndPlaceCursorAtEnd("smile")
            val stateAfterQuery = awaitItem() as DefaultEmojiPickerState
            assertThat(stateAfterQuery.searchQuery.text.toString()).isEqualTo("smile")

            val stateWithResults = awaitItem() as DefaultEmojiPickerState
            assertThat(stateWithResults.searchQuery.text.toString()).isEqualTo("smile")
            assertThat(stateWithResults.searchResults).isInstanceOf(SearchBarResultState.Results::class.java)
        }
    }

    @Test
    fun `recent emojis are prepended as a category when the source returns any`() = runTest {
        createPresenter(
            categories = persistentListOf(emojiCategory(EmojibaseCategory.Activity)),
            getRecentEmojis = GetRecentEmojis { Result.success(persistentListOf("😊")) },
        ).test {
            val state = awaitStoreLoaded()
            assertThat(state.categories.size).isEqualTo(2)
        }
    }

    @Test
    fun `ToggleSearchActive toggles the search state`() = runTest {
        createPresenter().test {
            val initialState = awaitStoreLoaded()
            assertThat(initialState.isSearchActive).isFalse()

            initialState.eventSink(EmojiPickerEvent.ToggleSearchActive(true))
            assertThat((awaitItem() as DefaultEmojiPickerState).isSearchActive).isTrue()

            initialState.eventSink(EmojiPickerEvent.ToggleSearchActive(false))
            assertThat((awaitItem() as DefaultEmojiPickerState).isSearchActive).isFalse()
        }
    }

    @Test
    fun `presenter exposes categories from the emoji store when the recent source is empty`() = runTest {
        val providedCategories = persistentListOf(
            emojiCategory(EmojibaseCategory.Activity),
            emojiCategory(EmojibaseCategory.People),
        )
        createPresenter(categories = providedCategories).test {
            val state = awaitStoreLoaded()
            assertThat(state.categories.size).isEqualTo(providedCategories.size)
        }
    }

    private suspend fun TurbineTestContext<EmojiPickerState>.awaitStoreLoaded(): DefaultEmojiPickerState {
        var state = awaitItem() as DefaultEmojiPickerState
        while (state.categories.isEmpty()) {
            state = awaitItem() as DefaultEmojiPickerState
        }
        return state
    }

    private fun TestScope.createPresenter(
        categories: ImmutableList<Pair<EmojibaseCategory, ImmutableList<Emoji>>> = persistentListOf(emojiCategory()),
        getRecentEmojis: GetRecentEmojis = GetRecentEmojis { Result.success(persistentListOf()) },
    ) = DefaultEmojiPickerPresenter(
        emojibaseProvider = FakeEmojibaseProvider(emojis = categories.toMap()),
        getRecentEmojis = getRecentEmojis,
        coroutineDispatchers = testCoroutineDispatchers(),
    )

    private fun emojiCategory(
        category: EmojibaseCategory = EmojibaseCategory.Activity,
        emojis: ImmutableList<Emoji> = persistentListOf(
            Emoji("1F3C3", "Smile", persistentListOf("smile"), persistentListOf("smile"), "😊", skins = null)
        )
    ) = category to emojis
}
