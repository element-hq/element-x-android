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

package io.element.android.features.securebackup.impl.disable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.element.android.features.securebackup.impl.loggerTagDisable
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SecureBackupDisablePresenter @Inject constructor(
    private val encryptionService: EncryptionService,
    private val buildMeta: BuildMeta,
) : Presenter<SecureBackupDisableState> {

    @Composable
    override fun present(): SecureBackupDisableState {
        val backupState by encryptionService.backupStateStateFlow.collectAsState()
        Timber.tag(loggerTagDisable.value).d("backupState: $backupState")
        val disableAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }
        val coroutineScope = rememberCoroutineScope()
        var showDialog by remember { mutableStateOf(false) }
        fun handleEvents(event: SecureBackupDisableEvents) {
            when (event) {
                is SecureBackupDisableEvents.DisableBackup -> if (event.force) {
                    showDialog = false
                    coroutineScope.disableBackup(disableAction)
                } else {
                    showDialog = true
                }
                SecureBackupDisableEvents.DismissDialogs -> {
                    showDialog = false
                    disableAction.value = AsyncAction.Uninitialized
                }
            }
        }

        return SecureBackupDisableState(
            backupState = backupState,
            disableAction = disableAction.value,
            showConfirmationDialog = showDialog,
            appName = buildMeta.applicationName,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.disableBackup(disableAction: MutableState<AsyncAction<Unit>>) = launch {
        suspend {
            Timber.tag(loggerTagDisable.value).d("Calling encryptionService.disableRecovery()")
            encryptionService.disableRecovery().getOrThrow()
        }.runCatchingUpdatingState(disableAction)
    }
}
