/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomdirectory

import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiClient
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RustBaseRoomDirectoryServiceTest {
    @Test
    fun test() = runTest {
        val client = FakeFfiClient()
        val sut = RustRoomDirectoryService(
            client = client,
            sessionDispatcher = StandardTestDispatcher(testScheduler),
        )
        sut.createRoomDirectoryList(backgroundScope)
    }
}
