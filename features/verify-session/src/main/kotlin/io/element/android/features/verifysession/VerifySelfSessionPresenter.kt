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

package io.element.android.features.verifysession

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.VerificationAttemptState
import javax.inject.Inject

class VerifySelfSessionPresenter @Inject constructor(
    private val sessionVerificationService: SessionVerificationService,
) : Presenter<VerifySelfSessionState> {

    @Composable
    override fun present(): VerifySelfSessionState {
        val verificationAttemptState by sessionVerificationService.verificationAttemptStatus.collectAsState()
        val state = when (verificationAttemptState) {
            VerificationAttemptState.Initial -> { VerificationState.Initial }
            VerificationAttemptState.RequestingVerification,
            VerificationAttemptState.StartingSasVerification,
            VerificationAttemptState.SasVerificationStarted,
            VerificationAttemptState.VerificationRequestAccepted -> { VerificationState.AwaitingOtherDeviceResponse }
            VerificationAttemptState.Failed, VerificationAttemptState.Canceled -> { VerificationState.Canceled }
            is VerificationAttemptState.Verifying -> {
                val emojis = (verificationAttemptState as VerificationAttemptState.Verifying).emojis.map {
                    EmojiEntry(it.symbol(), it.description())
                }
                val async = when (verificationAttemptState) {
                    is VerificationAttemptState.Verifying.Replying -> Async.Loading()
                    else -> Async.Uninitialized
                }
                VerificationState.Verifying(emojis, async)
            }
            VerificationAttemptState.Completed -> { VerificationState.Completed }
        }

        fun handleEvents(event: VerifySelfSessionEvents) {
            when (event) {
                VerifySelfSessionEvents.StartVerification -> sessionVerificationService.requestVerification()
                VerifySelfSessionEvents.ConfirmVerification -> sessionVerificationService.approveVerification()
                VerifySelfSessionEvents.Cancel -> sessionVerificationService.cancelVerification()
            }
        }

        return VerifySelfSessionState(
            verificationState = state,
            eventSink = ::handleEvents,
        )
    }
}
