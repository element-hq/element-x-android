/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.emoji.impl.fixtures

import io.element.android.emojibasebindings.Emoji
import io.element.android.emojibasebindings.EmojibaseCategory
import io.element.android.emojibasebindings.EmojibaseStore
import io.element.android.libraries.emoji.impl.EmojibaseProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentMap

internal class FakeEmojibaseProvider(
    emojis: Map<EmojibaseCategory, ImmutableList<Emoji>> = mapOf(),
) : EmojibaseProvider {
    private val store = EmojibaseStore(emojis.toPersistentMap())
    override suspend fun getStore(): EmojibaseStore = store
}
