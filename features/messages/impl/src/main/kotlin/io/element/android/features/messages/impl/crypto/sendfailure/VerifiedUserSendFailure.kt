/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.sendfailure

import androidx.compose.runtime.Immutable

@Immutable
sealed interface VerifiedUserSendFailure {
    data object None : VerifiedUserSendFailure

    data class UnsignedDevice(
        val userDisplayName: String,
    ) : VerifiedUserSendFailure

    data class ChangedIdentity(
        val userDisplayName: String,
    ) : VerifiedUserSendFailure
}
