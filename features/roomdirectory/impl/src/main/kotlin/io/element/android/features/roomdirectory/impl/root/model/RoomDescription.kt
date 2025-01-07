/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdirectory.impl.root.model

import io.element.android.features.roomdirectory.api.RoomDescription
import io.element.android.libraries.matrix.api.roomdirectory.RoomDescription as MatrixRoomDescription

fun MatrixRoomDescription.toFeatureModel(): RoomDescription {
    return RoomDescription(
        roomId = roomId,
        name = name,
        alias = alias,
        topic = topic,
        avatarUrl = avatarUrl,
        numberOfMembers = numberOfMembers,
        joinRule = when (joinRule) {
            MatrixRoomDescription.JoinRule.PUBLIC -> RoomDescription.JoinRule.PUBLIC
            MatrixRoomDescription.JoinRule.KNOCK -> RoomDescription.JoinRule.KNOCK
            MatrixRoomDescription.JoinRule.UNKNOWN -> RoomDescription.JoinRule.UNKNOWN
        }
    )
}
