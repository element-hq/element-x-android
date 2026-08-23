/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.search

import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.search.MessageSearchIndexer
import io.element.android.libraries.matrix.api.search.MessageSearchSweepActivity
import io.element.android.libraries.matrix.api.search.SearchBackfillCursor
import io.element.android.tests.testutils.lambda.lambdaError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeMessageSearchIndexer(
    val cursorFlow: MutableStateFlow<SearchBackfillCursor?> = MutableStateFlow(null),
    val userSweepActivityFlow: MutableStateFlow<MessageSearchSweepActivity> = MutableStateFlow(MessageSearchSweepActivity.NONE),
    private val startUserInitiatedSweepLambda: (SessionId) -> Unit = { lambdaError() },
    private val cancelSweepLambda: (SessionId) -> Unit = { lambdaError() },
) : MessageSearchIndexer {
    override fun cursorFlow(sessionId: SessionId): Flow<SearchBackfillCursor?> = cursorFlow

    override fun userSweepActivityFlow(sessionId: SessionId): Flow<MessageSearchSweepActivity> = userSweepActivityFlow

    override suspend fun startUserInitiatedSweep(sessionId: SessionId) {
        startUserInitiatedSweepLambda(sessionId)
    }

    override fun cancelSweep(sessionId: SessionId) {
        cancelSweepLambda(sessionId)
    }
}
