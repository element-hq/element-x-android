/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.search.backfill

import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.WorkManagerWorkerType
import io.element.android.libraries.workmanager.api.workManagerTag
import io.element.android.tests.testutils.robolectric.RobolectricTest
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SearchBackfillRequestBuilderTest : RobolectricTest() {
    @Test
    fun `the sweep is unique per session and keeps one already in flight`() = runTest {
        // The sweep is enqueued from every client start and keeps all of its progress in a single
        // stored cursor, so two concurrent runs would interleave writes to it and re-fetch the same
        // pages. Enqueued as ordinary work this would happen on the second launch.
        val results = SearchBackfillRequestBuilder(A_SESSION_ID).build()

        val wrapper = results.getOrThrow().single()
        val type = wrapper.type as WorkManagerWorkerType.Unique
        assertThat(type.name).isEqualTo(workManagerTag(A_SESSION_ID, WorkManagerRequestType.SEARCH_BACKFILL))
        assertThat(type.policy).isEqualTo(ExistingWorkPolicy.KEEP)
    }

    @Test
    fun `the request is tagged for logout cancellation and constrained to free network`() = runTest {
        val results = SearchBackfillRequestBuilder(A_SESSION_ID).build()

        val request = results.getOrThrow().single().request
        assertThat(request).isInstanceOf(OneTimeWorkRequest::class.java)
        // Cancel-on-logout works by tag: without it the sweep keeps downloading a signed-out user's
        // history.
        assertThat(request.tags).contains(workManagerTag(A_SESSION_ID, WorkManagerRequestType.SEARCH_BACKFILL))
        // The user did not ask for this download, so it must not arrive on their data plan.
        assertThat(request.workSpec.constraints.requiredNetworkType).isEqualTo(NetworkType.UNMETERED)
    }
}
