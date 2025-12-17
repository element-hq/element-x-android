/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ftue.impl.sessionverification.choosemode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.zacsweers.metro.Inject
import io.element.android.features.logout.api.direct.DirectLogoutEvents
import io.element.android.features.logout.api.direct.DirectLogoutState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.mapState
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.encryption.RecoveryState

@Inject
class ChooseSelfVerificationModePresenter(
    private val encryptionService: EncryptionService,
    private val directLogoutPresenter: Presenter<DirectLogoutState>,
) : Presenter<ChooseSelfVerificationModeState> {
    @Composable
    override fun present(): ChooseSelfVerificationModeState {
        val hasDevicesToVerifyAgainst by encryptionService.hasDevicesToVerifyAgainst.collectAsState()
        val canEnterRecoveryKey by encryptionService.recoveryStateStateFlow
            .mapState { recoveryState ->
                when (recoveryState) {
                    RecoveryState.WAITING_FOR_SYNC,
                    RecoveryState.UNKNOWN -> AsyncData.Loading()
                    RecoveryState.INCOMPLETE -> AsyncData.Success(true)
                    RecoveryState.ENABLED,
                    RecoveryState.DISABLED -> AsyncData.Success(false)
                }
            }
            .collectAsState()
        val buttonsState by remember {
            derivedStateOf {
                val canUseAnotherDevice = hasDevicesToVerifyAgainst.dataOrNull()
                val canEnterRecoveryKey = canEnterRecoveryKey.dataOrNull()
                if (canUseAnotherDevice == null || canEnterRecoveryKey == null) {
                    AsyncData.Loading()
                } else {
                    AsyncData.Success(
                        ChooseSelfVerificationModeState.ButtonsState(
                            canUseAnotherDevice = canUseAnotherDevice,
                            canEnterRecoveryKey = canEnterRecoveryKey,
                        )
                    )
                }
            }
        }

        val directLogoutState = directLogoutPresenter.present()

        fun handleEvent(event: ChooseSelfVerificationModeEvent) {
            when (event) {
                ChooseSelfVerificationModeEvent.SignOut -> directLogoutState.eventSink(DirectLogoutEvents.Logout(ignoreSdkError = false))
            }
        }

        return ChooseSelfVerificationModeState(
            buttonsState = buttonsState,
            directLogoutState = directLogoutState,
            eventSink = ::handleEvent,
        )
    }
}
