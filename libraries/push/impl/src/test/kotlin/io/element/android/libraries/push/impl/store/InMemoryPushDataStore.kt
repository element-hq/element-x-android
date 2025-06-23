/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.store

import io.element.android.libraries.push.api.history.PushHistoryItem
import io.element.android.tests.testutils.lambda.lambdaError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryPushDataStore(
    initialPushCounter: Int = 0,
    initialPushHistoryItems: List<PushHistoryItem> = emptyList(),
    private val resetResult: () -> Unit = { lambdaError() }
) : PushDataStore {
    private val mutablePushCounterFlow = MutableStateFlow(initialPushCounter)
    override val pushCounterFlow: Flow<Int> = mutablePushCounterFlow.asStateFlow()

    private val mutablePushHistoryItemsFlow = MutableStateFlow(initialPushHistoryItems)

    override fun getPushHistoryItemsFlow(): Flow<List<PushHistoryItem>> {
        return mutablePushHistoryItemsFlow.asStateFlow()
    }

    override suspend fun reset() {
        resetResult()
    }
}
