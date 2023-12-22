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

package io.element.android.libraries.matrix.impl.encryption

import io.element.android.libraries.matrix.api.encryption.BackupUploadState
import org.matrix.rustcomponents.sdk.BackupUploadState as RustBackupUploadState

class BackupUploadStateMapper {
    fun map(rustEnableProgress: RustBackupUploadState): BackupUploadState {
        return when (rustEnableProgress) {
            RustBackupUploadState.Done ->
                BackupUploadState.Done
            is RustBackupUploadState.Uploading -> {
                val backedUpCount = rustEnableProgress.backedUpCount.toInt()
                val totalCount = rustEnableProgress.totalCount.toInt()
                if (backedUpCount == totalCount) {
                    // Consider that the state is Done in this case,
                    // the SDK will not send a Done state
                    BackupUploadState.Done
                } else {
                    BackupUploadState.Uploading(
                        backedUpCount = backedUpCount,
                        totalCount = totalCount,
                    )
                }
            }
            RustBackupUploadState.Waiting ->
                BackupUploadState.Waiting
            RustBackupUploadState.Error ->
                BackupUploadState.Error
        }
    }
}
