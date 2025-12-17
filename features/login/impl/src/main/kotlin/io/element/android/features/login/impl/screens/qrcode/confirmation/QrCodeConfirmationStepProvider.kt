/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.qrcode.confirmation

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class QrCodeConfirmationStepProvider : PreviewParameterProvider<QrCodeConfirmationStep> {
    override val values: Sequence<QrCodeConfirmationStep>
        get() = sequenceOf(
            QrCodeConfirmationStep.DisplayCheckCode("12"),
            QrCodeConfirmationStep.DisplayVerificationCode("123456"),
            QrCodeConfirmationStep.DisplayVerificationCode("123456789"),
        )
}
