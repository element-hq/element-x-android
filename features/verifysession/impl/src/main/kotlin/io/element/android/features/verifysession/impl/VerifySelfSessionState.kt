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

package io.element.android.features.verifysession.impl

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.verification.SessionVerificationData

@Immutable
data class VerifySelfSessionState(
    val verificationFlowStep: VerificationStep,
    val displaySkipButton: Boolean,
    val eventSink: (VerifySelfSessionViewEvents) -> Unit,
) {
    @Stable
    sealed interface VerificationStep {
        data class Initial(val canEnterRecoveryKey: Boolean) : VerificationStep
        data object Canceled : VerificationStep
        data object AwaitingOtherDeviceResponse : VerificationStep
        data object Ready : VerificationStep
        data class Verifying(val data: SessionVerificationData, val state: AsyncData<Unit>) : VerificationStep
        data object Completed : VerificationStep
    }
}
