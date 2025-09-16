/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.recentemojis

import dev.zacsweers.metro.Inject
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.MatrixClient
import kotlinx.coroutines.withContext

@Inject
class AddRecentEmoji(
    private val client: MatrixClient,
    private val dispatchers: CoroutineDispatchers,
) {
    suspend operator fun invoke(emoji: String): Result<Unit> = withContext(dispatchers.io) {
        client.addRecentEmoji(emoji)
    }
}
