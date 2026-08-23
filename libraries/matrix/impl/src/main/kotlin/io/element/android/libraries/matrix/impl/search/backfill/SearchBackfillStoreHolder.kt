/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.search.backfill

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.search.SearchBackfillStore
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.ConcurrentHashMap

/**
 * Hands out the one [SearchBackfillStore] per session.
 *
 * Being the single construction point is the entire job of this class: DataStore throws as soon as
 * two instances are opened on the same file, and both the sweep worker and the developer-settings
 * progress UI need this store — often at the same time. Nobody else may call
 * [DataStoreSearchBackfillStore]'s constructor.
 *
 * Stores live on the app scope, not the session scope, because the worker can run headless before
 * any session-scoped graph exists. Entries are never evicted; a store is a handle on one small
 * preferences file and a logged-out session simply stops being asked for.
 */
@SingleIn(AppScope::class)
@Inject
class SearchBackfillStoreHolder(
    @ApplicationContext private val context: Context,
    @AppCoroutineScope private val appCoroutineScope: CoroutineScope,
) {
    private val stores = ConcurrentHashMap<SessionId, SearchBackfillStore>()

    fun storeFor(sessionId: SessionId): SearchBackfillStore {
        return stores.getOrPut(sessionId) {
            DataStoreSearchBackfillStore(
                context = context,
                sessionId = sessionId,
                sessionCoroutineScope = appCoroutineScope,
            )
        }
    }
}
