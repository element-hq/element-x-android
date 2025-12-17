/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.features.securebackup.impl.loggerTagDisable
import io.element.android.features.securebackup.impl.loggerTagRoot
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.collectSnackbarMessageAsState
import io.element.android.libraries.matrix.api.encryption.BackupState
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@Inject
class SecureBackupRootPresenter(
    private val encryptionService: EncryptionService,
    private val buildMeta: BuildMeta,
    private val snackbarDispatcher: SnackbarDispatcher,
) : Presenter<SecureBackupRootState> {
    @Composable
    override fun present(): SecureBackupRootState {
        val localCoroutineScope = rememberCoroutineScope()
        val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()

        val backupState by encryptionService.backupStateStateFlow.collectAsState()
        val recoveryState by encryptionService.recoveryStateStateFlow.collectAsState()
        val enableAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }
        var displayKeyStorageDisabledError by remember { mutableStateOf(false) }
        Timber.tag(loggerTagRoot.value).d("backupState: $backupState")
        Timber.tag(loggerTagRoot.value).d("recoveryState: $recoveryState")

        val doesBackupExistOnServerAction: MutableState<AsyncData<Boolean>> = remember { mutableStateOf(AsyncData.Uninitialized) }

        LaunchedEffect(backupState) {
            if (backupState == BackupState.UNKNOWN) {
                getKeyBackupStatus(doesBackupExistOnServerAction)
            }
        }

        fun handleEvent(event: SecureBackupRootEvents) {
            when (event) {
                SecureBackupRootEvents.RetryKeyBackupState -> localCoroutineScope.getKeyBackupStatus(doesBackupExistOnServerAction)
                SecureBackupRootEvents.EnableKeyStorage -> localCoroutineScope.enableBackup(enableAction)
                SecureBackupRootEvents.DismissDialog -> {
                    enableAction.value = AsyncAction.Uninitialized
                    displayKeyStorageDisabledError = false
                }
                SecureBackupRootEvents.DisplayKeyStorageDisabledError -> displayKeyStorageDisabledError = true
            }
        }

        return SecureBackupRootState(
            enableAction = enableAction.value,
            backupState = backupState,
            doesBackupExistOnServer = doesBackupExistOnServerAction.value,
            recoveryState = recoveryState,
            appName = buildMeta.applicationName,
            displayKeyStorageDisabledError = displayKeyStorageDisabledError,
            snackbarMessage = snackbarMessage,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.getKeyBackupStatus(action: MutableState<AsyncData<Boolean>>) = launch {
        suspend {
            encryptionService.doesBackupExistOnServer().getOrThrow()
        }.runCatchingUpdatingState(action)
    }

    private fun CoroutineScope.enableBackup(action: MutableState<AsyncAction<Unit>>) = launch {
        suspend {
            Timber.tag(loggerTagDisable.value).d("Calling encryptionService.enableBackups()")
            encryptionService.enableBackups().getOrThrow()
        }.runCatchingUpdatingState(action)
    }
}
