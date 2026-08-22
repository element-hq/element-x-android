/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.impl.status

import com.google.common.truth.Truth.assertThat
import io.element.android.features.privatepush.api.PrivatePushStatus
import io.element.android.features.privatepush.api.PrivatePushStatusEvents
import io.element.android.features.privatepush.test.FakePrivatePushService
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class PrivatePushStatusPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - loads the status and refreshes on demand`() = runTest {
        val service = FakePrivatePushService(statusResult = PrivatePushStatus.Private)
        val presenter = createPrivatePushStatusPresenter(service)
        presenter.test {
            assertThat(awaitItem().status).isEqualTo(AsyncData.Uninitialized)
            val loaded = awaitItem()
            assertThat(loaded.status).isEqualTo(AsyncData.Success(PrivatePushStatus.Private))

            service.statusResult = PrivatePushStatus.PublicServer("ntfy.sh")
            loaded.eventSink(PrivatePushStatusEvents.Refresh)
            // Recomposition triggered by the refresh counter, then the new status
            skipItems(1)
            assertThat(awaitItem().status).isEqualTo(AsyncData.Success(PrivatePushStatus.PublicServer("ntfy.sh")))
        }
    }

    private fun createPrivatePushStatusPresenter(
        privatePushService: FakePrivatePushService = FakePrivatePushService(),
    ): PrivatePushStatusPresenter {
        return PrivatePushStatusPresenter(
            matrixClient = FakeMatrixClient(),
            privatePushService = privatePushService,
        )
    }
}
