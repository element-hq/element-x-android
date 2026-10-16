/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.search

import io.element.android.libraries.matrix.api.SdkPendingTask
import io.element.android.libraries.matrix.api.search.SearchBackfillService
import io.element.android.libraries.matrix.api.search.SearchBackfillStrategy
import io.element.android.tests.testutils.lambda.lambdaError

class FakeSearchBackfillService(
    private val schedulePeriodicSearchBackfillLambda: (SearchBackfillStrategy) -> Result<Unit> = { lambdaError() },
    private val cancelPeriodicSearchBackfillLambda: () -> Unit = { lambdaError() },
    private val startSearchBackfillLambda: (SearchBackfillStrategy) -> Result<SdkPendingTask> = { lambdaError() },
    private val isSearchBackfillRunningLambda: () -> Boolean = { false },
    private val cancelSearchBackfillLambda: () -> Result<Unit> = { lambdaError() },
) : SearchBackfillService {
    override suspend fun schedulePeriodicSearchBackfill(strategy: SearchBackfillStrategy): Result<Unit> {
        return schedulePeriodicSearchBackfillLambda(strategy)
    }

    override fun cancelPeriodicSearchBackfill() {
        cancelPeriodicSearchBackfillLambda()
    }

    override fun startSearchBackfill(strategy: SearchBackfillStrategy): Result<SdkPendingTask> {
        return startSearchBackfillLambda(strategy)
    }

    override fun isSearchBackfillRunning(): Boolean {
        return isSearchBackfillRunningLambda()
    }

    override fun cancelSearchBackfill(): Result<Unit> {
        return cancelSearchBackfillLambda()
    }
}
