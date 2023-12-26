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

package io.element.android.features.logout.impl.tools

import io.element.android.libraries.matrix.api.encryption.BackupUploadState
import io.element.android.libraries.matrix.api.encryption.SteadyStateException

internal fun BackupUploadState.isBackingUp(): Boolean {
    return when (this) {
        BackupUploadState.Waiting,
        is BackupUploadState.Uploading -> true
        // The backup is in progress, but there have been a network issue, so we have to warn the user.
        is BackupUploadState.SteadyException -> exception is SteadyStateException.Connection
        BackupUploadState.Unknown,
        BackupUploadState.Done,
        BackupUploadState.Error -> false
    }
}
