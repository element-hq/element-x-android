/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.data

import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.powerlevels.canBan
import io.element.android.libraries.matrix.api.room.powerlevels.canInvite
import io.element.android.libraries.matrix.api.room.powerlevels.canKick
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class KnockRequestPermissions(
    val canAccept: Boolean,
    val canDecline: Boolean,
    val canBan: Boolean,
) {
    val canHandle = canAccept || canDecline || canBan
}

fun JoinedRoom.knockRequestPermissionsFlow(): Flow<KnockRequestPermissions> {
    return syncUpdateFlow.map {
        val canAccept = canInvite().getOrDefault(false)
        val canDecline = canKick().getOrDefault(false)
        val canBan = canBan().getOrDefault(false)
        KnockRequestPermissions(canAccept, canDecline, canBan)
    }
}
