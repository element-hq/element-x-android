/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.test.actions

import io.element.android.features.poll.api.actions.SendPollResponseAction
import io.element.android.libraries.matrix.api.core.EventId

class FakeSendPollResponseAction : SendPollResponseAction {
    private var executionCount = 0

    fun verifyExecutionCount(count: Int) {
        assert(executionCount == count)
    }

    override suspend fun execute(pollStartId: EventId, answerId: String): Result<Unit> {
        executionCount++
        return Result.success(Unit)
    }
}
