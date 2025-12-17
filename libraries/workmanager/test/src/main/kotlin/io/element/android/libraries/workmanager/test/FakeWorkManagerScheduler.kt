/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.workmanager.test

import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.workmanager.api.WorkManagerRequest
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.WorkManagerScheduler
import io.element.android.tests.testutils.lambda.lambdaError

class FakeWorkManagerScheduler(
    private val submitLambda: (WorkManagerRequest) -> Unit = { lambdaError() },
    private val hasPendingWorkLambda: (SessionId, WorkManagerRequestType) -> Boolean = { _, _ -> false },
    private val cancelLambda: (SessionId) -> Unit = { lambdaError() },
) : WorkManagerScheduler {
    override fun submit(workManagerRequest: WorkManagerRequest) {
        submitLambda(workManagerRequest)
    }

    override fun hasPendingWork(sessionId: SessionId, requestType: WorkManagerRequestType): Boolean {
        return hasPendingWorkLambda(sessionId, requestType)
    }

    override fun cancel(sessionId: SessionId) {
        cancelLambda(sessionId)
    }
}
