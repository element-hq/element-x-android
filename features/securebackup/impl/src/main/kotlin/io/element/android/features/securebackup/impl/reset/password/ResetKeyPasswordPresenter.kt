/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.securebackup.impl.reset.password

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.IdentityPasswordResetHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ResetKeyPasswordPresenter(
    private val userId: UserId,
    private val identityPasswordResetHandle: IdentityPasswordResetHandle,
) : Presenter<ResetKeyPasswordState> {
    @Composable
    override fun present(): ResetKeyPasswordState {
        val coroutineScope = rememberCoroutineScope()

        val resetAction = remember { mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized) }

        fun handleEvent(event: ResetKeyPasswordEvent) {
            when (event) {
                is ResetKeyPasswordEvent.Reset -> coroutineScope.reset(userId, event.password, resetAction)
                ResetKeyPasswordEvent.DismissError -> resetAction.value = AsyncAction.Uninitialized
            }
        }

        return ResetKeyPasswordState(
            resetAction = resetAction.value,
            eventSink = ::handleEvent
        )
    }

    private fun CoroutineScope.reset(userId: UserId, password: String, action: MutableState<AsyncAction<Unit>>) = launch {
        suspend {
            identityPasswordResetHandle.resetPassword(userId, password).getOrThrow()
        }.runCatchingUpdatingState(action)
    }
}
