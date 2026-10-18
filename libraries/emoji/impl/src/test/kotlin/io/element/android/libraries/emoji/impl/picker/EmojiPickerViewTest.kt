/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.libraries.emoji.impl.picker

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.unit.dp
import io.element.android.emojibasebindings.Emoji
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.tests.testutils.robolectric.RobolectricTest
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import org.junit.Test
import org.robolectric.annotation.Config

private val PICKER_HEIGHT = 600.dp

class EmojiPickerViewTest : RobolectricTest() {
    @Config(qualifiers = "w400dp-h800dp")
    @Test
    fun `the search results reach the bottom of the picker`() = runAndroidComposeUiTest<ComponentActivity> {
        setEmojiPickerView()
        onNode(hasScrollToIndexAction())
            .getBoundsInRoot()
            .bottom
            .assertIsEqualTo(PICKER_HEIGHT, "emoji grid bottom")
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setEmojiPickerView() {
    setContent {
        Box(Modifier.size(400.dp, PICKER_HEIGHT)) {
            EmojiPickerView(
                state = aDefaultEmojiPickerState(
                    isSearchActive = true,
                    searchQuery = "smile",
                    searchResults = SearchBarResultState.Results(
                        persistentListOf(
                            Emoji(
                                hexcode = "0x00",
                                label = "grinning face",
                                tags = persistentListOf("grinning"),
                                shortcodes = persistentListOf("smile"),
                                unicode = "😀",
                                skins = null,
                            ),
                        )
                    ),
                ),
                onSelectEmoji = {},
                selectedEmojis = persistentSetOf(),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
