/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.mapper

import io.element.android.libraries.matrix.api.encryption.identity.IdentityState
import uniffi.matrix_sdk_crypto.IdentityState as RustIdentityState

fun RustIdentityState.map(): IdentityState = when (this) {
    RustIdentityState.VERIFIED -> IdentityState.Verified
    RustIdentityState.PINNED -> IdentityState.Pinned
    RustIdentityState.PIN_VIOLATION -> IdentityState.PinViolation
    RustIdentityState.VERIFICATION_VIOLATION -> IdentityState.VerificationViolation
}
