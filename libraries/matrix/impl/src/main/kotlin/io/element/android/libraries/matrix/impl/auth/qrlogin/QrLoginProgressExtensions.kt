/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth.qrlogin

import io.element.android.libraries.matrix.api.auth.qrlogin.QrCodeLoginStep
import org.matrix.rustcomponents.sdk.QrLoginProgress

fun QrLoginProgress.toStep(): QrCodeLoginStep {
    return when (this) {
        is QrLoginProgress.EstablishingSecureChannel -> QrCodeLoginStep.EstablishingSecureChannel(checkCodeString)
        is QrLoginProgress.Starting -> QrCodeLoginStep.Starting
        is QrLoginProgress.WaitingForToken -> QrCodeLoginStep.WaitingForToken(userCode)
        is QrLoginProgress.Done -> QrCodeLoginStep.Finished
    }
}
