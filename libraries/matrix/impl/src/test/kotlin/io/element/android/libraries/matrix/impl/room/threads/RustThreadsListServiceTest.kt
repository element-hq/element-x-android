/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.threads

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.threads.ThreadListPaginationStatus
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiTaskHandle
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiThreadListService
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.TaskHandle
import org.matrix.rustcomponents.sdk.ThreadListPaginationStateListener
import uniffi.matrix_sdk_ui.ThreadListPaginationState

@OptIn(ExperimentalCoroutinesApi::class)
class RustThreadsListServiceTest {
    @Test
    fun `subscribing to pagination updates calls the FFI method and allows retrieving new items`() = runTest {
        val subscribeToPaginationUpdatesRecorder = lambdaRecorder<ThreadListPaginationStateListener, TaskHandle> { FakeFfiTaskHandle() }
        val inner = FakeFfiThreadListService(subscribeToPaginationStateUpdates = subscribeToPaginationUpdatesRecorder)
        val service = createThreadsListService(inner = inner)

        service.subscribeToPaginationUpdates().test {
            assertThat(awaitItem()).isEqualTo(ThreadListPaginationStatus.Idle(hasMoreToLoad = true))

            runCurrent()
            subscribeToPaginationUpdatesRecorder.assertions().isCalledOnce()

            inner.emitPaginationState(ThreadListPaginationState.Loading)

            assertThat(awaitItem()).isEqualTo(ThreadListPaginationStatus.Loading)
        }
    }

    @Test
    fun `paginate calls the FFI method`() = runTest {
        val paginateRecorder = lambdaRecorder<Unit> {}
        val inner = FakeFfiThreadListService(paginate = paginateRecorder)
        val service = createThreadsListService(inner = inner)

        service.paginate()

        paginateRecorder.assertions().isCalledOnce()
    }

    @Test
    fun `reset calls the FFI method`() = runTest {
        val resetRecorder = lambdaRecorder<Unit> {}
        val inner = FakeFfiThreadListService(reset = resetRecorder)
        val service = createThreadsListService(inner = inner)

        service.reset()

        resetRecorder.assertions().isCalledOnce()
    }

    @Test
    fun `destroy calls the FFI method`() = runTest {
        val destroyRecorder = lambdaRecorder<Unit> {}
        val inner = FakeFfiThreadListService(destroy = destroyRecorder)
        val service = createThreadsListService(inner = inner)

        service.destroy()

        destroyRecorder.assertions().isCalledOnce()
    }

    private fun createThreadsListService(
        inner: FakeFfiThreadListService = FakeFfiThreadListService(),
    ) = RustThreadsListService(
        inner = inner,
    )
}
