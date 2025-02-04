/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.onboarding.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class OnBoardingStateProvider : PreviewParameterProvider<OnBoardingState> {
    override val values: Sequence<OnBoardingState>
        get() = sequenceOf(
            anOnBoardingState(),
            anOnBoardingState(canLoginWithQrCode = true),
            anOnBoardingState(canCreateAccount = true),
            anOnBoardingState(canLoginWithQrCode = true, canCreateAccount = true),
        )
}

fun anOnBoardingState(
    productionApplicationName: String = "Element",
    canLoginWithQrCode: Boolean = false,
    canCreateAccount: Boolean = false
) = OnBoardingState(
    productionApplicationName = productionApplicationName,
    canLoginWithQrCode = canLoginWithQrCode,
    canCreateAccount = canCreateAccount
)
