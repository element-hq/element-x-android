/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomlist

import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeRustRoomList
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeRustRoomListService
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.coroutines.EmptyCoroutineContext

class RoomListFactoryTest {
    @Test
    fun `createRoomList should work`() = runTest {
        val sut = RoomListFactory(
            innerRoomListService = FakeRustRoomListService(),
            sessionCoroutineScope = backgroundScope,
        )
        sut.createRoomList(
            pageSize = 10,
            coroutineContext = EmptyCoroutineContext,
        ) {
            FakeRustRoomList()
        }
    }
}
