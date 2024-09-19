/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomdirectory

import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeRustClient
import io.element.android.tests.testutils.runCancellableScopeTestWithTestScope
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Test

class RustRoomDirectoryServiceTest {
    @Test
    fun test() = runCancellableScopeTestWithTestScope { testScope, cancellableScope ->
        val client = FakeRustClient()
        val sut = RustRoomDirectoryService(
            client = client,
            sessionDispatcher = StandardTestDispatcher(testScope.testScheduler),
        )
        sut.createRoomDirectoryList(cancellableScope)
    }
}
