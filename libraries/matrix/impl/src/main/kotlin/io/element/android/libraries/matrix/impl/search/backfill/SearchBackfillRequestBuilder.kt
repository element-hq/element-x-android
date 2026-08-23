/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.search.backfill

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.impl.search.backfill.SearchBackfillWorker.Companion.SESSION_ID_PARAM
import io.element.android.libraries.matrix.impl.search.backfill.SearchBackfillWorker.Companion.USER_INITIATED_PARAM
import io.element.android.libraries.workmanager.api.WorkManagerRequestBuilder
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.WorkManagerRequestWrapper
import io.element.android.libraries.workmanager.api.WorkManagerWorkerType
import io.element.android.libraries.workmanager.api.workManagerTag
import java.util.concurrent.TimeUnit

/**
 * Builds the backfill work request in one of two shapes sharing a single unique work name, so at
 * most one sweep per session ever runs:
 *
 * - **Background** (default): opportunistic and free to the user — unmetered network, battery and
 *   storage not low, KEEP policy so an in-flight sweep is never restarted.
 * - **User-initiated** (`userInitiated = true`): the user pressed "Start indexing" and is watching —
 *   any connection will do, REPLACE policy so it takes over a request stuck waiting for the
 *   background constraints, and a marker tag ([WorkManagerRequestType.SEARCH_BACKFILL_USER]) that
 *   lets UI observe this sweep specifically. It still carries the shared SEARCH_BACKFILL tag:
 *   logout cancellation and `hasPendingWork` key on it.
 */
class SearchBackfillRequestBuilder(
    private val sessionId: SessionId,
    private val userInitiated: Boolean = false,
) : WorkManagerRequestBuilder {
    override suspend fun build(): Result<List<WorkManagerRequestWrapper>> {
        val tag = workManagerTag(sessionId, WorkManagerRequestType.SEARCH_BACKFILL)
        // One sweep per session at a time. All of its progress lives in a single stored cursor —
        // two of them running at once would interleave writes to it and pay twice for the same
        // pages. Background KEEPs (a sweep already in flight is strictly more useful than
        // restarting it); user-initiated REPLACEs (a request parked on unmet constraints is
        // strictly less useful than the one the user just asked for).
        val policy = if (userInitiated) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP
        val type = WorkManagerWorkerType.Unique(name = tag, policy = policy)
        val data = Data.Builder()
            .putString(SESSION_ID_PARAM, sessionId.value)
            .putBoolean(USER_INITIATED_PARAM, userInitiated)
            .build()
        val constraints = if (userInitiated) {
            // The user explicitly asked and is watching a progress bar: any connection is fine,
            // but history downloads still make no sense offline.
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        } else {
            Constraints.Builder()
                // Never on cellular. The user did not ask for this download, so it must not
                // arrive on their data plan.
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresBatteryNotLow(true)
                // The index grows as history lands; do not fill a nearly-full device.
                .setRequiresStorageNotLow(true)
                .build()
        }
        val backoff = if (userInitiated) 30L to TimeUnit.SECONDS else 10L to TimeUnit.MINUTES
        val workRequest = OneTimeWorkRequest.Builder(SearchBackfillWorker::class)
            // Load-bearing and unenforced by the compiler: without this tag the sweep keeps
            // downloading a signed-out user's history, because cancel-on-logout works by tag.
            .addTag(tag)
            .apply {
                if (userInitiated) {
                    addTag(workManagerTag(sessionId, WorkManagerRequestType.SEARCH_BACKFILL_USER))
                }
            }
            .setInputData(data)
            .setConstraints(constraints)
            // Deliberately not expedited: even the user-initiated sweep is long-running by nature
            // and shows its own foreground notification instead.
            .setBackoffCriteria(BackoffPolicy.LINEAR, backoff.first, backoff.second)
            .build()

        return Result.success(listOf(WorkManagerRequestWrapper(workRequest, type)))
    }
}
