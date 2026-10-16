/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.search.workmanager

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.binding
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.search.SearchBackfillStrategy
import io.element.android.libraries.workmanager.api.di.MetroWorkerFactory
import io.element.android.libraries.workmanager.api.di.WorkerKey
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@AssistedInject
class SearchBackfillWorker(
    @Assisted params: WorkerParameters,
    @ApplicationContext context: Context,
    private val matrixClientProvider: MatrixClientProvider,
    private val processLifecycleOwner: LifecycleOwner,
) : CoroutineWorker(context, params) {
    companion object {
        const val SESSION_ID_PARAM = "session_id"
        private val TIMEOUT = 10.minutes
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        internal val POLL_INTERVAL = 5.seconds
    }

    override suspend fun doWork(): Result {
        val appLifecycle = processLifecycleOwner.lifecycle
        if (appLifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            Timber.w("App is in foreground, skipping search backfill as it would degrade performance")
            return Result.retry()
        }

        Timber.d("Starting search backfill. This will run up to $TIMEOUT")
        val sessionId = inputData.getString(SESSION_ID_PARAM)?.let(::SessionId) ?: return Result.failure()
        val client = matrixClientProvider.getOrRestore(sessionId).getOrNull() ?: return Result.failure()

        if (client.searchBackfillService.isSearchBackfillRunning()) {
            Timber.d("Search backfill is already running for session $sessionId, nothing to do")
            return Result.success()
        }

        Timber.d("Starting search backfill for session $sessionId")
        val task = client.searchBackfillService.startSearchBackfill(SearchBackfillStrategy.BACKGROUND).getOrElse {
            Timber.e(it, "Failed to start search backfill")
            return Result.failure()
        }

        return task.use {
            try {
                withTimeout(TIMEOUT) {
                    while (task.isRunning()) {
                        delay(POLL_INTERVAL)

                        if (task.isRunning() && appLifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                            Timber.w("App is now in foreground, cancelling search backfill as it would degrade performance")
                            return@withTimeout
                        }
                    }
                }
                Timber.d("Search backfill finished")
                Result.success()
            } catch (_: TimeoutCancellationException) {
                Timber.d("Search backfill timed out after $TIMEOUT, cancelling it")
                // Run again later to continue backfilling.
                Result.retry()
            }
        }
    }

    @ContributesIntoMap(AppScope::class, binding = binding<MetroWorkerFactory.WorkerInstanceFactory<*>>())
    @WorkerKey(SearchBackfillWorker::class)
    @AssistedFactory
    interface Factory : MetroWorkerFactory.WorkerInstanceFactory<SearchBackfillWorker>
}
