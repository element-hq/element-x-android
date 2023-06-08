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

package io.element.android.libraries.matrix.test.verification

import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.VerificationFlowState
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.api.verification.VerificationEmoji
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeSessionVerificationService : SessionVerificationService {
    private val _isReady = MutableStateFlow(false)
    private val _sessionVerifiedStatus = MutableStateFlow<SessionVerifiedStatus>(SessionVerifiedStatus.Unknown)
    private var _verificationFlowState = MutableStateFlow<VerificationFlowState>(VerificationFlowState.Initial)
    private var emojiList = emptyList<VerificationEmoji>()
    var shouldFail = false

    override val verificationFlowState: StateFlow<VerificationFlowState>
        get() = _verificationFlowState

    override val sessionVerifiedStatus: StateFlow<SessionVerifiedStatus> = _sessionVerifiedStatus

    override val isReady: StateFlow<Boolean> = _isReady

    override suspend fun requestVerification() {
        _verificationFlowState.value = VerificationFlowState.AcceptedVerificationRequest
        _verificationFlowState.value = VerificationFlowState.StartedSasVerification
        _verificationFlowState.value = VerificationFlowState.ReceivedVerificationData(emojiList)
    }

    override suspend fun cancelVerification() {
        _verificationFlowState.value = VerificationFlowState.Canceled
    }

    override suspend fun approveVerification() {
        if (!shouldFail) {
            _verificationFlowState.value = VerificationFlowState.Finished
        } else {
            _verificationFlowState.value = VerificationFlowState.Failed
        }
    }

    override suspend fun declineVerification() {
        if (!shouldFail) {
            _verificationFlowState.value = VerificationFlowState.Canceled
        } else {
            _verificationFlowState.value = VerificationFlowState.Failed
        }
    }

    override suspend fun startVerification() {
        _verificationFlowState.value = VerificationFlowState.StartedSasVerification
        _verificationFlowState.value = VerificationFlowState.ReceivedVerificationData(emojiList)
    }

    fun givenVerifiedStatus(status: SessionVerifiedStatus) {
        _sessionVerifiedStatus.value = status
    }

    fun givenVerificationFlowState(state: VerificationFlowState) {
        _verificationFlowState.value = state
    }

    fun givenIsReady(value: Boolean) {
        _isReady.value = value
    }

    fun givenEmojiList(emojis: List<VerificationEmoji>) {
        this.emojiList = emojis
    }

    override suspend fun reset() {
        _verificationFlowState.value = VerificationFlowState.Initial
    }
}
