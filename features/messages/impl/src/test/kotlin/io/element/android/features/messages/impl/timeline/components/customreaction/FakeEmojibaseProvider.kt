/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.customreaction

import io.element.android.emojibasebindings.EmojibaseStore

class FakeEmojibaseProvider : EmojibaseProvider {
    override val emojibaseStore: EmojibaseStore
        get() = EmojibaseStore(mapOf())
}
