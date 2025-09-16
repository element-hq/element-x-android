/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.recentemojis

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import kotlinx.coroutines.withContext

fun interface GetRecentEmojis {
    suspend operator fun invoke(): Result<List<String>>
}

@ContributesBinding(SessionScope::class)
@Inject
class DefaultGetRecentEmojis(
    private val client: MatrixClient,
    private val dispatchers: CoroutineDispatchers,
) : GetRecentEmojis {
    override suspend operator fun invoke(): Result<List<String>> = withContext(dispatchers.io) {
        client.getRecentEmojis()
    }
}
