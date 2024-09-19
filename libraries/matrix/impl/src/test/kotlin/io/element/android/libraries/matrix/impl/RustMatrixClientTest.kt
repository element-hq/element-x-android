/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeRustClient
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeRustSyncService
import io.element.android.libraries.matrix.impl.room.FakeTimelineEventTypeFilterFactory
import io.element.android.libraries.matrix.test.A_DEVICE_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.impl.memory.InMemorySessionStore
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.File

class RustMatrixClientTest {
    @Test
    fun `ensure that sessionId and deviceId can be retrieved from the client`() = runTest {
        createRustMatrixClient().use { sut ->
            assertThat(sut.sessionId).isEqualTo(A_USER_ID)
            assertThat(sut.deviceId).isEqualTo(A_DEVICE_ID)
        }
    }

    private fun TestScope.createRustMatrixClient(
        sessionStore: SessionStore = InMemorySessionStore(),
    ) = RustMatrixClient(
        client = FakeRustClient(),
        baseDirectory = File(""),
        sessionStore = sessionStore,
        appCoroutineScope = this,
        sessionDelegate = aRustClientSessionDelegate(
            sessionStore = sessionStore,
        ),
        syncService = FakeRustSyncService(),
        dispatchers = testCoroutineDispatchers(),
        baseCacheDirectory = File(""),
        clock = FakeSystemClock(),
        timelineEventTypeFilterFactory = FakeTimelineEventTypeFilterFactory(),
    )
}
