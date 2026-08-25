/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.outgoing

sealed interface OutgoingVerificationViewEvent {
    data object RequestVerification : OutgoingVerificationViewEvent
    data object StartSasVerification : OutgoingVerificationViewEvent
    data object ConfirmVerification : OutgoingVerificationViewEvent
    data object DeclineVerification : OutgoingVerificationViewEvent
    data object Cancel : OutgoingVerificationViewEvent
    data object Reset : OutgoingVerificationViewEvent
}
