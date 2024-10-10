/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.verifysession.impl

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.verification.SessionVerificationData

@Immutable
data class VerifySelfSessionState(
    val verificationFlowStep: VerificationStep,
    val signOutAction: AsyncAction<String?>,
    val displaySkipButton: Boolean,
    val eventSink: (VerifySelfSessionViewEvents) -> Unit,
) {
    @Stable
    sealed interface VerificationStep {
        data object Loading : VerificationStep

        // FIXME canEnterRecoveryKey value is never read.
        data class Initial(val canEnterRecoveryKey: Boolean, val isLastDevice: Boolean = false) : VerificationStep
        data object Canceled : VerificationStep
        data object AwaitingOtherDeviceResponse : VerificationStep
        data object Ready : VerificationStep
        data class Verifying(val data: SessionVerificationData, val state: AsyncData<Unit>) : VerificationStep
        data object Completed : VerificationStep
        data object Skipped : VerificationStep
    }
}
