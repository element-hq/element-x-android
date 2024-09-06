/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.impl.userlist

import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class UserListDataStore @Inject constructor() {
    private val selectedUsers: MutableStateFlow<List<MatrixUser>> = MutableStateFlow(emptyList())

    fun selectUser(user: MatrixUser) {
        if (!selectedUsers.value.contains(user)) {
            selectedUsers.tryEmit(selectedUsers.value.plus(user))
        }
    }

    fun removeUserFromSelection(user: MatrixUser) {
        selectedUsers.tryEmit(selectedUsers.value.minus(user))
    }

    fun selectedUsers(): Flow<List<MatrixUser>> = selectedUsers
}
