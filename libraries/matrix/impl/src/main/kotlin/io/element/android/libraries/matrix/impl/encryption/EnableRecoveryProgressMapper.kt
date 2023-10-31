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

import io.element.android.libraries.matrix.api.encryption.EnableRecoveryProgress
import org.matrix.rustcomponents.sdk.EnableRecoveryProgress as RustEnableRecoveryProgress

class EnableRecoveryProgressMapper {
    fun map(rustEnableProgress: RustEnableRecoveryProgress): EnableRecoveryProgress {
        return when (rustEnableProgress) {
            is RustEnableRecoveryProgress.CreatingRecoveryKey -> EnableRecoveryProgress.CreatingRecoveryKey
            is RustEnableRecoveryProgress.CreatingBackup -> EnableRecoveryProgress.CreatingBackup
            is RustEnableRecoveryProgress.BackingUp -> EnableRecoveryProgress.BackingUp(
                backedUpCount = rustEnableProgress.backedUpCount.toInt(),
                totalCount = rustEnableProgress.totalCount.toInt(),
            )
            is RustEnableRecoveryProgress.Done -> EnableRecoveryProgress.Done(
                recoveryKey = rustEnableProgress.recoveryKey
            )
        }
    }
}
