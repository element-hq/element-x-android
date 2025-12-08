/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import io.element.android.features.securityandprivacy.api.SecurityAndPrivacyPermissions.Companion.DEFAULT
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.api.room.powerlevels.canSendState

data class SecurityAndPrivacyPermissions(
    val canChangeRoomAccess: Boolean,
    val canChangeHistoryVisibility: Boolean,
    val canChangeEncryption: Boolean,
    val canChangeRoomVisibility: Boolean,
) {
    val hasAny = canChangeRoomAccess ||
        canChangeHistoryVisibility ||
        canChangeEncryption ||
        canChangeRoomVisibility

    companion object {
        val DEFAULT = SecurityAndPrivacyPermissions(
            canChangeRoomAccess = false,
            canChangeHistoryVisibility = false,
            canChangeEncryption = false,
            canChangeRoomVisibility = false,
        )
    }
}

@Composable
fun BaseRoom.securityAndPrivacyPermissionsAsState(updateKey: Long): State<SecurityAndPrivacyPermissions> {
    return produceState(DEFAULT, key1 = updateKey) {
        value = SecurityAndPrivacyPermissions(
            canChangeRoomAccess = canSendState(type = StateEventType.ROOM_JOIN_RULES).getOrElse { false },
            canChangeHistoryVisibility = canSendState(type = StateEventType.ROOM_HISTORY_VISIBILITY).getOrElse { false },
            canChangeEncryption = canSendState(type = StateEventType.ROOM_ENCRYPTION).getOrElse { false },
            canChangeRoomVisibility = canSendState(type = StateEventType.ROOM_CANONICAL_ALIAS).getOrElse { false },
        )
    }
}
