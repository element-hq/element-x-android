/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.indicator.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.indicator.api.IndicatorService
import io.element.android.libraries.matrix.api.encryption.BackupState
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.verification.SessionVerificationService

@ContributesBinding(SessionScope::class)
class DefaultIndicatorService(
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
