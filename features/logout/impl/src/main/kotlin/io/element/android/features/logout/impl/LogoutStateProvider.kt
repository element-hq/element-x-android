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

package io.element.android.features.logout.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.api.encryption.BackupUploadState
import io.element.android.libraries.matrix.api.encryption.SteadyStateException

open class LogoutStateProvider : PreviewParameterProvider<LogoutState> {
    override val values: Sequence<LogoutState>
        get() = sequenceOf(
            aLogoutState(),
            aLogoutState(isLastSession = true),
            aLogoutState(isLastSession = false, backupUploadState = BackupUploadState.Uploading(66, 200)),
            aLogoutState(isLastSession = true, backupUploadState = BackupUploadState.Done),
            aLogoutState(showConfirmationDialog = true),
            aLogoutState(logoutAction = Async.Loading()),
            aLogoutState(logoutAction = Async.Failure(Exception("Failed to logout"))),
            aLogoutState(backupUploadState = BackupUploadState.SteadyException(SteadyStateException.Connection("No network"))),
        )
}

fun aLogoutState(
    isLastSession: Boolean = false,
    backupUploadState: BackupUploadState = BackupUploadState.Unknown,
    showConfirmationDialog: Boolean = false,
    logoutAction: Async<String?> = Async.Uninitialized,
) = LogoutState(
    isLastSession = isLastSession,
    backupUploadState = backupUploadState,
    showConfirmationDialog = showConfirmationDialog,
    logoutAction = logoutAction,
    eventSink = {}
)
