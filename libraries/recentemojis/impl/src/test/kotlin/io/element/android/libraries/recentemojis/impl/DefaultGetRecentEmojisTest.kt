/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.recentemojis.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.emojibasebindings.Emoji
import io.element.android.emojibasebindings.EmojibaseCategory
import io.element.android.emojibasebindings.EmojibaseCategory.People
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.recentemojis.test.FakeEmojibaseProvider
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultGetRecentEmojisTest {
    @Test
    fun `invoke - deduplicates results`() = runTest {
        val recentEmojiResult = persistentListOf(":)", ":D", ":)")
        val getRecentEmojis = createDefaultGetRecentEmojis(
            recentEmojis = { Result.success(recentEmojiResult) },
            emojibaseContents = persistentMapOf(People to recentEmojiResult.map { emoji(it) }.toImmutableList())
        )

        assertThat(getRecentEmojis()).isEqualTo(Result.success(persistentListOf(":)", ":D")))
    }

    @Test
    fun `invoke - removes non-standard emojis`() = runTest {
        val recentEmojiResult = persistentListOf(":)", ":D", "Custom reaction")
        val getRecentEmojis = createDefaultGetRecentEmojis(
            recentEmojis = { Result.success(recentEmojiResult) },
            emojibaseContents = persistentMapOf(
                People to persistentListOf(emoji(":)"), emoji(":D"))
            )
        )

        assertThat(getRecentEmojis()).isEqualTo(Result.success(persistentListOf(":)", ":D")))
    }

    private fun emoji(unicode: String) = Emoji(
        hexcode = "",
        label = "",
        tags = null,
        shortcodes = persistentListOf(),
        unicode = unicode,
        skins = null,
    )

    private fun TestScope.createDefaultGetRecentEmojis(
        recentEmojis: () -> Result<List<String>> = { Result.success(emptyList()) },
        emojibaseContents: ImmutableMap<EmojibaseCategory, ImmutableList<Emoji>> = persistentMapOf(People to persistentListOf(emoji(":)"))),
    ) = DefaultGetRecentEmojis(
        client = FakeMatrixClient(getRecentEmojisLambda = recentEmojis),
        dispatchers = testCoroutineDispatchers(),
        emojibaseProvider = FakeEmojibaseProvider(emojibaseContents),
    )
}
