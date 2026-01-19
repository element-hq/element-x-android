/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.store

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.store.CustomNotificationChannelsStore
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.ConcurrentHashMap

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultCustomNotificationChannelsStoreFactory(
    @ApplicationContext private val context: Context,
    private val sessionObserver: SessionObserver,
    @AppCoroutineScope private val appCoroutineScope: CoroutineScope,
) : CustomNotificationChannelsStoreFactory {
    // We can have only one class accessing a single data store, so keep a cache of them.
    private val cache = ConcurrentHashMap<SessionId, CustomNotificationChannelsStore>()

    override fun getOrCreate(sessionId: SessionId): CustomNotificationChannelsStore {
        return cache.getOrPut(sessionId) {
            DefaultCustomNotificationChannelsStore(
                context = context,
                sessionId = sessionId,
                sessionCoroutineScope = appCoroutineScope,
                sessionObserver = sessionObserver,
            )
        }
    }
}
