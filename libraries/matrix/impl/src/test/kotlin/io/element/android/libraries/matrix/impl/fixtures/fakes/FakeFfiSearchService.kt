/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import org.matrix.rustcomponents.sdk.NoHandle
import org.matrix.rustcomponents.sdk.SearchService
import org.matrix.rustcomponents.sdk.SearchServicePaginationStateListener
import org.matrix.rustcomponents.sdk.SearchServiceResultsListener
import org.matrix.rustcomponents.sdk.TaskHandle
import uniffi.matrix_sdk_ui.SearchServicePaginationState

class FakeFfiSearchService(
    private val initialPaginationState: SearchServicePaginationState = SearchServicePaginationState.Idle(endReached = false),
    private val setQueryLambda: (String) -> Unit = {},
    private val paginateLambda: () -> Unit = {},
) : SearchService(NoHandle) {
    var resultsListener: SearchServiceResultsListener? = null
        private set
    var paginationStateListener: SearchServicePaginationStateListener? = null
        private set
    var subscribeToResultsCallCount = 0
        private set

    override suspend fun paginate() = paginateLambda()

    override fun paginationState(): SearchServicePaginationState = initialPaginationState

    override suspend fun setQuery(query: String) = setQueryLambda(query)

    override fun subscribeToPaginationStateUpdates(listener: SearchServicePaginationStateListener): TaskHandle {
        paginationStateListener = listener
        return FakeFfiTaskHandle()
    }

    override suspend fun subscribeToResults(listener: SearchServiceResultsListener): TaskHandle {
        subscribeToResultsCallCount++
        resultsListener = listener
        return FakeFfiTaskHandle()
    }
}
