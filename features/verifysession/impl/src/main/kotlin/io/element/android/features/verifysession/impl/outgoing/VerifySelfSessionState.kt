/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.outgoing

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.verification.SessionVerificationData

@Immutable
data class VerifySelfSessionState(
    val step: Step,
    val signOutAction: AsyncAction<Unit>,
    val displaySkipButton: Boolean,
    val eventSink: (VerifySelfSessionViewEvents) -> Unit,
) {
    @Stable
    sealed interface Step {
        data object Loading : Step

        // FIXME canEnterRecoveryKey value is never read.
        data class Initial(val canEnterRecoveryKey: Boolean, val isLastDevice: Boolean = false) : Step
        data object UseAnotherDevice : Step
        data object Canceled : Step
        data object AwaitingOtherDeviceResponse : Step
        data object Ready : Step
        data class Verifying(val data: SessionVerificationData, val state: AsyncData<Unit>) : Step
        data object Completed : Step
        data object Skipped : Step
    }
}
