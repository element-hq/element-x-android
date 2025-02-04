/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.customreaction

import android.content.Context
import io.element.android.emojibasebindings.EmojibaseDatasource
import io.element.android.emojibasebindings.EmojibaseStore

class DefaultEmojibaseProvider(val context: Context) : EmojibaseProvider {
    override val emojibaseStore: EmojibaseStore by lazy {
        EmojibaseDatasource().load(context)
    }
}
