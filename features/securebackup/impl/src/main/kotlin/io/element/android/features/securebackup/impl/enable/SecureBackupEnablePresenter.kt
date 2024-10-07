/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.enable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.features.securebackup.impl.loggerTagDisable
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SecureBackupEnablePresenter @Inject constructor(
    private val encryptionService: EncryptionService,
) : Presenter<SecureBackupEnableState> {
    @Composable
    override fun present(): SecureBackupEnableState {
        val enableAction = remember { mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized) }
        val coroutineScope = rememberCoroutineScope()
        fun handleEvents(event: SecureBackupEnableEvents) {
            when (event) {
                is SecureBackupEnableEvents.EnableBackup ->
                    coroutineScope.enableBackup(enableAction)
                SecureBackupEnableEvents.DismissDialog -> {
                    enableAction.value = AsyncAction.Uninitialized
                }
            }
        }

        return SecureBackupEnableState(
            enableAction = enableAction.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.enableBackup(action: MutableState<AsyncAction<Unit>>) = launch {
        suspend {
            Timber.tag(loggerTagDisable.value).d("Calling encryptionService.enableBackups()")
            encryptionService.enableBackups().getOrThrow()
        }.runCatchingUpdatingState(action)
    }
}
