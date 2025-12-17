/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.outgoing

sealed interface OutgoingVerificationViewEvents {
    data object RequestVerification : OutgoingVerificationViewEvents
    data object StartSasVerification : OutgoingVerificationViewEvents
    data object ConfirmVerification : OutgoingVerificationViewEvents
    data object DeclineVerification : OutgoingVerificationViewEvents
    data object Cancel : OutgoingVerificationViewEvents
    data object Reset : OutgoingVerificationViewEvents
}
