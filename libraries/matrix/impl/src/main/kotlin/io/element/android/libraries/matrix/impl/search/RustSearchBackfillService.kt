/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.search

import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.SdkPendingTask
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.search.SearchBackfillService
import io.element.android.libraries.matrix.api.search.SearchBackfillStrategy
import io.element.android.libraries.matrix.impl.RustSdkPendingTask
import io.element.android.libraries.matrix.impl.workmanager.SearchBackfillRequestBuilder
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.WorkManagerScheduler
import org.matrix.rustcomponents.sdk.Client
import timber.log.Timber
import java.util.concurrent.atomic.AtomicReference

@AssistedInject
class RustSearchBackfillService(
    @Assisted private val innerClient: Client,
    private val searchBackfillRequestBuilderFactory: SearchBackfillRequestBuilder.Factory,
    private val workManagerScheduler: WorkManagerScheduler,
) : SearchBackfillService {
    @AssistedFactory
    interface Factory {
        fun create(innerClient: Client): RustSearchBackfillService
    }

    private val currentSearchBackfillTaskHandle: AtomicReference<SdkPendingTask?> = AtomicReference(null)

    override suspend fun schedulePeriodicSearchBackfill(strategy: SearchBackfillStrategy): Result<Unit> = runCatchingExceptions {
        val sessionId = SessionId(innerClient.userId())
        if (workManagerScheduler.hasPendingWork(sessionId, WorkManagerRequestType.SEARCH_BACKFILL)) {
            Timber.d("Background search backfill already scheduled for session $sessionId")
            return@runCatchingExceptions
        }
        Timber.d("Scheduling background search backfill for session $sessionId")
        val builder = searchBackfillRequestBuilderFactory.create(sessionId)
        workManagerScheduler.submit(builder)
    }

    override fun cancelPeriodicSearchBackfill() {
        val sessionId = SessionId(innerClient.userId())
        Timber.d("Cancelling background search backfill for session $sessionId")
        workManagerScheduler.cancel(sessionId, WorkManagerRequestType.SEARCH_BACKFILL)
    }

    override fun startSearchBackfill(strategy: SearchBackfillStrategy): Result<SdkPendingTask> = runCatchingExceptions {
        if (currentSearchBackfillTaskHandle.get()?.isRunning() == true) {
            error("Search backfill is already running")
        }
        RustSdkPendingTask(innerClient.runSearchBackfill(strategy.map()))
            .also { currentSearchBackfillTaskHandle.set(it) }
    }

    override fun isSearchBackfillRunning(): Boolean {
        return currentSearchBackfillTaskHandle.get()?.isRunning() ?: false
    }

    override fun cancelSearchBackfill(): Result<Unit> = runCatchingExceptions {
        currentSearchBackfillTaskHandle.getAndSet(null)?.close()
    }
}
