/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
