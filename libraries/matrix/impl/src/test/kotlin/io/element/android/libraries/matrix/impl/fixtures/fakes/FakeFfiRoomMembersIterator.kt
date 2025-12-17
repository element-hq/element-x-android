/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import org.matrix.rustcomponents.sdk.NoHandle
import org.matrix.rustcomponents.sdk.RoomMember
import org.matrix.rustcomponents.sdk.RoomMembersIterator

class FakeFfiRoomMembersIterator(
    private var members: List<RoomMember>? = null
) : RoomMembersIterator(NoHandle) {
    override fun len(): UInt {
        return members?.size?.toUInt() ?: 0u
    }

    override fun nextChunk(chunkSize: UInt): List<RoomMember>? {
        if (members?.isEmpty() == true) {
            return null
        }
        return members?.let {
            val result = it.take(chunkSize.toInt())
            members = it.subList(result.size, it.size)
            result
        }
    }
}
