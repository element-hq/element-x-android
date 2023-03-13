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
import io.element.android.libraries.matrix.api.verification.SessionVerificationServiceState
import io.element.android.libraries.matrix.api.verification.VerificationEmoji
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeSessionVerificationService : SessionVerificationService {
    private var _isReady = MutableStateFlow(false)
    private var _isVerified = false
    private var _verificationAttemptStatus = MutableStateFlow<SessionVerificationServiceState>(SessionVerificationServiceState.Initial)
    private var emojiList = emptyList<VerificationEmoji>()
    var shouldFail = false

    override val verificationAttemptStatus: StateFlow<SessionVerificationServiceState>
        get() = _verificationAttemptStatus
    override val isVerified: Boolean get()= _isVerified

    override val isReady: StateFlow<Boolean> = _isReady

    override fun requestVerification() {
        _verificationAttemptStatus.value = SessionVerificationServiceState.AcceptedVerificationRequest
        _verificationAttemptStatus.value = SessionVerificationServiceState.StartedSasVerification
        _verificationAttemptStatus.value = SessionVerificationServiceState.ReceivedVerificationData(emojiList)
    }

    override fun cancelVerification() {
        _verificationAttemptStatus.value = SessionVerificationServiceState.Canceled
    }

    override fun approveVerification() {
        if (!shouldFail) {
            _verificationAttemptStatus.value = SessionVerificationServiceState.Finished
        } else {
            _verificationAttemptStatus.value = SessionVerificationServiceState.Failed
        }
    }

    override fun declineVerification() {
        if (!shouldFail) {
            _verificationAttemptStatus.value = SessionVerificationServiceState.Canceled
        } else {
            _verificationAttemptStatus.value = SessionVerificationServiceState.Failed
        }
    }

    override fun startVerification() {
        _verificationAttemptStatus.value = SessionVerificationServiceState.StartedSasVerification
        _verificationAttemptStatus.value = SessionVerificationServiceState.ReceivedVerificationData(emojiList)
    }

    fun givenIsVerified(value: Boolean) {
        _isVerified = value
    }

    fun givenVerificationAttemptStatus(state: SessionVerificationServiceState) {
        _verificationAttemptStatus.value = state
    }

    fun givenIsReady(value: Boolean) {
        _isReady.value = value
    }

    fun givenEmojiList(emojis: List<VerificationEmoji>) {
        this.emojiList = emojis
    }
}
