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
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.di.annotations.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultEmojibaseProvider(
    @ApplicationContext private val context: Context,
    dispatchers: CoroutineDispatchers,
    @AppCoroutineScope appScope: CoroutineScope,
) : EmojibaseProvider {
    private val deferred = appScope.async(dispatchers.io, start = CoroutineStart.LAZY) {
        EmojibaseDatasource().load(context)
    }

    override suspend fun getStore(): EmojibaseStore = deferred.await()
}
