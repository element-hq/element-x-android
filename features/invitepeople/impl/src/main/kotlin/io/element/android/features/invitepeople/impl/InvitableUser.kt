/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invitepeople.impl

import io.element.android.libraries.matrix.api.user.MatrixUser

data class InvitableUser(
    val matrixUser: MatrixUser,
    val isSelected: Boolean = false,
    val isAlreadyJoined: Boolean = false,
    val isAlreadyInvited: Boolean = false,
    val isUnresolved: Boolean = false,
)
