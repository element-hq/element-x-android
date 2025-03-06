/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ftue.impl.sessionverification.choosemode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.element.android.features.logout.api.direct.DirectLogoutEvents
import io.element.android.features.logout.api.direct.DirectLogoutState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import javax.inject.Inject

class ChooseSelfVerificationModePresenter @Inject constructor(
    private val encryptionService: EncryptionService,
    private val directLogoutPresenter: Presenter<DirectLogoutState>,
) : Presenter<ChooseSelfVerificationModeState> {
    @Composable
    override fun present(): ChooseSelfVerificationModeState {
        val isLastDevice by encryptionService.isLastDevice.collectAsState()
        val recoveryState by encryptionService.recoveryStateStateFlow.collectAsState()
        val canEnterRecoveryKey by remember { derivedStateOf { recoveryState == RecoveryState.INCOMPLETE } }

        val directLogoutState = directLogoutPresenter.present()

        fun eventHandler(event: ChooseSelfVerificationModeEvent) {
            when (event) {
                ChooseSelfVerificationModeEvent.SignOut -> directLogoutState.eventSink(DirectLogoutEvents.Logout(ignoreSdkError = false))
            }
        }

        return ChooseSelfVerificationModeState(
            isLastDevice = isLastDevice,
            canEnterRecoveryKey = canEnterRecoveryKey,
            directLogoutState = directLogoutState,
            eventSink = ::eventHandler,
        )
    }
}
