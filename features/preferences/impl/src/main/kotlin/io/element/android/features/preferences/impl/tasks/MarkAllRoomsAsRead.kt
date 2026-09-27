/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.tasks

import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.push.api.notifications.NotificationCleaner
import kotlinx.coroutines.withContext

interface MarkAllRoomsAsRead {
    suspend operator fun invoke(): Result<Unit>
}

@ContributesBinding(SessionScope::class)
class DefaultMarkAllRoomsAsRead(
    private val client: MatrixClient,
    private val notificationCleaner: NotificationCleaner,
    private val coroutineDispatchers: CoroutineDispatchers,
) : MarkAllRoomsAsRead {
    override suspend fun invoke(): Result<Unit> = withContext(coroutineDispatchers.io) {
        client.markAllRoomsAsRead()
            .onSuccess {
                notificationCleaner.clearAllMessagesEvents(client.sessionId)
            }
    }
}
