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
import io.element.android.libraries.workmanager.api.WorkManagerRequestBuilder
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.WorkManagerRequestWrapper
import io.element.android.libraries.workmanager.api.WorkManagerWorkerType
import io.element.android.libraries.workmanager.api.workManagerTag
import java.util.concurrent.TimeUnit

class SearchBackfillRequestBuilder(
    private val sessionId: SessionId,
) : WorkManagerRequestBuilder {
    override suspend fun build(): Result<List<WorkManagerRequestWrapper>> {
        val tag = workManagerTag(sessionId, WorkManagerRequestType.SEARCH_BACKFILL)
        // One sweep per session at a time. The sweep is enqueued from every client start, and all
        // of its progress lives in a single stored cursor — two of them running at once would
        // interleave writes to it and pay twice for the same pages. KEEP rather than REPLACE
        // because a sweep already in flight is strictly more useful than restarting it.
        val type = WorkManagerWorkerType.Unique(name = tag, policy = ExistingWorkPolicy.KEEP)
        val data = Data.Builder().putString(SESSION_ID_PARAM, sessionId.value).build()
        val workRequest = OneTimeWorkRequest.Builder(SearchBackfillWorker::class)
            // Load-bearing and unenforced by the compiler: without this tag the sweep keeps
            // downloading a signed-out user's history, because cancel-on-logout works by tag.
            .addTag(tag)
            .setInputData(data)
            .setConstraints(
                Constraints.Builder()
                    // Never on cellular. The user did not ask for this download, so it must not
                    // arrive on their data plan.
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .setRequiresBatteryNotLow(true)
                    // The index grows as history lands; do not fill a nearly-full device.
                    .setRequiresStorageNotLow(true)
                    .build()
            )
            // Deliberately not expedited: this is entirely background work with no user waiting on it.
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.MINUTES)
            .build()

        return Result.success(listOf(WorkManagerRequestWrapper(workRequest, type)))
    }
}
