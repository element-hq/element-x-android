/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.recentemojis.test

import io.element.android.emojibasebindings.Emoji
import io.element.android.emojibasebindings.EmojibaseCategory
import io.element.android.emojibasebindings.EmojibaseStore
import io.element.android.libraries.recentemojis.api.EmojibaseProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentMap

class FakeEmojibaseProvider(
    val emojis: Map<EmojibaseCategory, ImmutableList<Emoji>> = mapOf(),
) : EmojibaseProvider {
    override val emojibaseStore: EmojibaseStore
        get() = EmojibaseStore(emojis.toPersistentMap())
}
