/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomlist

import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiRoomList
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiRoomListService
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.coroutines.EmptyCoroutineContext

class RoomListFactoryTest {
    @Test
    fun `createRoomList should work`() = runTest {
        val sut = RoomListFactory(
            innerRoomListService = FakeFfiRoomListService(),
            sessionCoroutineScope = backgroundScope,
        )
        sut.createRoomList(
            pageSize = 10,
            coroutineContext = EmptyCoroutineContext,
        ) {
            FakeFfiRoomList()
        }
    }
}
