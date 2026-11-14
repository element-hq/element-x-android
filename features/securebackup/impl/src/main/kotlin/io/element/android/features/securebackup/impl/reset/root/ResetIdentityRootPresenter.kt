/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.reset.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus

@Inject
class ResetIdentityRootPresenter(
    private val matrixClient: MatrixClient,
    private val encryptionService: EncryptionService,
    private val sessionVerificationService: SessionVerificationService,
) : Presenter<ResetIdentityRootState> {
    @Composable
    override fun present(): ResetIdentityRootState {
        var displayConfirmDialog by remember { mutableStateOf(false) }
        val sessionVerifiedStatus by sessionVerificationService.sessionVerifiedStatus.collectAsState()
        val hasDevicesToVerifyAgainst by encryptionService.hasDevicesToVerifyAgainst.collectAsState()
        val recoveryState by encryptionService.recoveryStateStateFlow.collectAsState()

        // A verified session, or any state that is still loading, is treated as having other options.
        val isResetTheOnlyOption = sessionVerifiedStatus == SessionVerifiedStatus.NotVerified &&
            hasDevicesToVerifyAgainst.dataOrNull() == false &&
            recoveryState in listOf(RecoveryState.ENABLED, RecoveryState.DISABLED)

        val hasEncryptedRooms by produceState<AsyncData<Boolean>>(AsyncData.Loading(), isResetTheOnlyOption) {
            if (isResetTheOnlyOption) {
                value = AsyncData.Loading()
                // Treat a failure as having encrypted rooms, so the warning is never softer than it should be.
                value = AsyncData.Success(matrixClient.hasEncryptedRooms().getOrDefault(true))
            }
        }

        fun handleEvent(event: ResetIdentityRootEvent) {
            displayConfirmDialog = when (event) {
                ResetIdentityRootEvent.Continue -> true
                ResetIdentityRootEvent.DismissDialog -> false
            }
        }

        return ResetIdentityRootState(
            isResetTheOnlyOption = isResetTheOnlyOption,
            hasEncryptedRooms = hasEncryptedRooms,
            displayConfirmationDialog = displayConfirmDialog,
            eventSink = ::handleEvent,
        )
    }
}
