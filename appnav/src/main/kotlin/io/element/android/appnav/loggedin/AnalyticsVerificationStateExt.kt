/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.appnav.loggedin

import im.vector.app.features.analytics.plan.CryptoSessionStateChange
import im.vector.app.features.analytics.plan.UserProperties
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus

fun SessionVerifiedStatus.toAnalyticsUserPropertyValue(): UserProperties.VerificationState? {
    return when (this) {
        // we don't need to report transient states
        SessionVerifiedStatus.Unknown -> null
        SessionVerifiedStatus.NotVerified -> UserProperties.VerificationState.NotVerified
        SessionVerifiedStatus.Verified -> UserProperties.VerificationState.Verified
    }
}

fun RecoveryState.toAnalyticsUserPropertyValue(): UserProperties.RecoveryState? {
    return when (this) {
        RecoveryState.ENABLED -> UserProperties.RecoveryState.Enabled
        RecoveryState.DISABLED -> UserProperties.RecoveryState.Disabled
        RecoveryState.INCOMPLETE -> UserProperties.RecoveryState.Incomplete
        // we don't need to report transient states
        else -> null
    }
}
fun SessionVerifiedStatus.toAnalyticsStateChangeValue(): CryptoSessionStateChange.VerificationState? {
    return when (this) {
        // we don't need to report transient states
        SessionVerifiedStatus.Unknown -> null
        SessionVerifiedStatus.NotVerified -> CryptoSessionStateChange.VerificationState.NotVerified
        SessionVerifiedStatus.Verified -> CryptoSessionStateChange.VerificationState.Verified
    }
}

fun RecoveryState.toAnalyticsStateChangeValue(): CryptoSessionStateChange.RecoveryState? {
    return when (this) {
        RecoveryState.ENABLED -> CryptoSessionStateChange.RecoveryState.Enabled
        RecoveryState.DISABLED -> CryptoSessionStateChange.RecoveryState.Disabled
        RecoveryState.INCOMPLETE -> CryptoSessionStateChange.RecoveryState.Incomplete
        // we don't need to report transient states
        else -> null
    }
}
