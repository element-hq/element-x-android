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

package io.element.android.features.securebackup.impl.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.features.securebackup.impl.loggerTagRoot
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
import javax.inject.Inject

class SecureBackupRootPresenter @Inject constructor(
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

        Timber.tag(loggerTagRoot.value).d("backupState: $backupState")
        Timber.tag(loggerTagRoot.value).d("recoveryState: $recoveryState")

        val doesBackupExistOnServerAction: MutableState<AsyncData<Boolean>> = remember { mutableStateOf(AsyncData.Uninitialized) }

        LaunchedEffect(backupState) {
            if (backupState == BackupState.UNKNOWN) {
                getKeyBackupStatus(doesBackupExistOnServerAction)
            }
        }

        fun handleEvents(event: SecureBackupRootEvents) {
            when (event) {
                SecureBackupRootEvents.RetryKeyBackupState -> localCoroutineScope.getKeyBackupStatus(doesBackupExistOnServerAction)
            }
        }

        return SecureBackupRootState(
            backupState = backupState,
            doesBackupExistOnServer = doesBackupExistOnServerAction.value,
            recoveryState = recoveryState,
            appName = buildMeta.applicationName,
            snackbarMessage = snackbarMessage,
            eventSink = ::handleEvents,
        )
    }

    private fun CoroutineScope.getKeyBackupStatus(action: MutableState<AsyncData<Boolean>>) = launch {
        suspend {
            encryptionService.doesBackupExistOnServer().getOrThrow()
        }.runCatchingUpdatingState(action)
    }
}
