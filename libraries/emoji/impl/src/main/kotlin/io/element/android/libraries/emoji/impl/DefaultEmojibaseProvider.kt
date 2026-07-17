/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.emoji.impl

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.emojibasebindings.EmojibaseDatasource
import io.element.android.emojibasebindings.EmojibaseStore
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.emoji.api.EmojibaseProvider

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultEmojibaseProvider(@ApplicationContext val context: Context) : EmojibaseProvider {
    override val emojibaseStore: EmojibaseStore by lazy {
        EmojibaseDatasource().load(context)
    }
}
