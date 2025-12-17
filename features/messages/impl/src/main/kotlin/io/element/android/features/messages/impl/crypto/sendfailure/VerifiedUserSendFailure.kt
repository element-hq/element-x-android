/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.sendfailure

import androidx.compose.runtime.Immutable

@Immutable
sealed interface VerifiedUserSendFailure {
    data object None : VerifiedUserSendFailure

    sealed interface UnsignedDevice : VerifiedUserSendFailure {
        data object FromYou : UnsignedDevice
        data class FromOther(val userDisplayName: String) : UnsignedDevice
    }

    data class ChangedIdentity(
        val userDisplayName: String,
    ) : VerifiedUserSendFailure
}
