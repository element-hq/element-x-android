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
            is RustBackupUploadState.CheckingIfUploadNeeded ->
                BackupUploadState.CheckingIfUploadNeeded(
                    backedUpCount = rustEnableProgress.backedUpCount.toInt(),
                    totalCount = rustEnableProgress.totalCount.toInt(),
                )
            RustBackupUploadState.Done ->
                BackupUploadState.Done
            is RustBackupUploadState.Uploading ->
                BackupUploadState.Uploading(
                    backedUpCount = rustEnableProgress.backedUpCount.toInt(),
                    totalCount = rustEnableProgress.totalCount.toInt(),
                )
            RustBackupUploadState.Waiting ->
                BackupUploadState.Waiting
            RustBackupUploadState.Error ->
                BackupUploadState.Error
        }
    }
}
