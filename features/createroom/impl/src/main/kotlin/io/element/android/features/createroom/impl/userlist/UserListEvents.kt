/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.userlist

import io.element.android.libraries.matrix.api.user.MatrixUser

sealed interface UserListEvents {
    data class UpdateSearchQuery(val query: String) : UserListEvents
    data class AddToSelection(val matrixUser: MatrixUser) : UserListEvents
    data class RemoveFromSelection(val matrixUser: MatrixUser) : UserListEvents
    data class OnSearchActiveChanged(val active: Boolean) : UserListEvents
}
