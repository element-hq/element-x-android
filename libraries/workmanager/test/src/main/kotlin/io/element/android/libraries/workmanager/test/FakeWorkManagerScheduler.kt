/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.workmanager.test

import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.workmanager.api.WorkManagerRequest
import io.element.android.libraries.workmanager.api.WorkManagerScheduler
import io.element.android.tests.testutils.lambda.lambdaError

class FakeWorkManagerScheduler(
    private val submitLambda: (WorkManagerRequest) -> Unit = { lambdaError() },
    private val cancelLambda: (SessionId) -> Unit = { lambdaError() },
) : WorkManagerScheduler {
    override fun submit(workManagerRequest: WorkManagerRequest) {
        submitLambda(workManagerRequest)
    }

    override fun cancel(sessionId: SessionId) {
        cancelLambda(sessionId)
    }
}
