/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
import dev.zacsweers.metro.Inject
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyUserStory
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyViewState
import io.element.android.features.securebackup.impl.tools.RecoveryKeyTools
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Inject
class SecureBackupEnterRecoveryKeyPresenter(
    private val encryptionService: EncryptionService,
    private val recoveryKeyTools: RecoveryKeyTools,
) : Presenter<SecureBackupEnterRecoveryKeyState> {
    @Composable
    override fun present(): SecureBackupEnterRecoveryKeyState {
        val coroutineScope = rememberCoroutineScope()
        var displayRecoveryKeyFieldContents by rememberSaveable {
            mutableStateOf(false)
        }
        var recoveryKey by rememberSaveable {
            mutableStateOf("")
        }
        val submitAction: MutableState<AsyncAction<Unit>> = remember {
            mutableStateOf(AsyncAction.Uninitialized)
        }

        fun handleEvent(event: SecureBackupEnterRecoveryKeyEvents) {
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
                is SecureBackupEnterRecoveryKeyEvents.ChangeRecoveryKeyFieldContentsVisibility -> {
                    displayRecoveryKeyFieldContents = event.visible
                }
            }
        }

        return SecureBackupEnterRecoveryKeyState(
            recoveryKeyViewState = RecoveryKeyViewState(
                recoveryKeyUserStory = RecoveryKeyUserStory.Enter,
                formattedRecoveryKey = recoveryKey,
                displayTextFieldContents = displayRecoveryKeyFieldContents,
                inProgress = submitAction.value.isLoading(),
            ),
            isSubmitEnabled = recoveryKey.isNotEmpty() && submitAction.value.isUninitialized(),
            submitAction = submitAction.value,
            eventSink = ::handleEvent,
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
