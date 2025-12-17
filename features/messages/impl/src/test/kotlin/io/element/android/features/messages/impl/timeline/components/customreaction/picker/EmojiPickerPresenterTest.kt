/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.customreaction.picker

import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.emojibasebindings.Emoji
import io.element.android.emojibasebindings.EmojibaseCategory
import io.element.android.emojibasebindings.EmojibaseStore
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EmojiPickerPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `UpdateSearchQuery loads new results`() = runTest {
        testPresenter {
            skipItems(1)

            val initialState = awaitItem()
            assertThat(initialState.searchQuery).isEmpty()
            assertThat(initialState.searchResults).isInstanceOf(SearchBarResultState.Initial::class.java)

            initialState.eventSink(EmojiPickerEvents.UpdateSearchQuery("smile"))
            assertThat(awaitItem().searchQuery).isEqualTo("smile")

            val stateWithResults = awaitItem()
            assertThat(stateWithResults.searchQuery).isEqualTo("smile")
            assertThat(stateWithResults.searchResults).isInstanceOf(SearchBarResultState.Results::class.java)
        }
    }

    @Test
    fun `ToggleSearchActive toggles the search state`() = runTest {
        testPresenter {
            skipItems(1)

            val initialState = awaitItem()
            assertThat(initialState.isSearchActive).isFalse()

            initialState.eventSink(EmojiPickerEvents.ToggleSearchActive(true))
            assertThat(awaitItem().isSearchActive).isTrue()

            initialState.eventSink(EmojiPickerEvents.ToggleSearchActive(false))
            assertThat(awaitItem().isSearchActive).isFalse()
        }
    }

    @Test
    fun `recent emojis are automatically added to the categories if present`() = runTest {
        val providedCategories = persistentListOf(emojiCategory(EmojibaseCategory.Activity))
        val presenter = createPresenter(
            categories = providedCategories,
            recentEmojis = persistentListOf("😊"),
        )
        testPresenter(presenter) {
            skipItems(1)

            val initialState = awaitItem()
            assertThat(providedCategories.size).isNotEqualTo(initialState.categories.size)
            assertThat(initialState.categories.size).isEqualTo(2)
        }
    }

    private fun TestScope.createPresenter(
        categories: ImmutableList<Pair<EmojibaseCategory, ImmutableList<Emoji>>> = persistentListOf(emojiCategory()),
        recentEmojis: ImmutableList<String> = persistentListOf(),
    ) = EmojiPickerPresenter(
        emojibaseStore = EmojibaseStore(categories.toMap().toImmutableMap()),
        recentEmojis = recentEmojis,
        coroutineDispatchers = testCoroutineDispatchers(),
    )

    private fun emojiCategory(
        category: EmojibaseCategory = EmojibaseCategory.Activity,
        emojis: ImmutableList<Emoji> = persistentListOf(
            Emoji("1F3C3", "Smile", persistentListOf("smile"), persistentListOf("smile"), "😊", skins = null)
        )
    ) = category to emojis

    @OptIn(InternalComposeApi::class)
    private suspend fun TestScope.testPresenter(
        presenter: EmojiPickerPresenter = createPresenter(),
        testBlock: suspend TurbineTestContext<EmojiPickerState>.() -> Unit,
    ) {
        moleculeFlow(RecompositionMode.Immediate) {
            // These are needed to load the history icon in the presenter
            currentComposer.startProviders(arrayOf(
                LocalContext provides InstrumentationRegistry.getInstrumentation().context,
                LocalConfiguration provides InstrumentationRegistry.getInstrumentation().context.resources.configuration,
            ))
            val state = presenter.present()
            currentComposer.endProviders()
            state
        }.test {
            testBlock()
        }
    }
}
