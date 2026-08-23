/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.workmanager.test

import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.workmanager.api.WorkActivityState
import io.element.android.libraries.workmanager.api.WorkManagerRequestBuilder
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.WorkManagerScheduler
import io.element.android.tests.testutils.lambda.lambdaError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeWorkManagerScheduler(
    private val submitLambda: (WorkManagerRequestBuilder) -> Unit = { lambdaError() },
    private val hasPendingWorkLambda: (SessionId, WorkManagerRequestType) -> Boolean = { _, _ -> false },
    private val cancelLambda: (SessionId, WorkManagerRequestType?) -> Unit = { _, _ -> lambdaError() },
    val workStateFlows: MutableMap<WorkManagerRequestType, MutableStateFlow<WorkActivityState>> = mutableMapOf(),
) : WorkManagerScheduler {
    override suspend fun submit(workManagerRequestBuilder: WorkManagerRequestBuilder) {
        submitLambda(workManagerRequestBuilder)
    }

    override fun hasPendingWork(sessionId: SessionId, requestType: WorkManagerRequestType): Boolean {
        return hasPendingWorkLambda(sessionId, requestType)
    }

    override fun workStateFlow(sessionId: SessionId, requestType: WorkManagerRequestType): Flow<WorkActivityState> {
        return workStateFlows.getOrPut(requestType) { MutableStateFlow(WorkActivityState.NONE) }
    }

    override fun cancel(sessionId: SessionId, requestType: WorkManagerRequestType?) {
        cancelLambda(sessionId, requestType)
    }
}
