/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import io.element.android.tests.testutils.lambda.lambdaError
import org.matrix.rustcomponents.sdk.NoHandle
import org.matrix.rustcomponents.sdk.RoomList
import org.matrix.rustcomponents.sdk.RoomListEntriesListener
import org.matrix.rustcomponents.sdk.RoomListEntriesWithDynamicAdaptersResult
import org.matrix.rustcomponents.sdk.RoomListLoadingState
import org.matrix.rustcomponents.sdk.RoomListLoadingStateListener
import org.matrix.rustcomponents.sdk.RoomListLoadingStateResult

class FakeFfiRoomList(
    private val entriesWithDynamicAdaptersResult: () -> RoomListEntriesWithDynamicAdaptersResult = { lambdaError() },
    private val loadingStateResult: () -> RoomListLoadingStateResult = {
        RoomListLoadingStateResult(state = RoomListLoadingState.NotLoaded, stateStream = FakeFfiTaskHandle())
    },
) : RoomList(NoHandle) {
    override fun entriesWithDynamicAdapters(pageSize: UInt, listener: RoomListEntriesListener): RoomListEntriesWithDynamicAdaptersResult {
        return entriesWithDynamicAdaptersResult()
    }

    override fun loadingState(listener: RoomListLoadingStateListener): RoomListLoadingStateResult {
        return loadingStateResult()
    }

    override fun destroy() = Unit
}
