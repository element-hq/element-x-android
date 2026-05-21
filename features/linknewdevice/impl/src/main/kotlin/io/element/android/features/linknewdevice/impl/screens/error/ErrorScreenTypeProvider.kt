/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.linknewdevice.impl.screens.error

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class ErrorScreenTypeProvider : PreviewParameterProvider<ErrorScreenType> {
    override val values: Sequence<ErrorScreenType> = sequenceOf(
        ErrorScreenType.Cancelled,
        ErrorScreenType.Declined,
        ErrorScreenType.Expired,
        ErrorScreenType.ProtocolNotSupported,
        ErrorScreenType.Mismatch2Digits,
        ErrorScreenType.InsecureChannelDetected,
        ErrorScreenType.SlidingSyncNotAvailable,
        ErrorScreenType.UnknownError,
        ErrorScreenType.OtherDeviceAlreadySignedIn,
    )
}
