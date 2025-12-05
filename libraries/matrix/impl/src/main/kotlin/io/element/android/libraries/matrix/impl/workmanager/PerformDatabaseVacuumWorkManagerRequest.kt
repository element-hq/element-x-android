/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.workmanager

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkRequest
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.impl.workmanager.VacuumDatabaseWorker.Companion.SESSION_ID_PARAM
import io.element.android.libraries.workmanager.api.WorkManagerRequest
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.workManagerTag
import java.util.concurrent.TimeUnit

class PerformDatabaseVacuumWorkManagerRequest(
    private val sessionId: SessionId,
) : WorkManagerRequest {
    override fun build(): Result<List<WorkRequest>> {
        val data = Data.Builder().putString(SESSION_ID_PARAM, sessionId.value).build()
        val workRequest = PeriodicWorkRequest.Builder(
            workerClass = VacuumDatabaseWorker::class,
            // Run once a day
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS,
        )
            .addTag(workManagerTag(sessionId, WorkManagerRequestType.DB_VACUUM))
            .setInputData(data)
            // Only run when the device is idle to avoid impacting user experience
            .setConstraints(Constraints.Builder().setRequiresDeviceIdle(true).build())
            .build()

        return Result.success(listOf(workRequest))
    }
}
