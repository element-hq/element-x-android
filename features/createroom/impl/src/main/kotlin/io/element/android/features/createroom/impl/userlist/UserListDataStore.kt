/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
