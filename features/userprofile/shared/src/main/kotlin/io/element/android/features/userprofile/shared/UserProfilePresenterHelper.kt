/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.userprofile.shared

import androidx.compose.runtime.MutableState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class UserProfilePresenterHelper(
    private val userId: UserId,
    private val client: MatrixClient,
) {
    fun blockUser(
        scope: CoroutineScope,
        isBlockedState: MutableState<AsyncData<Boolean>>,
    ) = scope.launch {
        isBlockedState.value = AsyncData.Loading(false)
        client.ignoreUser(userId)
            .onFailure {
                isBlockedState.value = AsyncData.Failure(it, false)
            }
        // Note: on success, ignoredUserList will be updated.
    }

    fun unblockUser(
        scope: CoroutineScope,
        isBlockedState: MutableState<AsyncData<Boolean>>,
    ) = scope.launch {
        isBlockedState.value = AsyncData.Loading(true)
        client.unignoreUser(userId)
            .onFailure {
                isBlockedState.value = AsyncData.Failure(it, true)
            }
        // Note: on success, ignoredUserList will be updated.
    }
}
