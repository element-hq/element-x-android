/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.roomlist

class FakeMarkAllRoomsAsRead(
    private var hasUnread: Boolean = false,
    private val invokeLambda: suspend () -> Result<MarkAllRoomsAsReadResult> = {
        Result.success(MarkAllRoomsAsReadResult(processedCount = 0, failedCount = 0))
    },
) : MarkAllRoomsAsRead {
    var hasUnreadRoomsCallCount = 0
    var invokeCallCount = 0

    override suspend fun hasUnreadRooms(): Boolean {
        hasUnreadRoomsCallCount++
        return hasUnread
    }

    override suspend fun invoke(): Result<MarkAllRoomsAsReadResult> {
        invokeCallCount++
        return invokeLambda()
    }
}
