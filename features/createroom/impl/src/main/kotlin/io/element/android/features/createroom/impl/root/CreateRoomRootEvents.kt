/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.root

import io.element.android.libraries.matrix.api.user.MatrixUser

sealed interface CreateRoomRootEvents {
    data class StartDM(val matrixUser: MatrixUser) : CreateRoomRootEvents
    data object CancelStartDM : CreateRoomRootEvents
}
