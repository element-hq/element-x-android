/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.userlist

import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class UserListDataStore @Inject constructor() {
    private val _selectedUsers: MutableStateFlow<List<MatrixUser>> = MutableStateFlow(emptyList())

    fun selectUser(user: MatrixUser) {
        if (!_selectedUsers.value.contains(user)) {
            _selectedUsers.tryEmit(_selectedUsers.value.plus(user))
        }
    }

    fun removeUserFromSelection(user: MatrixUser) {
        _selectedUsers.tryEmit(_selectedUsers.value.minus(user))
    }

    val selectedUsers = _selectedUsers.asStateFlow()
}
