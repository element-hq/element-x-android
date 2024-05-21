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

package io.element.android.libraries.indicator.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.indicator.api.IndicatorService
import io.element.android.libraries.matrix.api.encryption.BackupState
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class DefaultIndicatorService @Inject constructor(
    private val sessionVerificationService: SessionVerificationService,
    private val encryptionService: EncryptionService,
) : IndicatorService {
    @Composable
    override fun showRoomListTopBarIndicator(): State<Boolean> {
        val canVerifySession by sessionVerificationService.needsSessionVerification.collectAsState(initial = false)
        val settingChatBackupIndicator = showSettingChatBackupIndicator()

        return remember {
            derivedStateOf {
                canVerifySession || settingChatBackupIndicator.value
            }
        }
    }

    @Composable
    override fun showSettingChatBackupIndicator(): State<Boolean> {
        val backupState by encryptionService.backupStateStateFlow.collectAsState()
        val recoveryState by encryptionService.recoveryStateStateFlow.collectAsState()

        return remember {
            derivedStateOf {
                val showForBackup = backupState in listOf(
                    BackupState.UNKNOWN,
                )
                val showForRecovery = recoveryState in listOf(
                    RecoveryState.DISABLED,
                    RecoveryState.INCOMPLETE,
                )
                showForBackup || showForRecovery
            }
        }
    }
}
