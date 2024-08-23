/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
}

val MessageShield.isCritical: Boolean
    get() = when (this) {
        is MessageShield.AuthenticityNotGuaranteed -> isCritical
        is MessageShield.UnknownDevice -> isCritical
        is MessageShield.UnsignedDevice -> isCritical
        is MessageShield.UnverifiedIdentity -> isCritical
        is MessageShield.SentInClear -> isCritical
    }
