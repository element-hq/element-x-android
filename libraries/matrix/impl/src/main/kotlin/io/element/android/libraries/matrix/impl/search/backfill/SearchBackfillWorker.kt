/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.search.backfill

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.binding
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.search.SearchBackfillCursor
import io.element.android.libraries.workmanager.api.di.MetroWorkerFactory
import io.element.android.libraries.workmanager.api.di.WorkerKey
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

/**
 * Hosts [SearchBackfillRunner] on WorkManager so the sweep survives the app being backgrounded and
 * resumes under its constraints rather than running whenever the user happens to be looking.
 *
 * The worker owns scheduling concerns only. All loop and budget logic lives in the runner, which is a
 * plain suspend class precisely so it stays testable without Robolectric.
 *
 * A **user-initiated** run ([USER_INITIATED_PARAM]) differs in visibility and appetite, not in what
 * it does: it promotes itself to a dataSync foreground service with a progress notification (so the
 * sweep keeps running and stays visible when the app is backgrounded), uses a larger budget, and
 * posts a completion notification once the queue drains.
 */
@AssistedInject
class SearchBackfillWorker(
    @Assisted params: WorkerParameters,
    @ApplicationContext private val context: Context,
    private val matrixClientProvider: MatrixClientProvider,
    private val featureFlagService: FeatureFlagService,
    private val storeHolder: SearchBackfillStoreHolder,
) : CoroutineWorker(context, params) {
    companion object {
        const val SESSION_ID_PARAM = "session_id"
        const val USER_INITIATED_PARAM = "user_initiated"

        private const val CHANNEL_ID = "message_search_index"
        private const val PROGRESS_NOTIFICATION_ID_BASE = 6480
    }

    private val userInitiated: Boolean = inputData.getBoolean(USER_INITIATED_PARAM, false)

    // Unique work is per session, so two accounts can index concurrently — fixed ids would make
    // their notifications overwrite each other and foreground teardown of one worker would remove
    // the other's notification. Derived from the session, stable across retries of the same work.
    private val progressNotificationId: Int =
        PROGRESS_NOTIFICATION_ID_BASE + 2 * ((inputData.getString(SESSION_ID_PARAM)?.hashCode() ?: 0) and 0x3FFF)
    private val completionNotificationId: Int = progressNotificationId + 1

    override suspend fun doWork(): Result {
        val sessionId = inputData.getString(SESSION_ID_PARAM)?.let(::SessionId) ?: return Result.failure()

        // Re-checked LIVE rather than trusting the value frozen at enqueue time: a user who turned
        // message search off should stop paying for history downloads at the next execution.
        if (!featureFlagService.isFeatureEnabled(FeatureFlags.MessageSearch)) {
            Timber.tag("SearchBackfill").d("Message search disabled, skipping sweep")
            return Result.success()
        }

        val client = matrixClientProvider.getOrRestore(sessionId).getOrNull() ?: return Result.failure()

        // The index only exists when it was attached at client-build time; without it, paginating
        // would spend network on history that nothing is going to index.
        if (!client.isMessageSearchAvailable) {
            Timber.tag("SearchBackfill").d("No search index for this session, skipping sweep")
            return Result.success()
        }

        // Shared with the developer-settings progress UI — never construct a store here, a second
        // DataStore on the same file throws.
        val store = storeHolder.storeFor(sessionId)

        // As a foreground service the sweep escapes WorkManager's ~10 minute background limit and
        // shows live progress in the shade. Promotion can be refused (e.g. this execution started
        // while the app was backgrounded, after a retry or a constraint delay, on API 31+).
        val isForeground = userInitiated &&
            runCatchingExceptions { setForeground(createForegroundInfo(progressNotification(cursor = null))) }
                .onFailure { Timber.tag("SearchBackfill").w(it, "Could not promote sweep to foreground") }
                .isSuccess

        val runner = SearchBackfillRunner(
            client = client,
            store = store,
            roomsProvider = { client.roomQueue() },
            // The generous budget is only safe under a foreground service; an unpromoted execution
            // is ordinary background work that WorkManager kills at ~10 minutes, so it must keep
            // the conservative budget and let Result.retry() cover the remainder.
            budget = if (isForeground) userInitiatedBudget() else SearchBackfillBudget(),
        )

        return coroutineScope {
            val progressUpdates = if (userInitiated) {
                launch {
                    store.cursorFlow().filterNotNull().collect { cursor -> updateProgressNotification(cursor) }
                }
            } else {
                null
            }

            val result = runCatchingExceptions { runner.runOnce() }
            progressUpdates?.cancel()

            result.fold(
                onSuccess = { cursor ->
                    Timber.tag("SearchBackfill").d(
                        "Sweep execution done: %d/%d rooms, %d pages",
                        cursor.index,
                        cursor.queue.size,
                        cursor.pagesIssued,
                    )
                    if (userInitiated && cursor.isDrained && cursor.queue.isNotEmpty()) {
                        // Posted as its own notification, not an update of the progress one: the
                        // foreground notification is torn down with the worker, and the user who
                        // backgrounded the app must still find out the sweep finished.
                        postCompletionNotification(cursor)
                    }
                    // More rooms left — or a queue that came up empty because the room list had
                    // not synced yet — means another execution is wanted, not that this one failed.
                    // Exception: on a user-initiated run the app is open and the room list synced
                    // (and the joined-rooms fallback already ran), so an empty queue is the
                    // authoritative "nothing to index", not a transient state — retrying it would
                    // keep the sweep alive forever with nothing to do.
                    when {
                        userInitiated && cursor.queue.isEmpty() -> Result.success()
                        cursor.needsAnotherExecution -> Result.retry()
                        else -> Result.success()
                    }
                },
                onFailure = { error ->
                    Timber.tag("SearchBackfill").w(error, "Sweep execution failed")
                    Result.retry()
                },
            )
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo(progressNotification(cursor = null))
    }

    /**
     * The user pressed the button and is watching: allowed to go long and fast. Still bounded —
     * a runaway sweep must end even when nobody cancels it; `Result.retry()` covers the remainder.
     */
    private fun userInitiatedBudget() = SearchBackfillBudget(
        maxPagesPerExecution = 1000,
        executionDeadline = 30.minutes,
        delayBetweenPages = 100.milliseconds,
        delayBetweenRooms = 250.milliseconds,
    )

    /**
     * Room summaries come from the sliding-sync room list, which needs a running sync — and this
     * worker is headless, so it may never populate. We wait briefly and fall back to the flat joined
     * room list rather than sweeping nothing.
     *
     * The fallback is logged at WARN on purpose: it is unordered, so "the most recent rooms first"
     * silently becomes "an arbitrary 200 rooms". If that branch turns out to be the one production
     * always takes, the prioritisation story is fiction and the log is how anyone would find out.
     */
    private suspend fun MatrixClient.roomQueue(): List<RoomId> {
        val summaries = withTimeoutOrNull(ROOM_LIST_TIMEOUT_MILLIS) {
            roomListService.allRooms.summaries.firstOrNull { it.isNotEmpty() }
        }
        if (!summaries.isNullOrEmpty()) {
            return planSearchBackfill(summaries)
        }
        Timber.tag("SearchBackfill").w("Room list unavailable headless; falling back to unordered joined rooms")
        return getJoinedRoomIds().getOrNull().orEmpty().take(ROOM_QUEUE_LIMIT)
    }

    // Notification strings are deliberately hardcoded: the whole surface is behind a developer
    // feature flag, matching the hardcoded developer-settings screen that launches it. They move
    // to translated resources if this ever ships to real users.

    private fun createForegroundInfo(notification: Notification): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(progressNotificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(progressNotificationId, notification)
        }
    }

    private fun updateProgressNotification(cursor: SearchBackfillCursor) {
        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) return
        @Suppress("MissingPermission")
        notificationManager.notify(progressNotificationId, progressNotification(cursor))
    }

    private fun postCompletionNotification(cursor: SearchBackfillCursor) {
        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) return
        val notification = notificationBuilder()
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Message indexing finished")
            // "Swept", not "indexed": history already sitting in the local event cache is skipped
            // upstream, so an event count or a completeness claim would be fabricated.
            .setContentText("${cursor.queue.size} rooms swept, ${cursor.pagesIssued} pages of history fetched")
            .setAutoCancel(true)
            .build()
        @Suppress("MissingPermission")
        notificationManager.notify(completionNotificationId, notification)
    }

    private fun progressNotification(cursor: SearchBackfillCursor?): Notification {
        val total = cursor?.queue?.size ?: 0
        val done = (cursor?.index ?: 0).coerceAtMost(total)
        return notificationBuilder()
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("Indexing messages")
            .setContentText(if (total == 0) "Preparing…" else "Room $done of $total")
            .setProgress(total, done, total == 0)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun notificationBuilder(): NotificationCompat.Builder {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName("Message indexing")
                .setDescription("Progress of the local message search index")
                .build()
        )
        val contentIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.let { intent ->
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentIntent(contentIntent)
    }

    @ContributesIntoMap(AppScope::class, binding = binding<MetroWorkerFactory.WorkerInstanceFactory<*>>())
    @WorkerKey(SearchBackfillWorker::class)
    @AssistedFactory
    interface Factory : MetroWorkerFactory.WorkerInstanceFactory<SearchBackfillWorker>
}

private const val ROOM_LIST_TIMEOUT_MILLIS = 30_000L
