/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.recentemojis.impl

import android.content.Context
import io.element.android.emojibasebindings.EmojibaseDatasource
import io.element.android.emojibasebindings.EmojibaseStore
import io.element.android.libraries.recentemojis.api.EmojibaseProvider

class DefaultEmojibaseProvider(val context: Context) : EmojibaseProvider {
    override val emojibaseStore: EmojibaseStore by lazy {
        EmojibaseDatasource().load(context)
    }
}
