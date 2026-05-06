/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.linknewdevice

import kotlinx.coroutines.flow.Flow

interface LinkMobileHandler {
    val linkMobileStep: Flow<LinkMobileStep>
    suspend fun start()
}

sealed interface LinkMobileStep {
    data object Uninitialized : LinkMobileStep
    data object Starting : LinkMobileStep
    data class QrReady(val data: String) : LinkMobileStep
    data class OpeningVerificationUri(
        val verificationUri: String,
        val continuationMessageSender: ContinuationMessageSender,
    ) : LinkMobileStep

    data class WaitingForAuth(
        val continuationMessageSender: ContinuationMessageSender,
    ) : LinkMobileStep

    data class QrScanned(val checkCodeSender: CheckCodeSender) : LinkMobileStep
    data class Error(val errorType: ErrorType) : LinkMobileStep
    data object SyncingSecrets : LinkMobileStep
    data object Done : LinkMobileStep
}
