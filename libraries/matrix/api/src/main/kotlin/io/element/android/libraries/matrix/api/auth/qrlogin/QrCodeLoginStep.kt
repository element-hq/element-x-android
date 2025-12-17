/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.auth.qrlogin

sealed interface QrCodeLoginStep {
    data object Uninitialized : QrCodeLoginStep
    data class EstablishingSecureChannel(val checkCode: String) : QrCodeLoginStep
    data object Starting : QrCodeLoginStep
    data class WaitingForToken(val userCode: String) : QrCodeLoginStep
    data object SyncingSecrets : QrCodeLoginStep
    data class Failed(val error: QrLoginException) : QrCodeLoginStep
    data object Finished : QrCodeLoginStep
}
