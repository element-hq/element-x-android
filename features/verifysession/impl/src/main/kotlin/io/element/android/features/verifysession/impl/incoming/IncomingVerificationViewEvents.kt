/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.incoming

sealed interface IncomingVerificationViewEvents {
    data object GoBack : IncomingVerificationViewEvents
    data object StartVerification : IncomingVerificationViewEvents
    data object IgnoreVerification : IncomingVerificationViewEvents
    data object ConfirmVerification : IncomingVerificationViewEvents
    data object DeclineVerification : IncomingVerificationViewEvents
}
