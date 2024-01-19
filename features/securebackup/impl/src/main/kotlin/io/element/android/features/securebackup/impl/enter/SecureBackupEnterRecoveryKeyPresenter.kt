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

package io.element.android.features.securebackup.impl.enter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyUserStory
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyViewState
import io.element.android.features.securebackup.impl.tools.RecoveryKeyTools
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SecureBackupEnterRecoveryKeyPresenter @Inject constructor(
    private val encryptionService: EncryptionService,
    private val recoveryKeyTools: RecoveryKeyTools,
) : Presenter<SecureBackupEnterRecoveryKeyState> {
    @Composable
    override fun present(): SecureBackupEnterRecoveryKeyState {
        val coroutineScope = rememberCoroutineScope()
        var recoveryKey by rememberSaveable {
            mutableStateOf("")
        }
        val submitAction: MutableState<AsyncAction<Unit>> = remember {
            mutableStateOf(AsyncAction.Uninitialized)
        }

        fun handleEvents(event: SecureBackupEnterRecoveryKeyEvents) {
            when (event) {
                SecureBackupEnterRecoveryKeyEvents.ClearDialog -> {
                    submitAction.value = AsyncAction.Uninitialized
                }
                is SecureBackupEnterRecoveryKeyEvents.OnRecoveryKeyChange -> {
                    val previousRecoveryKey = recoveryKey
                    recoveryKey = if (previousRecoveryKey.isEmpty() && recoveryKeyTools.isRecoveryKeyFormatValid(event.recoveryKey)) {
                        // A Recovery key has been entered, remove the spaces for a better rendering
                        event.recoveryKey.replace("\\s+".toRegex(), "")
                    } else {
                        // Keep the recovery key as entered by the user. May contains spaces.
                        event.recoveryKey
                    }
                }
                SecureBackupEnterRecoveryKeyEvents.Submit -> {
                    // No need to remove the spaces, the SDK will do it.
                    coroutineScope.submitRecoveryKey(recoveryKey, submitAction)
                }
            }
        }

        return SecureBackupEnterRecoveryKeyState(
            recoveryKeyViewState = RecoveryKeyViewState(
                recoveryKeyUserStory = RecoveryKeyUserStory.Enter,
                formattedRecoveryKey = recoveryKey,
                inProgress = submitAction.value.isLoading(),
            ),
            isSubmitEnabled = recoveryKey.isNotEmpty() && submitAction.value.isUninitialized(),
            submitAction = submitAction.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.submitRecoveryKey(
        recoveryKey: String,
        action: MutableState<AsyncAction<Unit>>
    ) = launch {
        suspend {
            encryptionService.recover(recoveryKey).getOrThrow()
        }.runCatchingUpdatingState(action)
    }
}
