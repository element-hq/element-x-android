/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.check

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
class SecureBackupCheckKeyPresenter(
    private val encryptionService: EncryptionService,
    private val recoveryKeyTools: RecoveryKeyTools,
) : Presenter<SecureBackupCheckKeyState> {
    @Composable
    override fun present(): SecureBackupCheckKeyState {
        val coroutineScope = rememberCoroutineScope()
        var displayRecoveryKeyFieldContents by rememberSaveable { mutableStateOf(false) }
        var recoveryKey by rememberSaveable { mutableStateOf("") }
        val submitAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }

        fun handleEvent(event: SecureBackupCheckKeyEvent) {
            when (event) {
                is SecureBackupCheckKeyEvent.OnRecoveryKeyChange -> {
                    // Typing again is a new attempt.
                    if (submitAction.value.isFailure()) {
                        submitAction.value = AsyncAction.Uninitialized
                    }
                    recoveryKey = if (recoveryKey.isEmpty() && recoveryKeyTools.isRecoveryKeyFormatValid(event.recoveryKey)) {
                        // A recovery key has been entered, remove the spaces for a better rendering
                        event.recoveryKey.replace("\\s+".toRegex(), "")
                    } else {
                        // Keep the recovery key as entered by the user. May contain spaces.
                        event.recoveryKey
                    }
                }
                is SecureBackupCheckKeyEvent.ChangeRecoveryKeyFieldContentsVisibility -> {
                    displayRecoveryKeyFieldContents = event.visible
                }
                SecureBackupCheckKeyEvent.Submit -> {
                    // No need to remove the spaces, the SDK will do it.
                    coroutineScope.checkRecoveryKey(recoveryKey, submitAction)
                }
            }
        }

        return SecureBackupCheckKeyState(
            recoveryKeyViewState = RecoveryKeyViewState(
                recoveryKeyUserStory = RecoveryKeyUserStory.Enter,
                formattedRecoveryKey = recoveryKey,
                displayTextFieldContents = displayRecoveryKeyFieldContents,
                inProgress = submitAction.value.isLoading(),
            ),
            isSubmitEnabled = recoveryKey.isNotEmpty() && submitAction.value.isUninitialized(),
            isKeyIncorrect = submitAction.value.isFailure(),
            isCompleted = submitAction.value.isSuccess(),
            eventSink = ::handleEvent,
        )
    }

    // The SDK has no way to only verify a recovery key, so it is checked the way it is used to get back into the account.
    private fun CoroutineScope.checkRecoveryKey(
        recoveryKey: String,
        action: MutableState<AsyncAction<Unit>>,
    ) = launch {
        suspend {
            encryptionService.recover(recoveryKey).getOrThrow()
        }.runCatchingUpdatingState(action)
    }
}
