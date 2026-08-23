/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.search.backfill

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.search.MessageSearchSweepActivity
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.workmanager.api.WorkActivityState
import io.element.android.libraries.workmanager.api.WorkManagerRequestBuilder
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.test.FakeWorkManagerScheduler
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.robolectric.RobolectricTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultMessageSearchIndexerTest : RobolectricTest() {
    @Test
    fun `an executing background sweep reports RUNNING, not paused`() = runTest {
        val scheduler = FakeWorkManagerScheduler(
            workStateFlows = mutableMapOf(
                WorkManagerRequestType.SEARCH_BACKFILL to MutableStateFlow(WorkActivityState.RUNNING),
                WorkManagerRequestType.SEARCH_BACKFILL_USER to MutableStateFlow(WorkActivityState.NONE),
            ),
        )
        val indexer = createIndexer(scheduler, backgroundScope)

        assertThat(indexer.userSweepActivityFlow(A_SESSION_ID).first()).isEqualTo(MessageSearchSweepActivity.RUNNING)
    }

    @Test
    fun `the permanently enqueued background sweep does not read as waiting`() = runTest {
        val scheduler = FakeWorkManagerScheduler(
            workStateFlows = mutableMapOf(
                WorkManagerRequestType.SEARCH_BACKFILL to MutableStateFlow(WorkActivityState.ENQUEUED),
                WorkManagerRequestType.SEARCH_BACKFILL_USER to MutableStateFlow(WorkActivityState.NONE),
            ),
        )
        val indexer = createIndexer(scheduler, backgroundScope)

        assertThat(indexer.userSweepActivityFlow(A_SESSION_ID).first()).isEqualTo(MessageSearchSweepActivity.NONE)
    }

    @Test
    fun `an enqueued user sweep reports WAITING`() = runTest {
        val scheduler = FakeWorkManagerScheduler(
            workStateFlows = mutableMapOf(
                WorkManagerRequestType.SEARCH_BACKFILL to MutableStateFlow(WorkActivityState.ENQUEUED),
                WorkManagerRequestType.SEARCH_BACKFILL_USER to MutableStateFlow(WorkActivityState.ENQUEUED),
            ),
        )
        val indexer = createIndexer(scheduler, backgroundScope)

        assertThat(indexer.userSweepActivityFlow(A_SESSION_ID).first()).isEqualTo(MessageSearchSweepActivity.WAITING)
    }

    @Test
    fun `starting a sweep submits a user-initiated request`() = runTest {
        val submitted = mutableListOf<WorkManagerRequestBuilder>()
        val scheduler = FakeWorkManagerScheduler(submitLambda = { submitted += it })
        val indexer = createIndexer(scheduler, backgroundScope)

        indexer.startUserInitiatedSweep(A_SESSION_ID)

        val builder = submitted.single() as SearchBackfillRequestBuilder
        val request = builder.build().getOrThrow().single().request
        assertThat(request.workSpec.input.getBoolean(SearchBackfillWorker.USER_INITIATED_PARAM, false)).isTrue()
    }

    @Test
    fun `cancelling a sweep cancels by the shared type`() = runTest {
        val cancelRecorder = lambdaRecorder<SessionId, WorkManagerRequestType?, Unit> { _, _ -> }
        val scheduler = FakeWorkManagerScheduler(cancelLambda = cancelRecorder)
        val indexer = createIndexer(scheduler, backgroundScope)

        indexer.cancelSweep(A_SESSION_ID)

        cancelRecorder.assertions().isCalledOnce().with(value(A_SESSION_ID), value(WorkManagerRequestType.SEARCH_BACKFILL))
    }

    private fun createIndexer(
        scheduler: FakeWorkManagerScheduler,
        scope: CoroutineScope,
    ) = DefaultMessageSearchIndexer(
        storeHolder = SearchBackfillStoreHolder(
            context = ApplicationProvider.getApplicationContext(),
            appCoroutineScope = scope,
        ),
        workManagerScheduler = scheduler,
    )
}
