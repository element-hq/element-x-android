/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.recentemojis.impl

import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.recentemojis.api.EmojibaseProvider
import io.element.android.libraries.recentemojis.api.GetRecentEmojis
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.withContext

@ContributesBinding(SessionScope::class)
class DefaultGetRecentEmojis(
    private val client: MatrixClient,
    private val dispatchers: CoroutineDispatchers,
    private val emojibaseProvider: EmojibaseProvider,
) : GetRecentEmojis {
    override suspend operator fun invoke(): Result<ImmutableList<String>> = withContext(dispatchers.io) {
        val allEmojis = emojibaseProvider.emojibaseStore.allEmojis
        client.getRecentEmojis()
            .map { emojis ->
                // Remove any possible duplicates
                emojis.distinct()
                    // Return only those emojis that are valid
                    .filter { recent -> allEmojis.any { recent == it.unicode } }
                    .toImmutableList()
            }
    }
}
