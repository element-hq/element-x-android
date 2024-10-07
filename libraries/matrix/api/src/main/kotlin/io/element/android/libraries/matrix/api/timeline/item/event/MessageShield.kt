/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.timeline.item.event

import androidx.compose.runtime.Immutable

@Immutable
sealed interface MessageShield {
    /** Not enough information available to check the authenticity. */
    data class AuthenticityNotGuaranteed(val isCritical: Boolean) : MessageShield

    /** The sending device isn't yet known by the Client. */
    data class UnknownDevice(val isCritical: Boolean) : MessageShield

    /** The sending device hasn't been verified by the sender. */
    data class UnsignedDevice(val isCritical: Boolean) : MessageShield

    /** The sender hasn't been verified by the Client's user. */
    data class UnverifiedIdentity(val isCritical: Boolean) : MessageShield

    /** An unencrypted event in an encrypted room. */
    data class SentInClear(val isCritical: Boolean) : MessageShield

    /** The sender was previously verified but is not anymore. */
    data class VerificationViolation(val isCritical: Boolean) : MessageShield
}

val MessageShield.isCritical: Boolean
    get() = when (this) {
        is MessageShield.AuthenticityNotGuaranteed -> isCritical
        is MessageShield.UnknownDevice -> isCritical
        is MessageShield.UnsignedDevice -> isCritical
        is MessageShield.UnverifiedIdentity -> isCritical
        is MessageShield.SentInClear -> isCritical
        is MessageShield.VerificationViolation -> isCritical
    }
