/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.logout.impl.direct

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Inject
import io.element.android.features.logout.api.direct.DirectLogoutEvents
import io.element.android.features.logout.api.direct.DirectLogoutState
import io.element.android.features.logout.impl.tools.isBackingUp
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.encryption.BackupUploadState
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Inject
class DirectLogoutPresenter(
    private val matrixClient: MatrixClient,
    private val encryptionService: EncryptionService,
) : Presenter<DirectLogoutState> {
    @Composable
    override fun present(): DirectLogoutState {
        val localCoroutineScope = rememberCoroutineScope()

        val logoutAction: MutableState<AsyncAction<Unit>> = remember {
            mutableStateOf(AsyncAction.Uninitialized)
        }

        val backupUploadState: BackupUploadState by remember {
            encryptionService.waitForBackupUploadSteadyState()
        }
            .collectAsState(initial = BackupUploadState.Unknown)

        val isLastDevice by encryptionService.isLastDevice.collectAsState()

        fun handleEvent(event: DirectLogoutEvents) {
            when (event) {
                is DirectLogoutEvents.Logout -> {
                    if (logoutAction.value.isConfirming() || event.ignoreSdkError) {
                        localCoroutineScope.logout(logoutAction, event.ignoreSdkError)
                    } else {
                        logoutAction.value = AsyncAction.ConfirmingNoParams
                    }
                }
                DirectLogoutEvents.CloseDialogs -> {
                    logoutAction.value = AsyncAction.Uninitialized
                }
            }
        }

        return DirectLogoutState(
            canDoDirectSignOut = !isLastDevice &&
                !backupUploadState.isBackingUp(),
            logoutAction = logoutAction.value,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.logout(
        logoutAction: MutableState<AsyncAction<Unit>>,
        ignoreSdkError: Boolean,
    ) = launch {
        suspend {
            matrixClient.logout(userInitiated = true, ignoreSdkError)
        }.runCatchingUpdatingState(logoutAction)
    }
}
