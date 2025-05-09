/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.qrcode.error

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.login.impl.qrcode.QrCodeErrorScreenType

class QrCodeErrorScreenTypeProvider : PreviewParameterProvider<QrCodeErrorScreenType> {
    override val values: Sequence<QrCodeErrorScreenType> = sequenceOf(
        QrCodeErrorScreenType.Cancelled,
        QrCodeErrorScreenType.Declined,
        QrCodeErrorScreenType.Expired,
        QrCodeErrorScreenType.ProtocolNotSupported,
        QrCodeErrorScreenType.InsecureChannelDetected,
        QrCodeErrorScreenType.SlidingSyncNotAvailable,
        QrCodeErrorScreenType.UnknownError
    )
}
