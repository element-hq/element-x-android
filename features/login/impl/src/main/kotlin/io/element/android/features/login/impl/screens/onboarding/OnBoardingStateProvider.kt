/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.onboarding

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.login.impl.login.LoginMode
import io.element.android.libraries.architecture.AsyncData

open class OnBoardingStateProvider : PreviewParameterProvider<OnBoardingState> {
    override val values: Sequence<OnBoardingState>
        get() = sequenceOf(
            anOnBoardingState(),
            anOnBoardingState(canLoginWithQrCode = true),
            anOnBoardingState(canCreateAccount = true),
            anOnBoardingState(canLoginWithQrCode = true, canCreateAccount = true),
            anOnBoardingState(canLoginWithQrCode = true, canCreateAccount = true, canReportBug = true),
            anOnBoardingState(defaultAccountProvider = "element.io", canCreateAccount = false, canReportBug = true),
        )
}

fun anOnBoardingState(
    productionApplicationName: String = "Element",
    defaultAccountProvider: String? = null,
    mustChooseAccountProvider: Boolean = false,
    canLoginWithQrCode: Boolean = false,
    canCreateAccount: Boolean = false,
    canReportBug: Boolean = false,
    loginMode: AsyncData<LoginMode> = AsyncData.Uninitialized,
    eventSink: (OnBoardingEvents) -> Unit = {},
) = OnBoardingState(
    productionApplicationName = productionApplicationName,
    defaultAccountProvider = defaultAccountProvider,
    mustChooseAccountProvider = mustChooseAccountProvider,
    canLoginWithQrCode = canLoginWithQrCode,
    canCreateAccount = canCreateAccount,
    canReportBug = canReportBug,
    loginMode = loginMode,
    eventSink = eventSink,
)
