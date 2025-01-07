/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.rolesandpermissions.changeroles

import io.element.android.libraries.matrix.api.user.MatrixUser

sealed interface ChangeRolesEvent {
    data object ToggleSearchActive : ChangeRolesEvent
    data class QueryChanged(val query: String?) : ChangeRolesEvent
    data class UserSelectionToggled(val matrixUser: MatrixUser) : ChangeRolesEvent
    data object Save : ChangeRolesEvent
    data object Exit : ChangeRolesEvent
    data object CancelExit : ChangeRolesEvent
    data object ClearError : ChangeRolesEvent
    data object CancelSave : ChangeRolesEvent
}
