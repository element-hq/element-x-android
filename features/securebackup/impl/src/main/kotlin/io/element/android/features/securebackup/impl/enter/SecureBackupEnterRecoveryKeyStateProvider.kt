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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyUserStory
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyViewState
import io.element.android.features.securebackup.impl.setup.views.aFormattedRecoveryKey
import io.element.android.libraries.architecture.AsyncAction

open class SecureBackupEnterRecoveryKeyStateProvider : PreviewParameterProvider<SecureBackupEnterRecoveryKeyState> {
    override val values: Sequence<SecureBackupEnterRecoveryKeyState>
        get() = sequenceOf(
            aSecureBackupEnterRecoveryKeyState(recoveryKey = ""),
            aSecureBackupEnterRecoveryKeyState(),
            aSecureBackupEnterRecoveryKeyState(submitAction = AsyncAction.Loading),
            aSecureBackupEnterRecoveryKeyState(submitAction = AsyncAction.Failure(Exception("A Failure"))),
        )
}

fun aSecureBackupEnterRecoveryKeyState(
    recoveryKey: String = aFormattedRecoveryKey(),
    isSubmitEnabled: Boolean = recoveryKey.isNotEmpty(),
    submitAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (SecureBackupEnterRecoveryKeyEvents) -> Unit = {},
) = SecureBackupEnterRecoveryKeyState(
    recoveryKeyViewState = RecoveryKeyViewState(
        recoveryKeyUserStory = RecoveryKeyUserStory.Enter,
        formattedRecoveryKey = recoveryKey,
        inProgress = submitAction.isLoading(),
    ),
    isSubmitEnabled = isSubmitEnabled,
    submitAction = submitAction,
    eventSink = eventSink,
)
