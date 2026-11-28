/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.generate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.features.securebackup.impl.R
import io.element.android.libraries.androidutils.clipboard.ClipboardHelper
import io.element.android.libraries.androidutils.toast.ToastHelper
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AssistedInject
class SecureBackupGenerateKeyPresenter(
    @Assisted private val isReplacingExistingKey: Boolean,
    private val encryptionService: EncryptionService,
    private val clipboardHelper: ClipboardHelper,
    private val toastHelper: ToastHelper,
) : Presenter<SecureBackupGenerateKeyState> {
    @AssistedFactory
    interface Factory {
        fun create(isReplacingExistingKey: Boolean): SecureBackupGenerateKeyPresenter
    }

    @Composable
    override fun present(): SecureBackupGenerateKeyState {
        val coroutineScope = rememberCoroutineScope()
        // The key is generated straight away, but only becomes active once the user has confirmed that they saved it.
        val recoveryKey = rememberSaveable { encryptionService.generateRecoveryKey() }
        var showSaveConfirmationDialog by remember { mutableStateOf(false) }
        val activateAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }

        fun handleEvent(event: SecureBackupGenerateKeyEvent) {
            when (event) {
                SecureBackupGenerateKeyEvent.CopyRecoveryKey -> {
                    clipboardHelper.copyPlainText(recoveryKey, isSensitive = true) {
                        toastHelper.show(R.string.screen_recovery_key_copied_to_clipboard)
                    }
                }
                SecureBackupGenerateKeyEvent.Continue -> showSaveConfirmationDialog = true
                SecureBackupGenerateKeyEvent.DismissConfirmation -> showSaveConfirmationDialog = false
                SecureBackupGenerateKeyEvent.ConfirmSaved -> {
                    showSaveConfirmationDialog = false
                    coroutineScope.activateRecoveryKey(activateAction)
                }
                SecureBackupGenerateKeyEvent.DismissError -> activateAction.value = AsyncAction.Uninitialized
            }
        }

        return SecureBackupGenerateKeyState(
            isReplacingExistingKey = isReplacingExistingKey,
            recoveryKey = recoveryKey,
            showSaveConfirmationDialog = showSaveConfirmationDialog,
            activateAction = activateAction.value,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.activateRecoveryKey(action: MutableState<AsyncAction<Unit>>) = launch {
        suspend {
            encryptionService.activateRecoveryKey().getOrThrow()
        }.runCatchingUpdatingState(action)
    }
}
