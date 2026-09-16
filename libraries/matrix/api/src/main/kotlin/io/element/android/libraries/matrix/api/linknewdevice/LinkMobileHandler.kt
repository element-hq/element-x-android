/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.linknewdevice

import kotlinx.coroutines.flow.Flow

/**
 * Drives the linking of a new device that is able to scan a QR code, by showing one on this already signed in device.
 *
 * Progress is reported entirely through [linkMobileStep]; there is one handler per attempt, obtained from
 * [io.element.android.libraries.matrix.api.MatrixClient.createLinkMobileHandler].
 */
interface LinkMobileHandler {
    /** Each step of the flow, from generating the QR code to syncing the secrets to the new device. */
    val linkMobileStep: Flow<LinkMobileStep>

    /** Runs the whole flow, suspending until it finishes or fails; the outcome is reported through [linkMobileStep]. */
    suspend fun start()
}

sealed interface LinkMobileStep {
    data object Uninitialized : LinkMobileStep

    // Internal application step, for the UI
    data object CreatingQrCode : LinkMobileStep
    data object Starting : LinkMobileStep
    data class QrReady(val data: String) : LinkMobileStep
    data object QrRotating : LinkMobileStep

    data class WaitingForAuth(
        val verificationUri: String,
        val continuationMessageSender: ContinuationMessageSender,
    ) : LinkMobileStep

    data class QrScanned(val checkCodeSender: CheckCodeSender) : LinkMobileStep
    data class Error(val errorType: ErrorType) : LinkMobileStep
    data object SyncingSecrets : LinkMobileStep
    data object Done : LinkMobileStep
}
