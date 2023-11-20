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

package io.element.android.features.securebackup.impl.setup

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyUserStory
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyViewState
import io.element.android.features.securebackup.impl.setup.views.aFormattedRecoveryKey

open class SecureBackupSetupStateProvider : PreviewParameterProvider<SecureBackupSetupState> {
    override val values: Sequence<SecureBackupSetupState>
        get() = sequenceOf(
            aSecureBackupSetupState(setupState = SetupState.Init),
            aSecureBackupSetupState(setupState = SetupState.Creating),
            aSecureBackupSetupState(setupState = SetupState.Created(aFormattedRecoveryKey())),
            aSecureBackupSetupState(setupState = SetupState.CreatedAndSaved(aFormattedRecoveryKey())),
            aSecureBackupSetupState(
                setupState = SetupState.CreatedAndSaved(aFormattedRecoveryKey()),
                showSaveConfirmationDialog = true,
            ),
            // Add other states here
        )
}

fun aSecureBackupSetupState(
    setupState: SetupState = SetupState.Init,
    showSaveConfirmationDialog: Boolean = false,
) = SecureBackupSetupState(
    isChangeRecoveryKeyUserStory = false,
    setupState = setupState,
    showSaveConfirmationDialog = showSaveConfirmationDialog,
    recoveryKeyViewState = setupState.toRecoveryKeyViewState(),
    eventSink = {}
)

private fun SetupState.toRecoveryKeyViewState(): RecoveryKeyViewState {
    return RecoveryKeyViewState(
        recoveryKeyUserStory = RecoveryKeyUserStory.Setup,
        formattedRecoveryKey = recoveryKey(),
        inProgress = this is SetupState.Creating,
    )
}
