/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

import io.element.android.libraries.matrix.api.core.RoomId

class ForwardEventException(
    val roomIds: List<RoomId>
) : Exception() {
    override val message: String? = "Failed to deliver event to $roomIds rooms"
}
