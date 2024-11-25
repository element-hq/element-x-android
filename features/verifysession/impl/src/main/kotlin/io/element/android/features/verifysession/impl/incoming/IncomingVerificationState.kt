/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.incoming

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.verification.SessionVerificationData

@Immutable
data class IncomingVerificationState(
    val step: Step,
    val eventSink: (IncomingVerificationViewEvents) -> Unit,
) {
    @Stable
    sealed interface Step {
        data class Initial(
            val deviceDisplayName: String,
            val deviceId: DeviceId,
            val formattedSignInTime: String,
            val isWaiting: Boolean,
        ) : Step

        data class Verifying(
            val data: SessionVerificationData,
            val isWaiting: Boolean,
        ) : Step

        data object Canceled : Step
        data object Completed : Step
        data object Failure : Step
    }
}
