/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.linknewdevice

import io.element.android.libraries.matrix.api.auth.qrlogin.QrCodeDecodeException
import kotlinx.coroutines.flow.StateFlow

/**
 * Drives the linking of a new device that cannot scan a QR code, such as a desktop: this device scans the code the other one displays.
 *
 * Progress is reported entirely through [linkDesktopStep]; there is one handler per attempt, obtained from
 * [io.element.android.libraries.matrix.api.MatrixClient.createLinkDesktopHandler].
 */
interface LinkDesktopHandler {
    /** Each step of the flow, from establishing the secure channel to syncing the secrets to the new device. */
    val linkDesktopStep: StateFlow<LinkDesktopStep>

    /**
     * Runs the whole flow from the QR code just scanned, suspending until it finishes or fails.
     * A code that cannot be decoded is reported as an invalid-QR-code step rather than as a thrown error.
     *
     * @param data the raw content read from the QR code shown by the other device.
     */
    suspend fun handleScannedQrCode(data: ByteArray)
}

sealed interface LinkDesktopStep {
    data object Uninitialized : LinkDesktopStep
    data object Starting : LinkDesktopStep

    data class WaitingForAuth(
        val verificationUri: String,
        val continuationMessageSender: ContinuationMessageSender,
    ) : LinkDesktopStep

    data class EstablishingSecureChannel(
        val checkCode: UByte,
        val checkCodeString: String,
    ) : LinkDesktopStep

    data class InvalidQrCode(
        val error: QrCodeDecodeException,
    ) : LinkDesktopStep

    data class Error(
        val errorType: ErrorType,
    ) : LinkDesktopStep

    data object SyncingSecrets : LinkDesktopStep

    data object Done : LinkDesktopStep
}
