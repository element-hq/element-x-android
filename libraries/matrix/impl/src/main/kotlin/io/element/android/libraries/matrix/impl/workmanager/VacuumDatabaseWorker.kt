/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.SessionId

@AssistedInject
class VacuumDatabaseWorker(
    @Assisted workerParams: WorkerParameters,
    @ApplicationContext private val context: Context,
    private val matrixClientProvider: MatrixClientProvider,
) : CoroutineWorker(context, workerParams) {
    companion object {
        const val SESSION_ID_PARAM = "session_id"
    }

    override suspend fun doWork(): Result {
        val sessionId = inputData.getString(SESSION_ID_PARAM)?.let(::SessionId) ?: return Result.failure()
        val client = matrixClientProvider.getOrRestore(sessionId).getOrNull() ?: return Result.failure()
        return client.performDatabaseVacuum()
            .fold(
                onSuccess = { Result.success() },
                onFailure = { Result.failure() }
            )
    }
}
